package io.github.anthem37.easy.ddd.common.event;

/**
 * 触发阶段枚举
 */
public enum TriggeredPhase {
    IN_PROCESS,
    AFTER_COMMIT,
    AFTER_ROLLBACK,
}
