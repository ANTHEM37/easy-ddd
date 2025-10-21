package io.github.anthem37.easy.ddd.infrastructure.event;

import io.github.anthem37.easy.ddd.domain.event.IDomainEvent;

/**
 * 领域事件处理基类
 * 处理领域事件的通用逻辑
 *
 * @author hb28301
 * @date 2025/10/21 10:20:06
 */
public abstract class AbstractDomainEventHandler<T extends IDomainEvent<?>> extends AbstractEventHandler<T> {
}
