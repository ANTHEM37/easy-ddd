package io.github.anthem37.easy.ddd.domain.event;

import io.github.anthem37.easy.ddd.common.event.TriggeredPhase;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * 领域事件发布器
 * 纯领域层实现，不依赖外部框架
 *
 * @author anthem37
 * @since 2025/8/13 18:05:37
 */
@Slf4j
public class DomainEventPublisher {

    @Getter
    private static volatile IDomainEventPublisher eventPublisher;

    /**
     * 设置事件发布器实现
     */
    public static void setEventPublisher(IDomainEventPublisher publisher) {
        DomainEventPublisher.eventPublisher = publisher;
    }

    /**
     * 发布单个事件
     */
    public static <ID> void publish(IDomainEvent<ID> event) {
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
     * 使用指定发布器发布事件（不改变全局发布器），便于接入 MQ 实现
     */
    public static <ID> void publishWith(IDomainEvent<ID> event, IDomainEventPublisher publisher) {
        if (event == null || publisher == null) {
            return;
        }
        publisher.publish(event);
    }

    /**
     * 使用指定发布器按阶段发布事件（不改变全局发布器）
     */
    public static <ID> void publishWithPhase(IDomainEvent<ID> event, TriggeredPhase phase, IDomainEventPublisher publisher) {
        if (event == null || publisher == null) {
            return;
        }
        publisher.publish(new TriggeredPhaseDomainEvent<>(event, phase));
    }

    /**
     * 按指定阶段发布
     */
    public static <ID> void publishWithPhase(IDomainEvent<ID> event, TriggeredPhase phase) {
        publish(new TriggeredPhaseDomainEvent<>(event, phase));
    }

    /**
     * 便捷方法：发布为事务提交后阶段
     */
    public static <ID> void publishAfterCommit(IDomainEvent<ID> event) {
        publishWithPhase(event, TriggeredPhase.AFTER_COMMIT);
    }

    /**
     * 便捷方法：发布为事务回滚后阶段
     */
    public static <ID> void publishAfterRollback(IDomainEvent<ID> event) {
        publishWithPhase(event, TriggeredPhase.AFTER_ROLLBACK);
    }
}
