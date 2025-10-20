package io.github.anthem37.easy.ddd.application.event;

import io.github.anthem37.easy.ddd.common.event.IEventHandler;

/**
 * 应用事件处理器接口
 *
 * @param <T> 事件类型
 * @author anthem37
 * @since 2025/8/14 10:12:36
 */
public interface IApplicationEventHandler<T extends IApplicationEvent> extends IEventHandler<T> {

}