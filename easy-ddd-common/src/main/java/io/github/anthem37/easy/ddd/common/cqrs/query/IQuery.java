package io.github.anthem37.easy.ddd.common.cqrs.query;

/**
 * 查询标记接口
 * 查询用于获取数据，不改变系统状态
 *
 * @param <R> 查询返回结果类型
 * @author anthem37
 * @date 2025/8/13 19:24:56
 */
public interface IQuery<R> {

    /**
     * 验证查询是否有效
     * 子类可以重写此方法添加验证逻辑
     *
     * @return true表示查询有效，false表示无效
     */
    default boolean isValid() {
        return true;
    }
}
