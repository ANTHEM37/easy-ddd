package io.github.anthem37.easy.ddd.common.event;

import lombok.Getter;

import java.util.Map;

/**
 * 包装一个事件并指定触发阶段
 */
@Getter
public class TriggeredPhaseEvent implements IEvent {

    private final IEvent delegate;
    private final TriggeredPhase phase;

    public TriggeredPhaseEvent(IEvent delegate, TriggeredPhase phase) {
        this.delegate = delegate;
        this.phase = phase;
    }

    @Override
    public String getEventType() {
        return delegate.getEventType();
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