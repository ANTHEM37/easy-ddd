package io.github.anthem37.easy.ddd.domain.event;

/**
 * 事件处理器接口
 *
 * @param <T> 事件类型
 * @author anthem37
 * @since 2025/8/14 10:12:36
 */
public interface IEventHandler<T extends IDomainEvent> {

    /**
     * 处理事件
     *
     * @param event 要处理的事件
     */
    void handle(T event);

    /**
     * 获取支持的事件类型
     *
     * @return 事件类型
     */
    Class<T> getSupportedEventType();
}