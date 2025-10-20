package io.github.anthem37.easy.ddd.domain.event;

import io.github.anthem37.easy.ddd.common.event.TriggeredPhase;
import io.github.anthem37.easy.ddd.common.event.TriggeredPhaseEvent;
import lombok.Getter;

/**
 * 包装一个事件并指定触发阶段
 */
@Getter
public class TriggeredPhaseDomainEvent<ID> extends TriggeredPhaseEvent implements IDomainEvent<ID> {

    private final ID aggregateId;

    public TriggeredPhaseDomainEvent(IDomainEvent<ID> delegate, TriggeredPhase phase) {
        super(delegate, phase);
        this.aggregateId = delegate.getAggregateId();
    }
}