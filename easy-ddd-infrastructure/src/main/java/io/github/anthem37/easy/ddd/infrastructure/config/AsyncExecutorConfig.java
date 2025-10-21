package io.github.anthem37.easy.ddd.infrastructure.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.io.Serial;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 异步执行器配置
 * 为不同的异步任务提供专用的线程池
 * 支持通过外部配置文件进行参数化配置
 *
 * @author anthem37
 * @since 2025/8/13 15:38:42
 */
@Slf4j
@Configuration
@ConfigurationProperties(prefix = "easy.ddd.async")
@Data
public class AsyncExecutorConfig {

    /**
     * 查询线程池配置
     */
    private ExecutorProperties query = new ExecutorProperties();

    /**
     * 命令线程池配置
     */
    private ExecutorProperties command = new ExecutorProperties();


    /**
     * 事件线程池配置
     */
    private ExecutorProperties domainEvent = new ExecutorProperties();

    /**
     * 事件线程池配置
     */
    private ExecutorProperties applicationEvent = new ExecutorProperties();

    private ExecutorProperties createCpuBoundDefaults() {
        ExecutorProperties defaults = new ExecutorProperties();
        defaults.setCorePoolSizeMultiplier(1.0);
        defaults.setMaxPoolSizeMultiplier(2.0);
        defaults.setQueueCapacity(Math.max(50, Runtime.getRuntime().availableProcessors() * 10));
        defaults.setKeepAliveSeconds(30);
        defaults.setAwaitTerminationSeconds(30);
        defaults.setRejectedExecutionPolicy(ExecutorProperties.RejectedExecutionPolicy.CALLER_RUNS);
        return defaults;
    }

    private ExecutorProperties createBalancedDefaults() {
        ExecutorProperties defaults = new ExecutorProperties();
        defaults.setCorePoolSizeMultiplier(1.0);
        defaults.setMaxPoolSizeMultiplier(1.5);
        defaults.setQueueCapacity(200);
        defaults.setKeepAliveSeconds(120);
        defaults.setAwaitTerminationSeconds(45);
        defaults.setRejectedExecutionPolicy(ExecutorProperties.RejectedExecutionPolicy.CALLER_RUNS);
        return defaults;
    }

    private ExecutorProperties createIoBoundDefaults() {
        ExecutorProperties defaults = new ExecutorProperties();
        defaults.setCorePoolSizeMultiplier(1.0);
        // For I/O-bound tasks, a higher max pool size is beneficial.
        defaults.setMaxPoolSizeMultiplier(4.0);
        defaults.setQueueCapacity(500);
        defaults.setKeepAliveSeconds(300);
        defaults.setAwaitTerminationSeconds(60);
        defaults.setRejectedExecutionPolicy(ExecutorProperties.RejectedExecutionPolicy.CALLER_RUNS);
        return defaults;
    }

    /**
     * 查询处理专用线程池
     * 查询通常是CPU密集型操作，线程数不宜过多
     */
    @Bean("queryExecutor")
    public Executor queryExecutor() {
        return createExecutor(query, "Query", createCpuBoundDefaults());
    }

    /**
     * 事件处理专用线程池
     * 事件处理通常涉及IO操作（数据库、消息队列、外部服务调用）
     */
    @Bean("domainEventExecutor")
    public Executor domainEventExecutor() {
        return createExecutor(domainEvent, "DomainEvent", createIoBoundDefaults());
    }

    /**
     * 事件处理专用线程池
     * 事件处理通常涉及IO操作（数据库、消息队列、外部服务调用）
     */
    @Bean("applicationEventExecutor")
    public Executor applicationEventExecutor() {
        return createExecutor(applicationEvent, "ApplicationEvent", createIoBoundDefaults());
    }

    /**
     * 通用任务专用线程池
     * 用于处理各种通用异步任务，采用平衡配置
     */
    @Bean("commandExecutor")
    public Executor commandExecutor() {
        return createExecutor(command, "Command", createBalancedDefaults());
    }

    /**
     * 创建线程池执行器
     *
     * @param props      线程池配置属性
     * @param namePrefix 线程名前缀
     * @return 线程池执行器
     */
    private Executor createExecutor(ExecutorProperties props, String namePrefix, ExecutorProperties defaultProps) {
        props.mergeWith(defaultProps);
        ThreadPoolTaskExecutor executor = new MonitorableThreadPoolTaskExecutor();

        int cpuCores = Runtime.getRuntime().availableProcessors();
        int corePoolSize = Math.max(1, (int) (cpuCores * props.getCorePoolSizeMultiplier()));
        int maxPoolSize = Math.max(corePoolSize, (int) (cpuCores * props.getMaxPoolSizeMultiplier()));

        // 设置核心线程数
        executor.setCorePoolSize(corePoolSize);
        // 设置最大线程数
        executor.setMaxPoolSize(maxPoolSize);
        // 设置队列容量
        executor.setQueueCapacity(props.getQueueCapacity());
        // 设置线程名前缀
        executor.setThreadNamePrefix(namePrefix + "-");
        // 设置线程空闲时间
        executor.setKeepAliveSeconds(props.getKeepAliveSeconds());
        // 设置拒绝策略
        executor.setRejectedExecutionHandler(getRejectedExecutionHandler(props.getRejectedExecutionPolicy()));
        // 设置等待任务完成后关闭
        executor.setWaitForTasksToCompleteOnShutdown(props.isWaitForTasksToCompleteOnShutdown());
        // 设置等待时间
        executor.setAwaitTerminationSeconds(props.getAwaitTerminationSeconds());

        executor.initialize();

        log.info("{} 线程池初始化完成 - CPU核心数: {}, 核心线程数: {}, 最大线程数: {}, 队列容量: {}", namePrefix, cpuCores, executor.getCorePoolSize(), executor.getMaxPoolSize(), props.getQueueCapacity());

        return executor;
    }

