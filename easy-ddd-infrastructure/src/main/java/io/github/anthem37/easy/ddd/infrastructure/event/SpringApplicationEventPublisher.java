package io.github.anthem37.easy.ddd.infrastructure.event;

import io.github.anthem37.easy.ddd.application.event.IApplicationEvent;
import io.github.anthem37.easy.ddd.application.event.IApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisher;

import java.util.concurrent.Executor;

/**
 * Spring框架实现的应用事件发布器
 * 实现应用层定义的EventPublisher接口
 *
 * @author anthem37
 * @since 2025/8/14 16:15:47
 */
public class SpringApplicationEventPublisher extends SpringEventPublisher<IApplicationEvent> implements IApplicationEventPublisher {
    public SpringApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher, Executor eventExecutor) {
        super(applicationEventPublisher, eventExecutor);
    }
}
