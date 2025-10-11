package io.github.anthem37.easy.ddd.common.exception;

import java.io.Serial;

/**
 * 编排异常
 * 用于表示业务编排过程中的错误
 *
 * @author CodeBuddy
 * @date 2025/8/15
 */
public class BizFlowException extends BusinessException {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 构造函数
     *
     * @param message 异常消息
     */
    public BizFlowException(String message) {
        super("ORCHESTRATION_ERROR", message);
    }

    /**
     * 构造函数
     *
     * @param message 异常消息
     * @param cause   原因异常
     */
    public BizFlowException(String message, Throwable cause) {
        super("ORCHESTRATION_ERROR", message, cause);
    }

    /**
     * 构造函数
     *
     * @param errorCode 错误码
     * @param message   异常消息
     */
    public BizFlowException(String errorCode, String message) {
        super(errorCode, message);
    }
}