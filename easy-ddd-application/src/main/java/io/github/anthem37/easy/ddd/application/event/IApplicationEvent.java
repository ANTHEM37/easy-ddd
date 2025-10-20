package io.github.anthem37.easy.ddd.application.event;

import io.github.anthem37.easy.ddd.common.event.IEvent;
import io.github.anthem37.easy.ddd.common.event.TriggeredPhase;

/**
 * 应用事件接口
 * 应用事件表示应用中发生的重要业务事件
 *
 * @author anthem37
 * @since 2025/8/13 20:15:42
 */
public interface IApplicationEvent extends IEvent {

    @Override
    default TriggeredPhase getTriggeredPhase() {

        return TriggeredPhase.IN_PROCESS;
    }
}