    /**
     * 根据策略名称获取拒绝策略处理器
     *
     * @param policy 策略名称
     * @return 拒绝策略处理器
     */
    private RejectedExecutionHandler getRejectedExecutionHandler(ExecutorProperties.RejectedExecutionPolicy policy) {
        switch (policy) {
            case CALLER_RUNS:
                return new ThreadPoolExecutor.CallerRunsPolicy();
            case DISCARD_OLDEST:
                return new ThreadPoolExecutor.DiscardOldestPolicy();
            case DISCARD:
                return new ThreadPoolExecutor.DiscardPolicy();
            case ABORT:
                return new ThreadPoolExecutor.AbortPolicy();
            default:
                return new ThreadPoolExecutor.AbortPolicy();
        }
    }

    /**
     * 线程池配置属性
     */
    @Data
    public static class ExecutorProperties {
        /**
         * 核心线程数乘数（相对于CPU核心数）
         */
        private double corePoolSizeMultiplier;

        /**
         * 最大线程数乘数（相对于CPU核心数）
         */
        private double maxPoolSizeMultiplier;

        /**
         * 队列容量
         */
        private int queueCapacity;

        /**
         * 线程空闲时间（秒）
         */
        private int keepAliveSeconds;

        /**
         * 拒绝策略（CALLER_RUNS, DISCARD_OLDEST, DISCARD, ABORT）
         */
        private RejectedExecutionPolicy rejectedExecutionPolicy;

        /**
         * 是否等待任务完成后关闭
         */
        private boolean waitForTasksToCompleteOnShutdown = true;

        /**
         * 等待时间（秒）
         */
        private int awaitTerminationSeconds;

        public void mergeWith(ExecutorProperties defaults) {
            if (corePoolSizeMultiplier <= 0 && defaults.getCorePoolSizeMultiplier() > 0) {
                corePoolSizeMultiplier = defaults.getCorePoolSizeMultiplier();
            }
            if (maxPoolSizeMultiplier <= 0 && defaults.getMaxPoolSizeMultiplier() > 0) {
                maxPoolSizeMultiplier = defaults.getMaxPoolSizeMultiplier();
            }
            if (queueCapacity <= 0 && defaults.getQueueCapacity() > 0) {
                queueCapacity = defaults.getQueueCapacity();
            }
            if (keepAliveSeconds <= 0 && defaults.getKeepAliveSeconds() > 0) {
                keepAliveSeconds = defaults.getKeepAliveSeconds();
            }
            if (awaitTerminationSeconds <= 0 && defaults.getAwaitTerminationSeconds() > 0) {
                awaitTerminationSeconds = defaults.getAwaitTerminationSeconds();
            }
            if (rejectedExecutionPolicy == null && defaults.getRejectedExecutionPolicy() != null) {
                rejectedExecutionPolicy = defaults.getRejectedExecutionPolicy();
            }
        }

        /**
         * 拒绝策略枚举
         */
        public enum RejectedExecutionPolicy {
            /**
             * 调用者运行策略
             */
            CALLER_RUNS,
            /**
             * 丢弃最旧任务策略
             */
            DISCARD_OLDEST,
            /**
             * 丢弃任务策略
             */
            DISCARD,
            /**
             * 拒绝策略：抛出异常策略
             */
            ABORT
        }
    }

    /**
     * 可监控的线程池任务执行器
     * 扩展ThreadPoolTaskExecutor，添加监控功能
     */
    public static class MonitorableThreadPoolTaskExecutor extends ThreadPoolTaskExecutor {

        @Serial
        private static final long serialVersionUID = 1L;

        @Override
        public void execute(Runnable task) {
            // 可以在这里添加监控逻辑，如记录任务执行时间、计数等
            super.execute(task);
        }

        /**
         * 获取活跃线程数
         */
        @Override
        public int getActiveCount() {
            return super.getActiveCount();
        }

        /**
         * 获取线程池大小
         */
        @Override
        public int getPoolSize() {
            return super.getPoolSize();
        }

        /**
         * 获取队列大小
         */
        @Override
        public int getQueueSize() {
            return super.getThreadPoolExecutor().getQueue().size();
        }

        /**
         * 获取已完成任务数
         */
        public long getCompletedTaskCount() {
            return super.getThreadPoolExecutor().getCompletedTaskCount();
        }

        /**
         * 获取任务总数
         */
        public long getTaskCount() {
            return super.getThreadPoolExecutor().getTaskCount();
        }
    }
}
