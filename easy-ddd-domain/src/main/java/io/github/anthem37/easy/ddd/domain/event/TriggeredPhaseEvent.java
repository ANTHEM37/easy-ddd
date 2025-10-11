package io.github.anthem37.easy.ddd.domain.event;

import lombok.Getter;

import java.util.Map;

/**
 * 包装一个事件并指定触发阶段
 */
@Getter
public class TriggeredPhaseEvent implements IDomainEvent {

    private final IDomainEvent delegate;
    private final TriggeredPhase phase;

    public TriggeredPhaseEvent(IDomainEvent delegate, TriggeredPhase phase) {
        this.delegate = delegate;
        this.phase = phase;
    }

    @Override
    public String getEventType() {
        return delegate.getEventType();
    }

    @Override
    public Object getAggregateId() {
        return delegate.getAggregateId();
    }

    @Override
    public Map<String, Object> getEventData() {
        return delegate.getEventData();
    }

    @Override
    public Map<String, Object> getMetadata() {
        return delegate.getMetadata();
    }

    @Override
    public boolean isAsync() {
        return delegate.isAsync();
    }

    @Override
    public TriggeredPhase getTriggeredPhase() {
        return phase;
    }
}