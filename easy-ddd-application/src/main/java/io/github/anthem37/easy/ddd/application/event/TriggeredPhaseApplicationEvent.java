package io.github.anthem37.easy.ddd.application.event;

import io.github.anthem37.easy.ddd.common.event.TriggeredPhase;
import io.github.anthem37.easy.ddd.common.event.TriggeredPhaseEvent;
import lombok.Getter;

/**
 * 包装一个事件并指定触发阶段
 */
@Getter
public class TriggeredPhaseApplicationEvent extends TriggeredPhaseEvent implements IApplicationEvent {


    public TriggeredPhaseApplicationEvent(IApplicationEvent delegate, TriggeredPhase phase) {
        super(delegate, phase);
    }
}