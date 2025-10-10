package io.github.anthem37.easy.ddd.application;


import io.github.anthem37.easy.ddd.common.cqrs.command.ICommand;
import io.github.anthem37.easy.ddd.common.cqrs.command.ICommandBus;
import io.github.anthem37.easy.ddd.common.cqrs.query.IQuery;
import io.github.anthem37.easy.ddd.common.cqrs.query.IQueryBus;

/**
 * 应用服务标记接口
 * 应用服务负责：
 * 1. 业务用例编排
 * 2. 事务管理
 * 3. 权限控制
 * 4. DTO转换
 *
 * @author anthem37
 * @date 2025/8/13 16:45:32
 */
public interface IApplicationService {

    /**
     * 获取命令总线
     */
    ICommandBus getCommandBus();

    /**
     * 获取查询总线
     */
    IQueryBus getQueryBus();

    /**
     * 发送命令
     */
    default <R> R sendCommand(ICommand<R> command) {
        return getCommandBus().send(command);
    }

    /**
     * 发送查询
     */
    default <T extends IQuery<R>, R> R sendQuery(T query) {
        return getQueryBus().send(query);
    }

}