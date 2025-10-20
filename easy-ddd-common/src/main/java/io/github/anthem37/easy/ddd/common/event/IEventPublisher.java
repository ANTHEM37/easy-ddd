package io.github.anthem37.easy.ddd.common.event;

/**
 * 事件发布器接口
 *
 * @author anthem37
 * @since 2025/8/14 16:15:47
 */
@FunctionalInterface
public interface IEventPublisher<T extends IEvent> {

    void publish(T event);
}
