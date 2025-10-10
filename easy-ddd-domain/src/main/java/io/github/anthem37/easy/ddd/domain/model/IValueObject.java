package io.github.anthem37.easy.ddd.domain.model;

import java.io.Serializable;

/**
 * 值对象标记接口
 * 值对象特征：
 * 1. 无唯一标识
 * 2. 不可变性
 * 3. 值相等性
 * 4. 可替换性
 * 5. 自验证
 *
 * @author anthem37
 * @date 2025/8/14 13:28:45
 */
public interface IValueObject extends Serializable {

    /**
     * 值对象必须实现equals方法
     * 基于所有属性值进行相等性比较
     */
    @Override
    boolean equals(Object obj);

    /**
     * 值对象必须实现hashCode方法
     * 基于所有属性值计算哈希码
     */
    @Override
    int hashCode();
}