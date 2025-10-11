package io.github.anthem37.easy.ddd.infrastructure.event;

import io.github.anthem37.easy.ddd.common.assertion.Assert;
import io.github.anthem37.easy.ddd.domain.event.IDomainEvent;
import io.github.anthem37.easy.ddd.domain.event.IEventHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * 事件处理器抽象基类
 * 提供同步和异步事件处理的标准实现
 *
 * @author anthem37
 * @since 2025/8/14 12:18:53
 */
@Slf4j
public abstract class AbstractEventHandler<T extends IDomainEvent> implements IEventHandler<T> {

    /**
     * 处理事件
     */
    @EventListener
    @Override
    public void handle(T event) {
        if (event.getTriggeredPhase() != IDomainEvent.TriggeredPhase.IN_PROCESS) {
            return;
        }
        processEvent(event, "处理领域事件", this::doHandle, this::handleError);
    }

    /**
     * 在事务提交后处理事件
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleAfterCommit(T event) {
        if (event.getTriggeredPhase() != IDomainEvent.TriggeredPhase.AFTER_COMMIT) {
            return;
        }
        processEvent(event, "事务提交后处理领域事件", this::doHandleAfterCommit, this::handleAfterCommitError);
    }

    /**
     * 在事务回滚后处理事件
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_ROLLBACK)
    public void handleAfterRollback(T event) {
        if (event.getTriggeredPhase() != IDomainEvent.TriggeredPhase.IN_PROCESS
                && event.getTriggeredPhase() != IDomainEvent.TriggeredPhase.AFTER_ROLLBACK) {
            return;
        }
        processEvent(event, "事务回滚后处理领域事件", this::doHandleAfterRollback, this::handleAfterRollbackError);
    }

    /**
     * 通用事件处理逻辑
     *
     * @param event        事件对象
     * @param logPrefix    日志前缀
     * @param handler      事件处理函数
     * @param errorHandler 错误处理函数
     */
    private void processEvent(T event, String logPrefix, Consumer<T> handler, BiConsumer<T, Exception> errorHandler) {
        if (!canHandle(event)) {
            return;
        }

        log.debug("{}: {} - {}", logPrefix, event.getEventType(), event.getClass().getSimpleName());

        try {
            handler.accept(event);
            log.debug("{}完成: {}", logPrefix, event.getEventType());
        } catch (Exception e) {
            log.error("{}失败: {} - {}", logPrefix, event.getEventType(), e.getMessage(), e);
            errorHandler.accept(event, e);
        }
    }

    /**
     * 检查是否可以处理该事件
     * 子类可以重写此方法添加额外的过滤条件
     */
    protected boolean canHandle(T event) {
        return event != null && getSupportedEventType().isAssignableFrom(event.getClass());
    }

    /**
     * 具体的事件处理逻辑
     * 子类必须实现此方法
     */
    protected abstract void doHandle(T event);

    /**
     * 事务提交后的事件处理逻辑
     * 子类可以重写此方法，默认调用doHandle
     */
    protected void doHandleAfterCommit(T event) {
        doHandle(event);
    }

    /**
     * 事务回滚后的事件处理逻辑
     * 子类可以重写此方法，默认为空实现
     */
    protected void doHandleAfterRollback(T event) {
        // 默认不处理回滚后的事件
        log.debug("事务回滚后事件处理（默认空实现）: {}", event.getEventType());
    }

    /**
     * 处理同步事件处理异常
     * 子类可以重写此方法自定义异常处理
     */
    protected void handleError(T event, Exception e) {
        // 使用断言方式处理异常
        Assert.fail("事件处理失败: " + event.getEventType() + " - " + e.getMessage());
    }

    /**
     * 处理事务提交后事件处理异常
     * 子类可以重写此方法自定义异常处理
     */
    protected void handleAfterCommitError(T event, Exception e) {
        // 事务提交后处理异常默认只记录日志
        log.error("事务提交后事件处理异常，事件类型: {}", event.getEventType());
    }

    /**
     * 处理事务回滚后事件处理异常
     * 子类可以重写此方法自定义异常处理
     */
    protected void handleAfterRollbackError(T event, Exception e) {
        // 事务回滚后处理异常默认只记录日志
        log.error("事务回滚后事件处理异常，事件类型: {}", event.getEventType());
    }

    /**
     * 获取支持的事件类型
     * 子类需要实现此方法
     */
    public abstract Class<T> getSupportedEventType();
}