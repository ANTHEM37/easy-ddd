package io.github.anthem37.easy.ddd.common.cqrs.command;

import java.util.concurrent.CompletableFuture;

/**
 * 命令总线接口
 * 负责命令的路由和执行
 *
 * @author anthem37
 * @date 2025/8/14 10:45:18
 */
public interface ICommandBus {

    /**
     * 发送命令并执行
     *
     * @param command 要执行的命令
     * @param <R>     命令执行结果类型
     * @return 命令执行结果
     */
    <R> R send(ICommand<R> command);

    /**
     * 异步发送命令
     *
     * @param command 要执行的命令
     */
    <R> CompletableFuture<R> sendAsync(ICommand<R> command);

    /**
     * 获取已注册的处理器数量
     *
     * @return 处理器数量
     */
    int getHandlerCount();
}
