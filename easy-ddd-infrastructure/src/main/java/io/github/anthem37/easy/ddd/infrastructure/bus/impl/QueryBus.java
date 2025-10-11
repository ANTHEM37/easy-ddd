package io.github.anthem37.easy.ddd.infrastructure.bus.impl;

import io.github.anthem37.easy.ddd.common.cqrs.query.IQuery;
import io.github.anthem37.easy.ddd.common.cqrs.query.IQueryBus;
import io.github.anthem37.easy.ddd.common.cqrs.query.IQueryHandler;
import io.github.anthem37.easy.ddd.infrastructure.bus.AbstractMessageBus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * 查询总线实现
 * 负责查询的路由和执行
 *
 * @author anthem37
 * @since 2025/8/14 09:17:53
 */
@Slf4j
@AllArgsConstructor
public class QueryBus extends AbstractMessageBus<IQuery<?>, IQueryHandler<?, ?>> implements IQueryBus, InitializingBean {

    @Getter
    private final String messageTypeName = "查询";
    @Getter
    private Executor executor;

    @Override
    public <R> R send(IQuery<R> query) {
        return super.send(query);
    }

    @Override
    public <R> CompletableFuture<R> sendAsync(IQuery<R> query) {
        return super.sendAsync(query);
    }

    @Override
    protected boolean isValid(IQuery<?> message) {
        return message.isValid();
    }

    @Override
    @SuppressWarnings("unchecked")
    protected <R> R handleMessage(IQueryHandler<?, ?> handler, IQuery<?> message) {
        return (R) ((IQueryHandler<IQuery<?>, ?>) handler).handle(message);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected IQueryHandler<?, ?> findHandler(IQuery<?> message) {
        //优先从handlerCache取
        Class<? extends IQuery> messageClazz = message.getClass();
        IQueryHandler<?, ?> handler = handlerCache.get(messageClazz);
        if (handler != null) {
            return handler;
        }
        applicationContext.getBeansOfType(IQueryHandler.class).values().stream().filter(handlerTemp -> handlerTemp.getSupportedQueryType().isAssignableFrom(messageClazz)).findFirst().ifPresent(handlerTemp -> {
            handlerCache.put(messageClazz, handlerTemp);
        });
        return handlerCache.get(messageClazz);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        log.info("开始初始化查询处理器缓存...");

        Map<String, IQueryHandler> handlers = applicationContext.getBeansOfType(IQueryHandler.class);
        int registeredCount = 0;

        for (Map.Entry<String, IQueryHandler> entry : handlers.entrySet()) {
            String beanName = entry.getKey();
            IQueryHandler<?, ?> handler = entry.getValue();

            try {
                Class<?> supportedType = handler.getSupportedQueryType();
                if (supportedType != null) {
                    handlerCache.put(supportedType, handler);
                    registeredCount++;
                    log.debug("注册查询处理器: {} -> {}", supportedType.getSimpleName(), beanName);
                } else {
                    log.warn("查询处理器 {} 返回的支持类型为null，跳过注册", beanName);
                }
            } catch (Exception e) {
                log.error("注册查询处理器 {} 失败: {}", beanName, e.getMessage(), e);
                throw e;
            }
        }

        log.info("查询处理器缓存初始化完成，共注册 {} 个处理器", registeredCount);
    }
}
