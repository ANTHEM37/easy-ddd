package io.github.anthem37.easy.ddd.infrastructure.config;

import io.github.anthem37.easy.ddd.common.cqrs.command.ICommandBus;
import io.github.anthem37.easy.ddd.common.cqrs.query.IQueryBus;
import io.github.anthem37.easy.ddd.domain.event.DomainEventPublisher;
import io.github.anthem37.easy.ddd.infrastructure.bus.impl.CommandBus;
import io.github.anthem37.easy.ddd.infrastructure.bus.impl.QueryBus;
import io.github.anthem37.easy.ddd.infrastructure.event.SpringDomainEventPublisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.util.concurrent.Executor;

/**
 * DDD框架自动配置类
 * 提供框架核心组件的自动装配
 *
 * @author anthem37
 * @date 2025/8/14 13:47:56
 */
@Slf4j
@Configuration
@EnableAsync
@EnableAspectJAutoProxy
@EnableTransactionManagement
@ComponentScan(basePackages = {"io.github.anthem37.easy.ddd"})
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
    @ConditionalOnMissingBean(DomainEventPublisher.EventPublisher.class)
    public DomainEventPublisher.EventPublisher domainEventPublisher(ApplicationEventPublisher applicationEventPublisher, @Qualifier("eventExecutor") Executor eventExecutor) {
        DomainEventPublisher.EventPublisher publisher = new SpringDomainEventPublisher(applicationEventPublisher, eventExecutor);
        // 注册到领域层静态发布器，以保持框架默认行为
        DomainEventPublisher.setEventPublisher(publisher);
        return publisher;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        DomainEventPublisher.EventPublisher eventPublisher = DomainEventPublisher.getEventPublisher();
        if (eventPublisher == null) {
            log.warn("DomainEventPublisher.EventPublisher未设置, 领域事件将不会被发布");
        }
    }
}
