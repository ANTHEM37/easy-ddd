package io.github.anthem37.easy.ddd.domain.model;

import io.github.anthem37.easy.ddd.domain.event.IDomainEvent;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 聚合根基类
 * 聚合根是聚合的入口，负责维护业务不变性
 *
 * @param <ID> 聚合根标识类型
 * @author anthem37
 * @since 2025/8/14 09:27:38
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public abstract class AbstractAggregateRoot<ID> extends AbstractEntity<ID> {

    private final List<IDomainEvent> domainEvents = new ArrayList<>();

    /**
     * 添加领域事件
     */
    protected void addDomainEvent(IDomainEvent event) {
        domainEvents.add(event);
    }

    /**
     * 获取所有领域事件（只读）
     */
    public List<IDomainEvent> getDomainEvents() {
        return Collections.unmodifiableList(domainEvents);
    }

    /**
     * 清除所有领域事件
     */
    public void clearDomainEvents() {
        domainEvents.clear();
    }

    /**
     * 检查是否有未发布的事件
     */
    public boolean hasUnpublishedEvents() {
        return !domainEvents.isEmpty();
    }
}
