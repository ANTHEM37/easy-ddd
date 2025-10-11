# easy-ddd-application

应用层模块，提供 `IApplicationService` 标记接口与命令/查询编排入口，聚焦“用例编排、事务、权限、DTO 转换”。

## 目录
- [easy-ddd-application](#easy-ddd-application)
  - [目录](#目录)
  - [IApplicationService](#iapplicationservice)
  - [应用服务示例](#应用服务示例)
  - [结合 BizFlow 的编排](#结合-bizflow-的编排)
  - [事务与事件阶段实践](#事务与事件阶段实践)
  - [DTO 映射建议](#dto-映射建议)
  - [最佳实践](#最佳实践)

## IApplicationService

```java
public interface IApplicationService {
  ICommandBus getCommandBus();
  IQueryBus getQueryBus();
  default <R> R sendCommand(ICommand<R> command) { return getCommandBus().send(command); }
  default <T extends IQuery<R>, R> R sendQuery(T query) { return getQueryBus().send(query); }
}
```

- 在具体应用服务中注入 CommandBus / QueryBus 并实现上述方法即可
- 建议应用服务只做编排与事务，复杂业务交给领域层

## 应用服务示例

```java
@Service
public class UserApplicationService implements IApplicationService {
  @Autowired private ICommandBus commandBus;
  @Autowired private IQueryBus queryBus;
  @Override public ICommandBus getCommandBus() { return commandBus; }
  @Override public IQueryBus getQueryBus() { return queryBus; }

  @Transactional
  public UserDTO register(String phone) {
    String userId = sendCommand(new RegisterUserCommand(phone));
    return sendQuery(new GetUserQuery(userId));
  }
}
```

## 结合 BizFlow 的编排

```java
public BizFlow.Result registerWithFlow(String phone) {
  BizFlow flow = new BizFlow("user:register", "用户注册流程", getCommandBus(), getQueryBus())
      .addCommand("register", "注册用户", ctx -> new RegisterUserCommand(phone))
      .addQuery("detail", "查询详情", ctx -> new GetUserQuery(ctx.getResult("register", String.class)))
      .connect("register", "detail");
  return flow.execute();
}
```

```mermaid
flowchart LR
  A[register] -- cmd --> B[detail]
```

## 事务与事件阶段实践

- 写操作方法加 `@Transactional`
- 选择事件阶段：
  - 写后外部副作用：AFTER_COMMIT
  - 回滚后补偿：AFTER_ROLLBACK
  - 同步轻量侧效应：IN_PROCESS
- 事件处理失败策略：
  - 同步：抛错中断事务
  - 异步：记录日志、重试与幂等

## DTO 映射建议

- 使用 MapStruct 进行 DTO/实体转换（父 POM 已包含 `mapstruct` 及 `processor`）
- 约定：
  - Command/Query 命名后缀：`*Command`、`*Query`
  - 事件命名：领域过去式 `*Event`
  - 处理器命名：`*Handler`

## 最佳实践

- 将权限校验与事务放在应用服务，避免侵入领域模型
- 编排失败抛 `BizFlowException`，业务规则失败抛 `BusinessException`
- 处理器必须返回具体支持类型，确保路由缓存命中