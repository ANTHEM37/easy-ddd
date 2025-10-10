package io.github.anthem37.easy.ddd.common.cqrs.command;

/**
 * 命令处理器接口
 *
 * @param <C> 命令类型
 * @param <R> 返回结果类型
 * @author anthem37
 * @date 2025/8/14 08:17:35
 */
public interface ICommandHandler<C extends ICommand<R>, R> {

    /**
     * 处理命令
     *
     * @param command 要处理的命令
     * @return 处理结果
     */
    R handle(C command);

    /**
     * 获取支持的命令类型
     *
     * @return 命令类型
     */
    Class<C> getSupportedCommandType();
}