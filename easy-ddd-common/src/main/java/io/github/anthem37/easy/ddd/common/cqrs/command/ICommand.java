package io.github.anthem37.easy.ddd.common.cqrs.command;

/**
 * 命令标记接口
 * 命令用于改变系统状态
 *
 * @param <R> 命令执行结果类型
 * @author anthem37
 * @date 2025/8/14 10:23:18
 */
public interface ICommand<R> {

    /**
     * 命令验证
     * 子类可以重写此方法进行自定义验证
     *
     * @return 验证是否通过
     */
    default boolean isValid() {
        return true;
    }
}
