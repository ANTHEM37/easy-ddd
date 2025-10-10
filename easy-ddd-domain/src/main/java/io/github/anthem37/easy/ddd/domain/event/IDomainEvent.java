package io.github.anthem37.easy.ddd.domain.event;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;

/**
 * 领域事件接口
 * 领域事件表示领域中发生的重要业务事件
 *
 * @author anthem37
 * @date 2025/8/13 20:15:42
 */
public interface IDomainEvent {

    /**
     * 获取事件ID
     */
    default String getEventId() {
        return UUID.randomUUID().toString();
    }

    /**
     * 获取事件发生时间
     */
    default LocalDateTime getOccurredOn() {
        return LocalDateTime.now();
    }

    /**
     * 获取事件版本
     */
    default int getEventVersion() {
        return 1;
    }

    /**
     * 获取事件类型
     * 用于事件路由和处理器匹配
     */
    String getEventType();

    /**
     * 获取聚合根ID
     * 标识事件来源的聚合
     */
    default Object getAggregateId() {
        return null;
    }

    /**
     * 获取事件数据
     * 用于事件序列化和传输
     */
    default Map<String, Object> getEventData() {
        return Collections.emptyMap();
    }

    /**
     * 获取事件元数据
     * 包含上下文信息，如用户ID、租户ID等
     */
    default Map<String, Object> getMetadata() {
        return Collections.emptyMap();
    }

    /**
     * 是否异步事件
     */
    default boolean isAsync() {
        return false;
    }
}
