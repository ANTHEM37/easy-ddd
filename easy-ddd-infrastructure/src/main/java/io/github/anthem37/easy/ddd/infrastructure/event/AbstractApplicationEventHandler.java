package io.github.anthem37.easy.ddd.infrastructure.event;

import io.github.anthem37.easy.ddd.application.event.IApplicationEvent;

/**
 * 应用事件处理基类
 * 处理应用事件的通用逻辑
 *
 * @author hb28301
 * @date 2025/10/21 10:20:06
 */
public abstract class AbstractApplicationEventHandler<T extends IApplicationEvent> extends AbstractEventHandler<T> {
}
