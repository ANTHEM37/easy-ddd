package io.github.anthem37.easy.ddd.domain.service;


import io.github.anthem37.easy.ddd.domain.event.DomainEventPublisher;
import io.github.anthem37.easy.ddd.domain.event.IDomainEvent;

/**
 * 领域服务抽象基类
 * 提供通用的业务规则检查和事件发布功能
 *
 * @author anthem37
 * @date 2025/8/13 19:48:25
 */
public abstract class AbstractDomainService implements IDomainService {

    /**
     * 发布领域事件
     */
    protected void publishEvent(IDomainEvent event) {
        DomainEventPublisher.publish(event);
    }

    /**
     * 批量发布领域事件
     */
    protected void publishEvents(IDomainEvent... events) {
        for (IDomainEvent event : events) {
            publishEvent(event);
        }
    }

    /**
     * 执行业务操作并发布事件
     */
    protected <T> T executeAndPublish(BusinessOperation<T> operation, IDomainEvent... events) {
        T result = operation.execute();
        publishEvents(events);
        return result;
    }

    /**
     * 业务操作函数式接口
     */
    @FunctionalInterface
    protected interface BusinessOperation<T> {
        T execute();
    }
}