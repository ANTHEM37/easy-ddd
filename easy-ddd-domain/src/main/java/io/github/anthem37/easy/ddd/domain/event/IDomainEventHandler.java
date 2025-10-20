package io.github.anthem37.easy.ddd.domain.event;

import io.github.anthem37.easy.ddd.common.event.IEventHandler;

/**
 * 事件处理器接口
 *
 * @param <ID>    聚合根ID类型
 * @param <EVENT> 事件类型
 * @author anthem37
 * @since 2025/8/14 10:12:36
 */
public interface IDomainEventHandler<ID, EVENT extends IDomainEvent<ID>> extends IEventHandler<EVENT> {

}