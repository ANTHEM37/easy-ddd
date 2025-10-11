package io.github.anthem37.easy.ddd.domain.repository;


import io.github.anthem37.easy.ddd.domain.model.AbstractAggregateRoot;

import java.util.Optional;

/**
 * 仓储接口
 * 提供聚合的持久化抽象
 *
 * @param <T>  聚合根类型
 * @param <ID> 聚合根标识类型
 * @author anthem37
 * @since 2025/8/14 14:05:38
 */
public interface IDomainRepository<T extends AbstractAggregateRoot<ID>, ID> {

    /**
     * 根据ID查找聚合
     */
    Optional<T> findById(ID id);

    /**
     * 保存聚合
     */
    void save(T aggregate);

    /**
     * 更新聚合
     */
    void update(T aggregate);

    /**
     * 删除聚合
     */
    void remove(T aggregate);

}