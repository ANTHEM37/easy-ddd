# easy-ddd-infrastructure

基础设施层，提供与 Spring 的集成实现：命令/查询总线、事件发布器与自动装配、线程池配置、标准事件处理器基类。

## 目录
- [easy-ddd-infrastructure](#easy-ddd-infrastructure)
  - [目录](#目录)
  - [核心组件](#核心组件)
  - [自动装配与兼容性](#自动装配与兼容性)
  - [线程池配置与调优](#线程池配置与调优)
  - [事件处理器基类用法](#事件处理器基类用法)
  - [总线路由与缓存机制](#总线路由与缓存机制)
  - [错误处理与建议](#错误处理与建议)
  - [最佳实践](#最佳实践)
  - [FAQ](#faq)
  - [参考](#参考)

## 核心组件

- bus
  - AbstractMessageBus<M,H>：总线抽象，校验、处理器查找、同步/异步发送
  - CommandBus：命令总线实现（缓存处理器、初始化时预注册）
  - QueryBus：查询总线实现（缓存处理器、初始化时预注册）
- event
  - SpringDomainEventPublisher：基于 Spring 的事件发布器，支持同步/异步
  - AbstractEventHandler<T>：标准事件处理器基类，支持 IN_PROCESS / AFTER_COMMIT / AFTER_ROLLBACK
- config
  - AsyncExecutorConfig：查询/命令/事件三类线程池，支持外部化配置
  - EasyDDDAutoConfiguration：自动装配 CommandBus / QueryBus / EventPublisher 并注册到 DomainEventPublisher

## 自动装配与兼容性

- 现代方式（Spring Boot 3 推荐）：`META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`
  ```
  io.github.anthem37.easy.ddd.infrastructure.config.EasyDDDAutoConfiguration
  ```
- 兼容方式（legacy）：`src/main/resources/META-INF/spring.factories`
  ```
  org.springframework.boot.autoconfigure.EnableAutoConfiguration=\
  io.github.anthem37.easy.ddd.infrastructure.config.EasyDDDAutoConfiguration
  ```
- 引入该模块后，Spring Boot 将自动装配：
  - `ICommandBus`、`IQueryBus`（分别使用 `commandExecutor`、`queryExecutor`）
  - `DomainEventPublisher.EventPublisher`（使用 `ApplicationEventPublisher` 与 `eventExecutor`）
- 启动后注册发布器到领域层静态入口：
  ```java
  DomainEventPublisher.setEventPublisher(publisher);
  ```

## 线程池配置与调优

`AsyncExecutorConfig` 支持外部化配置前缀 `easy.ddd.async.*`：

```yaml
easy:
  ddd:
    async:
      query:
        corePoolSizeMultiplier: 1.0
        maxPoolSizeMultiplier: 1.5
        queueCapacity: 50
        keepAliveSeconds: 60
        rejectedExecutionPolicy: CALLER_RUNS
        awaitTerminationSeconds: 30
      command:
        corePoolSizeMultiplier: 1.0
        maxPoolSizeMultiplier: 1.5
        queueCapacity: 200
        keepAliveSeconds: 120
        rejectedExecutionPolicy: CALLER_RUNS
        awaitTerminationSeconds: 45
      event:
        corePoolSizeMultiplier: 1.0
        maxPoolSizeMultiplier: 2.0
        queueCapacity: 500
        keepAliveSeconds: 300
        rejectedExecutionPolicy: CALLER_RUNS
        awaitTerminationSeconds: 60
```

- 配置项说明
  - corePoolSizeMultiplier：核心线程数乘数（相对 CPU 核心数）
  - maxPoolSizeMultiplier：最大线程数乘数（相对 CPU 核心数）
  - queueCapacity：队列容量
  - keepAliveSeconds：空闲线程存活秒数
  - rejectedExecutionPolicy：拒绝策略（CALLER_RUNS / DISCARD_OLDEST / DISCARD / ABORT）
  - awaitTerminationSeconds：关闭时等待秒数
  - waitForTasksToCompleteOnShutdown：优雅关闭是否等待任务完成（默认 true）

- 场景建议
  - CPU 密集（大计算）：query/core=1.0, max=1.5, queue=50
  - IO 密集（外部调用/消息）：event/core=1.0, max=2.0, queue=500
  - 混合（通用业务）：command/core=1.0, max=1.5, queue=200

- 监控建议（可选）
  - `MonitorableThreadPoolTaskExecutor` 暴露指标：activeCount、poolSize、queueSize、completedTaskCount、taskCount
  - Micrometer 集成示例（骨架）：
    ```java
    @Bean
    MeterBinder commandExecutorMetrics(MonitorableThreadPoolTaskExecutor exec) {
      return registry -> {
        Gauge.builder("easy.ddd.command.queue.size", exec, MonitorableThreadPoolTaskExecutor::getQueueSize).register(registry);
        Gauge.builder("easy.ddd.command.active", exec, MonitorableThreadPoolTaskExecutor::getActiveCount).register(registry);
      };
    }
    ```

## 事件处理器基类用法

```java
@Component
public class OrderCreatedHandler extends AbstractEventHandler<OrderCreatedEvent> {
  @Override protected void doHandle(OrderCreatedEvent e) { /* 业务处理... */ }
  @Override protected void doHandleAfterCommit(OrderCreatedEvent e) { /* 发MQ/通知 */ }
  @Override public Class<OrderCreatedEvent> getSupportedEventType() { return OrderCreatedEvent.class; }
}
```

- IN_PROCESS：通过 `@EventListener` 同步处理
- AFTER_COMMIT / AFTER_ROLLBACK：通过 `@TransactionalEventListener` 在事务边界后处理

## 总线路由与缓存机制

- 处理器通过 `getSupported*Type()` 声明支持的消息类型
- 启动阶段 `afterPropertiesSet()` 预扫描并缓存
- 运行期若缓存未命中，将通过 `applicationContext.getBeansOfType` 查找并回填缓存
- 约定：请返回具体类型（如 `CreateOrderCommand.class`），避免返回接口或 Object，确保路由精准与缓存命中
- 多处理器匹配规则：使用 `isAssignableFrom` 首个匹配即命中；如出现歧义，建议收敛到明确的子类型或加限界接口

## 错误处理与建议

- 总线处理异常：记录日志并抛出原始异常
- 事件发布失败：
  - 同步发布：使用 `Assert.fail` 抛出业务异常（阻断事务）
  - 异步发布：仅记录日志，避免影响主流程；建议结合重试与幂等
- 事件处理异常：
  - 覆写 `handleError/handleAfterCommitError/handleAfterRollbackError` 自定义策略（报警、重试、死信）

## 最佳实践

- 命令/查询处理器避免阻塞 IO；异步事件处理使用 `eventExecutor`
- 合理设置线程池乘数与队列容量，结合 CPU 核心数与业务峰值
- 命令改变状态、查询只读；在领域层内进行不变式与规则校验
- 明确事务边界：写后事件用 AFTER_COMMIT；失败补偿用 AFTER_ROLLBACK

## FAQ

- Q：为何命令/查询需要 `getSupported*Type()`？
  - A：用于构建路由缓存，显著降低运行期查找成本。
- Q：如何切换到现代自动装配？
  - A：确保存在 `AutoConfiguration.imports` 文件；保留 `spring.factories` 以兼容旧项目（可选）。

## 参考

- `config/AsyncExecutorConfig`、`config/EasyDDDAutoConfiguration`
- `event/SpringDomainEventPublisher`、`event/AbstractEventHandler`
- `bus/AbstractMessageBus`、`bus/impl/CommandBus`、`bus/impl/QueryBus`