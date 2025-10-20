package io.github.anthem37.easy.ddd.domain.event;

import io.github.anthem37.easy.ddd.common.event.IEventPublisher;

/**
 * 事件发布器接口
 *
 * @author anthem37
 * @since 2025/8/14 16:15:47
 */
public interface IDomainEventPublisher extends IEventPublisher<IDomainEvent<?>> {

}
