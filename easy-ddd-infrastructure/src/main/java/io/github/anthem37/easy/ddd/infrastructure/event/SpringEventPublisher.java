package io.github.anthem37.easy.ddd.infrastructure.event;

import io.github.anthem37.easy.ddd.common.assertion.Assert;
import io.github.anthem37.easy.ddd.common.event.IEvent;
import io.github.anthem37.easy.ddd.common.event.IEventPublisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;

import java.util.concurrent.Executor;

/**
 * Spring框架实现的领域事件发布器
 * 实现领域层定义的EventPublisher接口
 *
 * @author anthem37
 * @since 2025/8/14 16:15:47
 */
@Slf4j
public class SpringEventPublisher<T extends IEvent> implements IEventPublisher<T> {

    private final ApplicationEventPublisher applicationEventPublisher;
    private final Executor eventExecutor;

    public SpringEventPublisher(ApplicationEventPublisher applicationEventPublisher, Executor eventExecutor) {
        this.applicationEventPublisher = applicationEventPublisher;
        this.eventExecutor = eventExecutor;
    }

    @Override
    public void publish(IEvent event) {
        if (event == null) {
            log.warn("尝试发布空事件");
            return;
        }
        boolean async = event.isAsync();
        log.debug("通过Spring发布{}领域事件: {}", (async ? "异步" : "同步"), event.getEventType());
        publishEventInternal(event);
    }

    /**
     * 内部方法：发布单个事件
     *
     * @param event 要发布的事件
     */
    private void publishEventInternal(IEvent event) {
        boolean async = event.isAsync();
        try {
            if (async) {
                eventExecutor.execute(() -> applicationEventPublisher.publishEvent(event));
                return;
            }
            applicationEventPublisher.publishEvent(event);
        } catch (Exception e) {
            String mode = async ? "异步" : "";
            log.error("{}事件发布失败: {} - {}", mode, event.getEventType(), e.getMessage(), e);
            // 同步发布失败时抛出异常，异步发布失败时只记录日志
            if (!async) {
                Assert.fail("事件发布失败: " + e.getMessage());
            }
        }
    }

}
