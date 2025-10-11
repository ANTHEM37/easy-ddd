package io.github.anthem37.easy.ddd.domain.event;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * 领域事件发布器
 * 纯领域层实现，不依赖外部框架
 *
 * @author anthem37
 * @date 2025/8/13 18:05:37
 */
@Slf4j
public class DomainEventPublisher {

    @Getter
    private static EventPublisher eventPublisher;

    /**
     * 设置事件发布器实现
     */
    public static void setEventPublisher(EventPublisher publisher) {
        DomainEventPublisher.eventPublisher = publisher;
    }

    /**
     * 发布单个事件
     */
    public static void publish(IDomainEvent event) {
        if (event == null) {
            return;
        }

        if (eventPublisher != null) {
            eventPublisher.publish(event);
            return;
        }
        log.debug("未设置事件发布器，事件将被忽略: {}", event.getEventType());
    }

    /**
     * 按指定阶段发布
     */
    public static void publishWithPhase(IDomainEvent event, IDomainEvent.TriggeredPhase phase) {
        publish(new TriggeredPhaseEvent(event, phase));
    }

    /**
     * 便捷方法：发布为事务提交后阶段
     */
    public static void publishAfterCommit(IDomainEvent event) {
        publishWithPhase(event, IDomainEvent.TriggeredPhase.AFTER_COMMIT);
    }

    /**
     * 便捷方法：发布为事务回滚后阶段
     */
    public static void publishAfterRollback(IDomainEvent event) {
        publishWithPhase(event, IDomainEvent.TriggeredPhase.AFTER_ROLLBACK);
    }

    /**
     * 事件发布器接口（实现类需要把自己注册到DomainEventPublisher）
     * 由基础设施层实现
     */
    public interface EventPublisher {

        /**
         * 发布领域事件
         */
        void publish(IDomainEvent event);
    }
}
