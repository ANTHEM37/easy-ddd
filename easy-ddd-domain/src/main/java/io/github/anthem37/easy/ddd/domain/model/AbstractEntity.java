package io.github.anthem37.easy.ddd.domain.model;

import io.github.anthem37.easy.ddd.common.assertion.Assert;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 * 实体基类
 * 实体具有唯一标识，可变状态，有生命周期
 *
 * @param <ID> 实体标识类型
 * @author anthem37
 * @date 2025/8/13 17:35:42
 */
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
public abstract class AbstractEntity<ID> {

    @EqualsAndHashCode.Include
    protected final ID id;

    protected AbstractEntity(ID id) {
        Assert.notNull(id, "实体ID不能为空");
        this.id = id;
    }

}
