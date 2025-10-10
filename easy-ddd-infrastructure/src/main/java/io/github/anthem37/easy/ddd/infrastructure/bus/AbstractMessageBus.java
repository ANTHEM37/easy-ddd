package io.github.anthem37.easy.ddd.infrastructure.bus;

import io.github.anthem37.easy.ddd.common.assertion.Assert;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;

/**
 * 消息总线抽象基类
 * 提供命令总线和查询总线的通用功能
 *
 * @param <M> 消息类型（命令或查询）
 * @param <H> 处理器类型（命令处理器或查询处理器）
 * @author anthem37
 * @date 2025/8/13 14:35:27
 */
@Slf4j
public abstract class AbstractMessageBus<M, H> implements ApplicationContextAware {

    // 缓存处理器映射关系
    protected final Map<Class<?>, H> handlerCache = new ConcurrentHashMap<>();

    protected ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    /**
     * 获取执行器
     */
    protected abstract Executor getExecutor();

    /**
     * 获取消息类型名称（用于日志）
     */
    protected abstract String getMessageTypeName();

    /**
     * 验证消息
     */
    protected abstract boolean isValid(M message);

    /**
     * 处理消息
     */
    protected abstract <R> R handleMessage(H handler, M message);

    /**
     * 发送消息并获取结果
     */
    public <R> R send(M message) {
        Assert.notNull(message, getMessageTypeName() + "不能为空");

        String messageClassName = message.getClass().getSimpleName();
        log.debug("处理{}: {}", getMessageTypeName(), messageClassName);

        // 验证消息
        Assert.isTrue(isValid(message), getMessageTypeName() + "验证失败: " + messageClassName);

        H handler = findHandler(message);
        Assert.notNull(handler, "找不到" + getMessageTypeName() + "处理器: " + messageClassName);

        try {
            R result = handleMessage(handler, message);
            log.debug("{}处理完成: {}", getMessageTypeName(), messageClassName);
            return result;
        } catch (Exception e) {
            log.error("{}处理失败: {} - {}", getMessageTypeName(), messageClassName, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 异步发送消息
     */
    public <R> CompletableFuture<R> sendAsync(M message) {
        log.debug("异步处理{}: {}", getMessageTypeName(), message.getClass().getSimpleName());
        return CompletableFuture.supplyAsync(() -> this.<R>send(message), getExecutor());
    }

    /**
     * 获取处理器数量
     */
    public int getHandlerCount() {
        return handlerCache.size();
    }

    /**
     * 查找消息对应的处理器
     */
    protected abstract H findHandler(M message);
}
