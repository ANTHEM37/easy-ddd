package io.github.anthem37.easy.ddd.common.assertion;


import io.github.anthem37.easy.ddd.common.exception.BizFlowException;
import io.github.anthem37.easy.ddd.common.exception.BusinessException;

import java.util.Collection;
import java.util.function.Supplier;

/**
 * 断言工具类
 * 用于统一管理业务异常的抛出
 *
 * @author anthem37
 * @since 2025/8/13 16:58:42
 */
public final class Assert {

    private Assert() {
        // 工具类，禁止实例化
    }

    /**
     * 断言表达式为真，否则抛出业务异常
     *
     * @param expression 表达式
     * @param message    异常消息
     * @throws BusinessException 当表达式为false时
     */
    public static void isTrue(boolean expression, String message) {
        if (!expression) {
            throw new BusinessException(message);
        }
    }

    /**
     * 断言表达式为真，否则抛出业务异常
     *
     * @param expression      表达式
     * @param messageSupplier 异常消息提供者
     * @throws BusinessException 当表达式为false时
     */
    public static void isTrue(boolean expression, Supplier<String> messageSupplier) {
        if (!expression) {
            throw new BusinessException(messageSupplier.get());
        }
    }

    /**
     * 断言表达式为假，否则抛出业务异常
     *
     * @param expression 表达式
     * @param message    异常消息
     * @throws BusinessException 当表达式为true时
     */
    public static void isFalse(boolean expression, String message) {
        if (expression) {
            throw new BusinessException(message);
        }
    }

    /**
     * 断言对象不为null，否则抛出业务异常
     *
     * @param object  对象
     * @param message 异常消息
     * @throws BusinessException 当对象为null时
     */
    public static void notNull(Object object, String message) {
        if (object == null) {
            throw new BusinessException(message);
        }
    }

    /**
     * 断言对象为null，否则抛出业务异常
     *
     * @param object  对象
     * @param message 异常消息
     * @throws BusinessException 当对象不为null时
     */
    public static void isNull(Object object, String message) {
        if (object != null) {
            throw new BusinessException(message);
        }
    }

    /**
     * 断言字符串不为空，否则抛出业务异常
     *
     * @param text    字符串
     * @param message 异常消息
     * @throws BusinessException 当字符串为null或空时
     */
    public static void hasText(String text, String message) {
        if (text == null || text.trim().isEmpty()) {
            throw new BusinessException(message);
        }
    }

    /**
     * 断言字符串不为空且长度在指定范围内
     *
     * @param text      字符串
     * @param minLength 最小长度
     * @param maxLength 最大长度
     * @param message   异常消息
     * @throws BusinessException 当字符串不符合要求时
     */
    public static void hasLength(String text, int minLength, int maxLength, String message) {
        hasText(text, message);
        int length = text.trim().length();
        if (length < minLength || length > maxLength) {
            throw new BusinessException(message);
        }
    }

    /**
     * 断言集合不为空，否则抛出业务异常
     *
     * @param collection 集合
     * @param message    异常消息
     * @throws BusinessException 当集合为null或空时
     */
    public static void notEmpty(Collection<?> collection, String message) {
        if (collection == null || collection.isEmpty()) {
            throw new BusinessException(message);
        }
    }

    /**
     * 断言数组不为空，否则抛出业务异常
     *
     * @param array   数组
     * @param message 异常消息
     * @throws BusinessException 当数组为null或空时
     */
    public static void notEmpty(Object[] array, String message) {
        if (array == null || array.length == 0) {
            throw new BusinessException(message);
        }
    }

    // ========== 编排专用断言方法 ==========

    /**
     * 编排断言：字符串不为空
     */
    public static void orchestrationHasText(String text, String message) {
        if (text == null || text.trim().isEmpty()) {
            throw new BizFlowException(message);
        }
    }

    /**
     * 编排断言：对象不为null
     */
    public static void orchestrationNotNull(Object object, String message) {
        if (object == null) {
            throw new BizFlowException(message);
        }
    }

    /**
     * 编排断言：表达式为真
     */
    public static void orchestrationIsTrue(boolean expression, String message) {
        if (!expression) {
            throw new BizFlowException(message);
        }
    }

    /**
     * 编排断言：表达式为假
     */
    public static void orchestrationIsFalse(boolean expression, String message) {
        if (expression) {
            throw new BizFlowException(message);
        }
    }

    /**
     * 编排断言：直接失败
     */
    public static void orchestrationFail(String message) {
        throw new BizFlowException(message);
    }

    // ========== 通用断言增强方法 ==========

    /**
     * 断言字符串匹配正则表达式
     */
    public static void matches(String text, String regex, String message) {
        hasText(text, message);
        if (!text.matches(regex)) {
            throw new BusinessException(message);
        }
    }

    /**
     * 断言对象类型正确
     */
    public static void isInstanceOf(Object object, Class<?> expectedType, String message) {
        notNull(object, message);
        if (!expectedType.isInstance(object)) {
            throw new BusinessException(message);
        }
    }

    /**
     * 断言两个对象相等
     */
    public static void equals(Object expected, Object actual, String message) {
        if (!java.util.Objects.equals(expected, actual)) {
            throw new BusinessException(message);
        }
    }

    /**
     * 断言数值为非负数，否则抛出业务异常
     *
     * @param number  数值
     * @param message 异常消息
     * @throws BusinessException 当数值小于0时
     */
    public static void isNotNegative(Number number, String message) {
        notNull(number, message);
        if (number.doubleValue() < 0) {
            throw new BusinessException(message);
        }
    }

    /**
     * 断言数值在指定范围内，否则抛出业务异常
     *
     * @param number  数值
     * @param min     最小值（包含）
     * @param max     最大值（包含）
     * @param message 异常消息
     * @throws BusinessException 当数值不在范围内时
     */
    public static void inRange(Number number, Number min, Number max, String message) {
        notNull(number, message);
        notNull(min, "最小值不能为空");
        notNull(max, "最大值不能为空");

        double value = number.doubleValue();
        double minValue = min.doubleValue();
        double maxValue = max.doubleValue();

        if (value < minValue || value > maxValue) {
            throw new BusinessException(message);
        }
    }

    /**
     * 直接抛出业务异常
     *
     * @param message 异常消息
     * @throws BusinessException 业务异常
     */
    public static void fail(String message) {
        throw new BusinessException(message);
    }

    /**
     * 直接抛出业务异常
     *
     * @param messageSupplier 异常消息提供者
     * @throws BusinessException 业务异常
     */
    public static void fail(Supplier<String> messageSupplier) {
        throw new BusinessException(messageSupplier.get());
    }
}