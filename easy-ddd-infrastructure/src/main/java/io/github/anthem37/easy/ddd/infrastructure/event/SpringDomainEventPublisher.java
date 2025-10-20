package io.github.anthem37.easy.ddd.infrastructure.event;

import io.github.anthem37.easy.ddd.domain.event.IDomainEvent;
import io.github.anthem37.easy.ddd.domain.event.IDomainEventPublisher;
import org.springframework.context.ApplicationEventPublisher;

import java.util.concurrent.Executor;

/**
 * Spring框架实现的领域事件发布器
 * 实现领域层定义的EventPublisher接口
 *
 * @author anthem37
 * @since 2025/8/14 16:15:47
 */
public class SpringDomainEventPublisher extends SpringEventPublisher<IDomainEvent<?>> implements IDomainEventPublisher {
    public SpringDomainEventPublisher(ApplicationEventPublisher applicationEventPublisher, Executor eventExecutor) {
        super(applicationEventPublisher, eventExecutor);
    }
}
