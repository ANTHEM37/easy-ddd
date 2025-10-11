package io.github.anthem37.easy.ddd.common.cqrs.query;

/**
 * 查询处理器接口
 *
 * @param <Q> 查询类型
 * @param <R> 返回结果类型
 * @author anthem37
 * @since 2025/8/14 11:32:45
 */
public interface IQueryHandler<Q extends IQuery<R>, R> {

    /**
     * 处理查询
     */
    R handle(Q query);

    /**
     * 获取支持的命令类型
     *
     * @return 命令类型
     */
    Class<Q> getSupportedQueryType();
}