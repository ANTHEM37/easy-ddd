package io.github.anthem37.easy.ddd.common.cqrs.query;

import java.util.concurrent.CompletableFuture;

/**
 * 查询总线接口
 * 负责查询的路由和执行
 *
 * @author anthem37
 * @since 2025/8/13 18:42:15
 */
public interface IQueryBus {

    /**
     * 发送查询并执行
     *
     * @param query 要执行的查询
     * @param <R>   查询结果类型
     * @return 查询结果
     */
    <R> R send(IQuery<R> query);

    /**
     * 异步发送查询
     *
     * @param query 要执行的查询
     * @param <R>   查询结果类型
     * @return 包含查询结果的CompletableFuture
     */
    <R> CompletableFuture<R> sendAsync(IQuery<R> query);

    /**
     * 获取已注册的处理器数量
     *
     * @return 处理器数量
     */
    int getHandlerCount();
}
