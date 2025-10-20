package io.github.anthem37.easy.ddd.infrastructure.config;

import io.github.anthem37.easy.ddd.application.event.IApplicationEventPublisher;
import io.github.anthem37.easy.ddd.common.cqrs.command.ICommandBus;
import io.github.anthem37.easy.ddd.common.cqrs.query.IQueryBus;
import io.github.anthem37.easy.ddd.domain.event.DomainEventPublisher;
import io.github.anthem37.easy.ddd.domain.event.IDomainEventPublisher;
import io.github.anthem37.easy.ddd.infrastructure.bus.impl.CommandBus;
import io.github.anthem37.easy.ddd.infrastructure.bus.impl.QueryBus;
import io.github.anthem37.easy.ddd.infrastructure.event.SpringApplicationEventPublisher;
import io.github.anthem37.easy.ddd.infrastructure.event.SpringDomainEventPublisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.util.concurrent.Executor;

/**
 * DDD框架自动配置类
 * 提供框架核心组件的自动装配
 *
 * @author anthem37
 * @since 2025/8/14 13:47:56
 */
@Slf4j
@Configuration
@EnableAsync
@EnableAspectJAutoProxy
@EnableTransactionManagement
@Import(AsyncExecutorConfig.class)
public class EasyDDDAutoConfiguration implements ApplicationRunner {

    /**
     * 命令总线
     */
    @Bean
    @ConditionalOnMissingBean(ICommandBus.class)
    public ICommandBus commandBus(@Qualifier("commandExecutor") Executor commandExecutor) {
        return new CommandBus(commandExecutor);
    }

    /**
     * 查询总线
     */
    @Bean
    @ConditionalOnMissingBean(IQueryBus.class)
    public IQueryBus queryBus(@Qualifier("queryExecutor") Executor queryExecutor) {
        return new QueryBus(queryExecutor);
    }

    /**
     * 领域事件发布器
     */
    @Bean
    @ConditionalOnMissingBean(IDomainEventPublisher.class)
    public IDomainEventPublisher domainEventPublisher(ApplicationEventPublisher applicationEventPublisher, @Qualifier("domainEventExecutor") Executor eventExecutor) {
        IDomainEventPublisher publisher = new SpringDomainEventPublisher(applicationEventPublisher, eventExecutor);
        // 注册到领域层静态发布器，以保持框架默认行为
        DomainEventPublisher.setEventPublisher(publisher);
        return publisher;
    }

    /**
     * 应用事件发布器
     */
    @Bean
    @ConditionalOnMissingBean(IApplicationEventPublisher.class)
    public IApplicationEventPublisher applicationEventPublisher(ApplicationEventPublisher applicationEventPublisher, @Qualifier("applicationEventExecutor") Executor eventExecutor) {
        IApplicationEventPublisher publisher = new SpringApplicationEventPublisher(applicationEventPublisher, eventExecutor);
        // 注册到应用层静态发布器，以保持框架默认行为
        io.github.anthem37.easy.ddd.application.event.ApplicationEventPublisher.setEventPublisher(publisher);
        return publisher;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        IDomainEventPublisher eventPublisher = DomainEventPublisher.getEventPublisher();
        if (eventPublisher == null) {
            log.warn("DomainEventPublisher.EventPublisher未设置, 领域事件将不会被发布");
        }
        IApplicationEventPublisher applicationEventPublisher = io.github.anthem37.easy.ddd.application.event.ApplicationEventPublisher.getEventPublisher();
        if (applicationEventPublisher == null) {
            log.warn("ApplicationEventPublisher.EventPublisher未设置, 应用事件将不会被发布");
        }
    }
}
