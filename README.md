# easy-ddd

一个轻量级的 DDD（领域驱动设计）与 CQRS 编排框架，聚焦“用例编排、命令与查询路由、领域事件发布与处理”的通用基础设施，基于 Spring Boot 3.5 与 Java 21。

## 目录
- [easy-ddd](#easy-ddd)
  - [目录](#目录)
    - [模块结构](#模块结构)
  - [总览架构](#总览架构)
  - [快速上手](#快速上手)
  - [最小可运行示例](#最小可运行示例)
  - [约束与最佳实践](#约束与最佳实践)
  - [参考](#参考)

### 模块结构
- easy-ddd-bom：依赖版本对齐与统一管理
- easy-ddd-common：通用工具与 CQRS 接口、业务编排 BizFlow
- easy-ddd-domain：纯领域层模型与事件协议
- easy-ddd-application：应用服务标记接口与用例编排入口
- easy-ddd-infrastructure：Spring 集成、总线实现与自动装配

## 总览架构

```mermaid
flowchart LR
  A[Application 层] -->|sendCommand/sendQuery| B[Infrastructure: CommandBus/QueryBus]
  B -->|ICommandHandler/IQueryHandler| C[Domain 层服务/聚合]
  C -->|发布领域事件| D[DomainEventPublisher]
  D -->|EventPublisher 实现| E[Infrastructure: SpringDomainEventPublisher]
  E -->|@EventListener / @TransactionalEventListener| F[Infrastructure: AbstractEventHandler<T>]
  A -->|用例编排| G[Common: BizFlow]
  G -->|调用| B
```

## 快速上手

1. 引入依赖（父 POM 已导入 `spring-boot-dependencies`、`mapstruct` 等）
2. 自动装配：基础设施模块已提供 `AutoConfiguration.imports` 与 `spring.factories`，开箱可用
3. 定义命令/查询及其处理器，领域事件与事件处理器
4. 编写应用服务，实现用例编排（使用 `IApplicationService` 或 `BizFlow`）

## 最小可运行示例

- 目录结构
```
demo/
 ├─ DemoApplication.java
 ├─ command/CreateOrderCommand.java
 ├─ command/CreateOrderHandler.java
 ├─ query/GetOrderQuery.java
 ├─ query/GetOrderHandler.java
 ├─ event/OrderCreatedEvent.java
 └─ app/OrderApplicationService.java
```

- 代码
```java
// DemoApplication.java
@SpringBootApplication
public class DemoApplication {
  public static void main(String[] args) { SpringApplication.run(DemoApplication.class, args); }
}

// command/CreateOrderCommand.java
public record CreateOrderCommand(String userId, BigDecimal amount) implements ICommand<String> {
  @Override public boolean isValid() { return userId != null && amount != null && amount.compareTo(BigDecimal.ZERO) > 0; }
}

// command/CreateOrderHandler.java
@Component
public class CreateOrderHandler implements ICommandHandler<CreateOrderCommand, String> {
  @Override public String handle(CreateOrderCommand cmd) { return "ORDER-" + UUID.randomUUID(); }
  @Override public Class<CreateOrderCommand> getSupportedCommandType() { return CreateOrderCommand.class; }
}

// query/GetOrderQuery.java
public record GetOrderQuery(String orderId) implements IQuery<String> { }
@Component
public class GetOrderHandler implements IQueryHandler<GetOrderQuery, String> {
  @Override public String handle(GetOrderQuery q) { return "Order:" + q.orderId(); }
  @Override public Class<GetOrderQuery> getSupportedQueryType() { return GetOrderQuery.class; }
}

// event/OrderCreatedEvent.java
public record OrderCreatedEvent(String orderId) implements IDomainEvent {
  @Override public String getEventType() { return "OrderCreated"; }
  @Override public boolean isAsync() { return true; }
}

// app/OrderApplicationService.java
@Service
public class OrderApplicationService implements IApplicationService {
  @Autowired private ICommandBus commandBus;
  @Autowired private IQueryBus queryBus;
  @Override public ICommandBus getCommandBus() { return commandBus; }
  @Override public IQueryBus getQueryBus() { return queryBus; }

  public String createAndFetch(String userId, BigDecimal amount) {
    String orderId = sendCommand(new CreateOrderCommand(userId, amount));
    return sendQuery(new GetOrderQuery(orderId));
  }
}
```

- 启动说明
  - 业务项目引入 `easy-ddd-infrastructure` 依赖后，CommandBus/QueryBus/EventPublisher 自动装配
  - 运行 `DemoApplication`，注入 `OrderApplicationService` 即可调用 `createAndFetch`

## 约束与最佳实践

- 命令改变状态、查询只读；分别实现 `ICommandHandler` 与 `IQueryHandler`
- 处理器必须返回具体支持类型，确保路由缓存命中
- 领域事件同步/异步与触发阶段选择需结合事务边界与幂等策略
- Assert 抛业务异常，BizFlowException 专用于编排校验

## 参考

- 自动装配入口：`easy-ddd-infrastructure/src/main/resources/META-INF/spring/`（imports 与 factories）
- 线程池配置：`AsyncExecutorConfig`，支持 `easy.ddd.async.*` 外部化配置