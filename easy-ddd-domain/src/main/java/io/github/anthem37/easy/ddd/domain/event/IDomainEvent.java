package io.github.anthem37.easy.ddd.domain.event;

import io.github.anthem37.easy.ddd.common.event.IEvent;
import io.github.anthem37.easy.ddd.common.event.TriggeredPhase;

/**
 * 领域事件接口
 * 领域事件表示领域中发生的重要业务事件
 *
 * @author anthem37
 * @since 2025/8/13 20:15:42
 */
public interface IDomainEvent<ID> extends IEvent {

    /**
     * 获取聚合根ID
     * 标识事件来源的聚合
     */
    ID getAggregateId();

    @Override
    default TriggeredPhase getTriggeredPhase() {

        return TriggeredPhase.AFTER_COMMIT;
    }
}
