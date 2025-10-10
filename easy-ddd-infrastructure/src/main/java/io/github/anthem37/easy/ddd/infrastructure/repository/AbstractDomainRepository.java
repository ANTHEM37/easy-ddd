package io.github.anthem37.easy.ddd.infrastructure.repository;

import io.github.anthem37.easy.ddd.common.assertion.Assert;
import io.github.anthem37.easy.ddd.domain.event.DomainEventPublisher;
import io.github.anthem37.easy.ddd.domain.event.IDomainEvent;
import io.github.anthem37.easy.ddd.domain.model.AbstractAggregateRoot;
import io.github.anthem37.easy.ddd.domain.repository.IDomainRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 仓储基础实现类
 * 提供通用的CRUD操作实现，支持规约模式和事件发布
 *
 * @param <T>  聚合根类型
 * @param <ID> 聚合根标识类型
 * @author anthem37
 * @date 2025/8/14 16:52:19
 */
@Slf4j
public abstract class AbstractDomainRepository<T extends AbstractAggregateRoot<ID>, ID> implements IDomainRepository<T, ID> {

    @Override
    public Optional<T> findById(ID id) {
        Assert.notNull(id, "ID不能为空");
        return doFindById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void save(T aggregate) {
        Assert.notNull(aggregate, "聚合不能为空");
        //插入
        log.debug("插入聚合: {}", aggregate.getId());
        doInsert(aggregate);
        // 发布领域事件
        publishDomainEvents(aggregate);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(T aggregate) {
        Assert.notNull(aggregate, "聚合不能为空");
        //更新
        log.debug("更新聚合: {}", aggregate.getId());
        doUpdate(aggregate);
        // 发布领域事件
        publishDomainEvents(aggregate);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void remove(T aggregate) {
        Assert.notNull(aggregate, "聚合不能为空");
        //删除
        log.debug("删除聚合: {}", aggregate.getId());
        doDeleteById(aggregate.getId());
        // 发布领域事件
        publishDomainEvents(aggregate);
    }

    // 子类需要实现的抽象方法
    protected abstract Optional<T> doFindById(ID id);

    protected abstract void doInsert(T aggregate);

    protected abstract void doUpdate(T aggregate);

    protected abstract void doDeleteById(ID id);

    /**
     * 发布领域事件
     */
    protected void publishDomainEvents(T aggregate) {
        List<IDomainEvent> events = aggregate.getDomainEvents();
        for (IDomainEvent event : events) {
            DomainEventPublisher.publish(event);
        }
        aggregate.clearDomainEvents();
    }
}
