package io.github.anthem37.easy.ddd.domain.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * 实体基类
 * 实体具有唯一标识，可变状态，有生命周期
 *
 * @param <ID> 实体标识类型
 * @author anthem37
 * @since 2025/8/13 17:35:42
 */
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Data
@ToString
@Accessors(chain = true)
public abstract class AbstractEntity<ID> {

    @EqualsAndHashCode.Include
    protected ID id;

}
