package io.github.anthem37.easy.ddd.infrastructure.bus.impl;

import io.github.anthem37.easy.ddd.common.cqrs.command.ICommand;
import io.github.anthem37.easy.ddd.common.cqrs.command.ICommandBus;
import io.github.anthem37.easy.ddd.common.cqrs.command.ICommandHandler;
import io.github.anthem37.easy.ddd.infrastructure.bus.AbstractMessageBus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * 命令总线实现
 * 负责命令的路由和执行
 *
 * @author anthem37
 * @since 2025/8/13 21:05:46
 */
@Slf4j
@AllArgsConstructor
public class CommandBus extends AbstractMessageBus<ICommand<?>, ICommandHandler<?, ?>> implements ICommandBus, InitializingBean {

    @Getter
    private final String messageTypeName = "命令";
    @Getter
    private Executor executor;

    @Override
    public <R> R send(ICommand<R> command) {

        return super.send(command);
    }

    @Override
    public <R> CompletableFuture<R> sendAsync(ICommand<R> command) {
        return super.sendAsync(command);
    }

    @Override
    protected boolean isValid(ICommand<?> message) {
        return message.isValid();
    }

    @Override
    @SuppressWarnings("unchecked")
    protected <R> R handleMessage(ICommandHandler<?, ?> handler, ICommand<?> message) {
        return (R) ((ICommandHandler<ICommand<?>, ?>) handler).handle(message);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected ICommandHandler<?, ?> findHandler(ICommand<?> message) {
        //优先从handlerCache取
        Class<? extends ICommand> messageClazz = message.getClass();
        ICommandHandler<?, ?> handler = handlerCache.get(messageClazz);
        if (handler != null) {
            return handler;
        }
        applicationContext.getBeansOfType(ICommandHandler.class).values().stream().filter(handlerTemp -> handlerTemp.getSupportedCommandType().isAssignableFrom(messageClazz)).findFirst().ifPresent(handlerTemp -> {
            handlerCache.put(messageClazz, handlerTemp);
        });
        return handlerCache.get(messageClazz);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        log.info("开始初始化命令处理器缓存...");

        Map<String, ICommandHandler> handlers = applicationContext.getBeansOfType(ICommandHandler.class);
        int registeredCount = 0;

        for (Map.Entry<String, ICommandHandler> entry : handlers.entrySet()) {
            String beanName = entry.getKey();
            ICommandHandler<?, ?> handler = entry.getValue();

            try {
                Class<?> supportedType = handler.getSupportedCommandType();
                if (supportedType != null) {
                    handlerCache.put(supportedType, handler);
                    registeredCount++;
                    log.debug("注册命令处理器: {} -> {}", supportedType.getSimpleName(), beanName);
                } else {
                    log.warn("命令处理器 {} 返回的支持类型为null，跳过注册", beanName);
                }
            } catch (Exception e) {
                log.error("注册命令处理器 {} 失败: {}", beanName, e.getMessage(), e);
                throw e;
            }
        }

        log.info("命令处理器缓存初始化完成，共注册 {} 个处理器", registeredCount);
    }
}
