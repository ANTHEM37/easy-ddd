package io.github.anthem37.easy.ddd.application.event;

import io.github.anthem37.easy.ddd.common.event.TriggeredPhase;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * 应用事件发布器
 * 纯应用层实现，不依赖外部框架
 *
 * @author anthem37
 * @since 2025/8/13 18:05:37
 */
@Slf4j
public class ApplicationEventPublisher {

    @Getter
    private static IApplicationEventPublisher eventPublisher;

    /**
     * 设置事件发布器实现
     */
    public static void setEventPublisher(IApplicationEventPublisher publisher) {
        ApplicationEventPublisher.eventPublisher = publisher;
    }

    /**
     * 发布单个事件
     */
    public static void publish(IApplicationEvent event) {
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
    public static void publishWith(IApplicationEvent event, IApplicationEventPublisher publisher) {
        if (event == null || publisher == null) {
            return;
        }
        publisher.publish(event);
    }

    /**
     * 使用指定发布器按阶段发布事件（不改变全局发布器）
     */
    public static void publishWithPhase(IApplicationEvent event, TriggeredPhase phase, IApplicationEventPublisher publisher) {
        if (event == null || publisher == null) {
            return;
        }
        publisher.publish(new TriggeredPhaseApplicationEvent(event, phase));
    }

    /**
     * 按指定阶段发布
     */
    public static void publishWithPhase(IApplicationEvent event, TriggeredPhase phase) {
        publish(new TriggeredPhaseApplicationEvent(event, phase));
    }

    /**
     * 便捷方法：发布为事务提交后阶段
     */
    public static void publishAfterCommit(IApplicationEvent event) {
        publishWithPhase(event, TriggeredPhase.AFTER_COMMIT);
    }

    /**
     * 便捷方法：发布为事务回滚后阶段
     */
    public static void publishAfterRollback(IApplicationEvent event) {
        publishWithPhase(event, TriggeredPhase.AFTER_ROLLBACK);
    }
}
