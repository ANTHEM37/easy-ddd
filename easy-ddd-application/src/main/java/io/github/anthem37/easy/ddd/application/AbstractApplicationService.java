package io.github.anthem37.easy.ddd.application;

import io.github.anthem37.easy.ddd.common.cqrs.command.ICommandBus;
import io.github.anthem37.easy.ddd.common.cqrs.query.IQueryBus;
import io.github.anthem37.easy.ddd.common.flow.BizFlow;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 应用服务抽象基类
 *
 * @author hb28301
 * @since 2025/10/11 20:28:45
 */
@Getter
@RequiredArgsConstructor
public abstract class AbstractApplicationService implements IApplicationService {

    private final ICommandBus commandBus;
    private final IQueryBus queryBus;
    private final BizFlow bizFlow;

}
