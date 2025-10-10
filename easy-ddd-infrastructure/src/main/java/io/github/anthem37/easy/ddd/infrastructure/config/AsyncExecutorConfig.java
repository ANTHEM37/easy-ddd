package io.github.anthem37.easy.ddd.infrastructure.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
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
 * @date 2025/8/13 15:38:42
 */
@Slf4j
@Configuration
@EnableAsync
@ConfigurationProperties(prefix = "ddd.async")
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
    private ExecutorProperties event = new ExecutorProperties();

    /**
     * 查询处理专用线程池
     * 查询通常是CPU密集型操作，线程数不宜过多
     */
    @Bean("queryExecutor")
    public Executor queryExecutor() {
        // 默认配置：CPU密集型
        if (query.getCorePoolSizeMultiplier() <= 0) {
            query.setCorePoolSizeMultiplier(1.0); // 默认为CPU核心数
        }
        if (query.getMaxPoolSizeMultiplier() <= 0) {
            query.setMaxPoolSizeMultiplier(1.5); // 默认为CPU核心数 * 1.5
        }
        if (query.getQueueCapacity() <= 0) {
            query.setQueueCapacity(50); // 默认队列容量
        }
        if (query.getKeepAliveSeconds() <= 0) {
            query.setKeepAliveSeconds(60); // 默认空闲时间
        }
        if (query.getAwaitTerminationSeconds() <= 0) {
            query.setAwaitTerminationSeconds(30); // 默认等待时间
        }
        if (query.getRejectedExecutionPolicy() == null) {
            query.setRejectedExecutionPolicy("CALLER_RUNS"); // 默认拒绝策略
        }

        return createExecutor(query, "Query");
    }

    /**
     * 事件处理专用线程池
     * 事件处理通常涉及IO操作（数据库、消息队列、外部服务调用）
     */
    @Bean("eventExecutor")
    public Executor eventExecutor() {
        // 默认配置：IO密集型
        if (event.getCorePoolSizeMultiplier() <= 0) {
            event.setCorePoolSizeMultiplier(1.0); // 默认为CPU核心数
        }
        if (event.getMaxPoolSizeMultiplier() <= 0) {
            event.setMaxPoolSizeMultiplier(2.0); // 默认为CPU核心数 * 2
        }
        if (event.getQueueCapacity() <= 0) {
            event.setQueueCapacity(500); // 默认队列容量
        }
        if (event.getKeepAliveSeconds() <= 0) {
            event.setKeepAliveSeconds(300); // 默认空闲时间
        }
        if (event.getAwaitTerminationSeconds() <= 0) {
            event.setAwaitTerminationSeconds(60); // 默认等待时间
        }
        if (event.getRejectedExecutionPolicy() == null) {
            event.setRejectedExecutionPolicy("DISCARD_OLDEST"); // 默认拒绝策略
        }

        return createExecutor(event, "Event");
    }

    /**
     * 通用任务专用线程池
     * 用于处理各种通用异步任务，采用平衡配置
     */
    @Bean("commandExecutor")
    public Executor commandExecutor() {
        // 默认配置：平衡型
        if (command.getCorePoolSizeMultiplier() <= 0) {
            command.setCorePoolSizeMultiplier(1.0); // 默认为CPU核心数
        }
        if (command.getMaxPoolSizeMultiplier() <= 0) {
            command.setMaxPoolSizeMultiplier(1.5); // 默认为CPU核心数 * 1.5
        }
        if (command.getQueueCapacity() <= 0) {
            command.setQueueCapacity(200); // 默认队列容量
        }
        if (command.getKeepAliveSeconds() <= 0) {
            command.setKeepAliveSeconds(120); // 默认空闲时间
        }
        if (command.getAwaitTerminationSeconds() <= 0) {
            command.setAwaitTerminationSeconds(45); // 默认等待时间
        }
        if (command.getRejectedExecutionPolicy() == null) {
            command.setRejectedExecutionPolicy("CALLER_RUNS"); // 默认拒绝策略
        }

        return createExecutor(command, "command");
    }

    /**
     * 创建线程池执行器
     *
     * @param props      线程池配置属性
     * @param namePrefix 线程名前缀
     * @return 线程池执行器
     */
    private Executor createExecutor(ExecutorProperties props, String namePrefix) {
        ThreadPoolTaskExecutor executor = new MonitorableThreadPoolTaskExecutor();

        int cpuCores = Runtime.getRuntime().availableProcessors();
        int corePoolSize = (int) (cpuCores * props.getCorePoolSizeMultiplier());
        int maxPoolSize = (int) (cpuCores * props.getMaxPoolSizeMultiplier());

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
    private RejectedExecutionHandler getRejectedExecutionHandler(String policy) {
        switch (policy.toUpperCase()) {
            case "CALLER_RUNS":
                return new ThreadPoolExecutor.CallerRunsPolicy();
            case "DISCARD_OLDEST":
                return new ThreadPoolExecutor.DiscardOldestPolicy();
            case "DISCARD":
                return new ThreadPoolExecutor.DiscardPolicy();
            case "ABORT":
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
        private String rejectedExecutionPolicy;

        /**
         * 是否等待任务完成后关闭
         */
        private boolean waitForTasksToCompleteOnShutdown = true;

        /**
         * 等待时间（秒）
         */
        private int awaitTerminationSeconds;
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
        public int getActiveCount() {
            return super.getActiveCount();
        }

        /**
         * 获取线程池大小
         */
        public int getPoolSize() {
            return super.getPoolSize();
        }

        /**
         * 获取队列大小
         */
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
