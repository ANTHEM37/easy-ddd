package io.github.anthem37.easy.ddd.common.flow;

import io.github.anthem37.easy.ddd.common.assertion.Assert;
import io.github.anthem37.easy.ddd.common.cqrs.command.ICommand;
import io.github.anthem37.easy.ddd.common.cqrs.command.ICommandBus;
import io.github.anthem37.easy.ddd.common.cqrs.query.IQuery;
import io.github.anthem37.easy.ddd.common.cqrs.query.IQueryBus;
import io.github.anthem37.easy.ddd.common.exception.OrchestrationException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 业务编排框架核心类
 *
 * <p>提供完整的业务流程编排功能，支持：</p>
 * <ul>
 *   <li>Command节点：执行业务命令操作</li>
 *   <li>Query节点：执行查询操作</li>
 *   <li>Condition节点：条件判断和流程控制</li>
 *   <li>Generic节点：自定义逻辑执行</li>
 *   <li>流程连接：定义节点间的执行顺序</li>
 *   <li>PlantUML导出：可视化流程图</li>
 * </ul>
 *
 * @author anthem37
 * @date 2025/8/15 15:01:27
 */
@RequiredArgsConstructor
public class BizFlow {

    @Getter
    private final String id;

    @Getter
    private final String name;

    private final List<GenericNode> nodes = new ArrayList<>();
    private final List<Connection> connections = new ArrayList<>();
    private final ICommandBus commandBus;
    private final IQueryBus queryBus;

    // ========== 流程构建方法 ==========

    /**
     * 添加命令节点（静态方式）
     *
     * @param nodeId   节点唯一标识
     * @param nodeName 节点显示名称
     * @param command  要执行的命令对象
     * @return 当前编排实例，支持链式调用
     */
    public BizFlow addCommand(String nodeId, String nodeName, ICommand<?> command) {
        validateNode(nodeId, nodeName);
        Assert.orchestrationNotNull(command, "命令不能为空");

        // 创建执行器：直接发送预定义的命令
        Function<Context, Object> executor = ctx -> commandBus.send(command);
        nodes.add(new GenericNode(nodeId, nodeName, "<<command>>", executor));
        return this;
    }

    /**
     * 添加命令节点（动态方式）
     *
     * @param nodeId         节点唯一标识
     * @param nodeName       节点显示名称
     * @param commandBuilder 根据上下文动态构建命令的函数
     * @return 当前编排实例，支持链式调用
     */
    public BizFlow addCommand(String nodeId, String nodeName, Function<Context, ICommand<?>> commandBuilder) {
        validateNode(nodeId, nodeName);
        Assert.orchestrationNotNull(commandBuilder, "命令构建函数不能为空");

        // 创建执行器：运行时根据上下文构建命令并发送
        Function<Context, Object> executor = ctx -> {
            ICommand<?> command = commandBuilder.apply(ctx);
            Assert.orchestrationNotNull(command, "构建的命令不能为空");
            return commandBus.send(command);
        };
        nodes.add(new GenericNode(nodeId, nodeName, "<<command>>", executor));
        return this;
    }

    /**
     * 添加查询节点（静态方式）
     *
     * @param nodeId   节点唯一标识
     * @param nodeName 节点显示名称
     * @param query    要执行的查询对象
     * @return 当前编排实例，支持链式调用
     */
    public BizFlow addQuery(String nodeId, String nodeName, IQuery<?> query) {
        validateNode(nodeId, nodeName);
        Assert.orchestrationNotNull(query, "查询不能为空");

        // 创建执行器：直接发送预定义的查询
        Function<Context, Object> executor = ctx -> queryBus.send(query);
        nodes.add(new GenericNode(nodeId, nodeName, "<<query>>", executor));
        return this;
    }

    /**
     * 添加查询节点（动态方式）
     *
     * @param nodeId       节点唯一标识
     * @param nodeName     节点显示名称
     * @param queryBuilder 根据上下文动态构建查询的函数
     * @return 当前编排实例，支持链式调用
     */
    public BizFlow addQuery(String nodeId, String nodeName, Function<Context, IQuery<?>> queryBuilder) {
        validateNode(nodeId, nodeName);
        Assert.orchestrationNotNull(queryBuilder, "查询构建函数不能为空");

        // 创建执行器：运行时根据上下文构建查询并发送
        Function<Context, Object> executor = ctx -> {
            IQuery<?> query = queryBuilder.apply(ctx);
            Assert.orchestrationNotNull(query, "构建的查询不能为空");
            return queryBus.send(query);
        };
        nodes.add(new GenericNode(nodeId, nodeName, "<<query>>", executor));
        return this;
    }

    /**
     * 添加条件节点（函数式）
     *
     * @param nodeId    节点唯一标识
     * @param nodeName  节点显示名称
     * @param condition 条件判断函数，返回 Boolean 值
     * @return 当前编排实例，支持链式调用
     */
    public BizFlow addCondition(String nodeId, String nodeName, Function<Context, Boolean> condition) {
        validateNode(nodeId, nodeName);
        Assert.orchestrationNotNull(condition, "条件函数不能为空");

        // 创建执行器：执行条件判断逻辑
        Function<Context, Object> executor = condition::apply;
        nodes.add(new GenericNode(nodeId, nodeName, "<<choice>>", executor));
        return this;
    }

    /**
     * 添加条件节点（变量比较）
     *
     * @param nodeId        节点唯一标识
     * @param nodeName      节点显示名称
     * @param variableName  要比较的变量名
     * @param expectedValue 期望的变量值
     * @return 当前编排实例，支持链式调用
     */
    public BizFlow addCondition(String nodeId, String nodeName, String variableName, Object expectedValue) {
        validateNode(nodeId, nodeName);
        Assert.orchestrationHasText(variableName, "变量名不能为空");

        // 创建执行器：比较上下文变量与期望值
        Function<Context, Object> executor = ctx -> {
            Object actualValue = ctx.getVariable(variableName, Object.class);
            return Objects.equals(actualValue, expectedValue);
        };
        nodes.add(new GenericNode(nodeId, nodeName, "<<choice>>", executor));
        return this;
    }

    /**
     * 添加条件节点（结果比较）
     *
     * @param nodeId         节点唯一标识
     * @param nodeName       节点显示名称
     * @param sourceNodeId   源节点ID，其执行结果用于比较
     * @param expectedResult 期望的结果值
     * @return 当前编排实例，支持链式调用
     */
    public BizFlow addResultCondition(String nodeId, String nodeName, String sourceNodeId, Object expectedResult) {
        validateNode(nodeId, nodeName);
        Assert.orchestrationHasText(sourceNodeId, "源节点ID不能为空");

        // 创建执行器：比较指定节点的执行结果与期望值
        Function<Context, Object> executor = ctx -> {
            Object actualResult = ctx.getResult(sourceNodeId, Object.class);
            return Objects.equals(actualResult, expectedResult);
        };
        nodes.add(new GenericNode(nodeId, nodeName, "<<choice>>", executor));
        return this;
    }

    /**
     * 添加通用节点
     *
     * @param nodeId   节点唯一标识
     * @param nodeName 节点显示名称
     * @param executor 自定义执行逻辑
     * @return 当前编排实例，支持链式调用
     */
    public BizFlow addGeneric(String nodeId, String nodeName, Function<Context, Object> executor) {
        validateNode(nodeId, nodeName);
        Assert.orchestrationNotNull(executor, "执行函数不能为空");

        nodes.add(new GenericNode(nodeId, nodeName, "<<generic>>", executor));
        return this;
    }

    /**
     * 连接两个节点（无条件连接）
     *
     * @param from 源节点ID
     * @param to   目标节点ID
     * @return 当前编排实例，支持链式调用
     */
    public BizFlow connect(String from, String to) {
        validateConnect(from, to);
        connections.add(new Connection(from, to, null, null));
        return this;
    }

    /**
     * 条件连接两个节点
     *
     * @param from           源节点ID
     * @param to             目标节点ID
     * @param conditionName  条件名称（用于PlantUML显示）
     * @param conditionCheck 条件检查函数
     * @return 当前编排实例，支持链式调用
     */
    public BizFlow connectWhen(String from, String to, String conditionName, Function<Context, Boolean> conditionCheck) {
        validateConnect(from, to);
        Assert.orchestrationHasText(conditionName, "条件名称不能为空");
        Assert.orchestrationNotNull(conditionCheck, "条件检查函数不能为空");

        connections.add(new Connection(from, to, conditionName, conditionCheck));
        return this;
    }

    /**
     * 当源节点返回 true 时连接到目标节点
     *
     * @param from 源节点ID（通常是条件节点）
     * @param to   目标节点ID
     * @return 当前编排实例，支持链式调用
     */
    public BizFlow connectWhenTrue(String from, String to) {
        validateConnect(from, to);
        // 创建条件：检查源节点结果是否为 true
        Function<Context, Boolean> condition = ctx -> {
            try {
                Object result = ctx.getResult(from, Object.class);
                return Boolean.TRUE.equals(result);
            } catch (Exception e) {
                return false; // 异常时返回 false
            }
        };
        connections.add(new Connection(from, to, "true", condition));
        return this;
    }

    /**
     * 当源节点返回 false 时连接到目标节点
     *
     * @param from 源节点ID（通常是条件节点）
     * @param to   目标节点ID
     * @return 当前编排实例，支持链式调用
     */
    public BizFlow connectWhenFalse(String from, String to) {
        validateConnect(from, to);
        // 创建条件：检查源节点结果是否不为 true
        Function<Context, Boolean> condition = ctx -> {
            try {
                Object result = ctx.getResult(from, Object.class);
                return !Boolean.TRUE.equals(result);
            } catch (Exception e) {
                return true; // 异常时走默认路径
            }
        };
        connections.add(new Connection(from, to, "false", condition));
        return this;
    }

    // ========== 流程执行方法 ==========

    public Result execute() {
        return execute(new Context(id));
    }

    public Result execute(Context context) {
        LocalDateTime startTime = LocalDateTime.now();

        try {
            validate();
            runNodes(context);
            return Result.success(id, startTime, LocalDateTime.now(), context.getAllResults());
        } catch (Exception e) {
            return Result.failure(id, e.getMessage(), startTime, LocalDateTime.now());
        }
    }

    /**
     * 简化的节点执行逻辑
     */
    private void runNodes(Context context) {
        Map<String, GenericNode> nodeMap = nodes.stream().collect(Collectors.toMap(GenericNode::getId, n -> n));
        Set<String> executed = new HashSet<>();
        Queue<String> toExecute = new LinkedList<>();

        // 找到入口节点
        Set<String> targets = connections.stream().map(c -> c.to).collect(Collectors.toSet());
        nodes.stream().filter(node -> !targets.contains(node.getId())).forEach(node -> toExecute.offer(node.getId()));

        Assert.orchestrationIsFalse(toExecute.isEmpty() && !nodes.isEmpty(), "没有找到入口节点");

        // 执行节点 - 简化的循环检测：执行次数不能超过节点总数的2倍
        int executionCount = 0;
        int maxExecutions = nodes.size() * 2;
        while (!toExecute.isEmpty()) {
            Assert.orchestrationIsFalse(++executionCount > maxExecutions, "检测到循环依赖");

            String nodeId = toExecute.poll();
            if (executed.contains(nodeId)) {
                continue;
            }

            GenericNode node = nodeMap.get(nodeId);
            Assert.orchestrationNotNull(node, "节点不存在: " + nodeId);

            try {
                Object result = node.execute(context);
                context.setResult(nodeId, result);
                executed.add(nodeId);

                // 添加后续节点
                connections.stream().filter(conn -> conn.from.equals(nodeId) && conn.canExecute(context)).filter(conn -> !executed.contains(conn.to)).forEach(conn -> toExecute.offer(conn.to));

            } catch (Exception e) {
                Assert.orchestrationFail("节点执行失败: " + nodeId + ", 错误: " + e.getMessage());
            }
        }
    }

    // ========== PlantUML导出 ==========

    public String toPlantUML() {
        StringBuilder uml = new StringBuilder();
        uml.append("@startuml\n");
        uml.append("!theme plain\n");
        uml.append("title ").append(name).append("\n\n");

        // 节点定义
        for (GenericNode node : nodes) {
            uml.append("state \"").append(node.getName()).append("\" as ").append(node.getId()).append(" ").append(node.getShape()).append("\n");
        }

        uml.append("\n");

        // 连接关系
        for (Connection conn : connections) {
            uml.append(conn.from).append(" --> ").append(conn.to);
            if (conn.condition != null) {
                uml.append(" : ").append(conn.condition);
            }
            uml.append("\n");
        }

        uml.append("\n@enduml");
        return uml.toString();
    }

    // ========== 私有辅助方法 ==========

    private void validateNode(String nodeId, String nodeName) {
        Assert.orchestrationHasText(nodeId, "节点ID不能为空");
        Assert.orchestrationHasText(nodeName, "节点名称不能为空");
        Assert.orchestrationIsFalse(nodeExists(nodeId), "节点ID已存在: " + nodeId);
    }

    private boolean nodeExists(String nodeId) {
        return nodes.stream().anyMatch(node -> node.getId().equals(nodeId));
    }

    private void validateConnect(String from, String to) {
        Assert.orchestrationHasText(from, "源节点ID不能为空");
        Assert.orchestrationHasText(to, "目标节点ID不能为空");
        Assert.orchestrationIsFalse(from.equals(to), "不能连接节点到自身");
    }

    private void validate() {
        Assert.orchestrationIsFalse(nodes.isEmpty(), "编排中没有定义任何节点");

        Set<String> nodeIds = nodes.stream().map(GenericNode::getId).collect(Collectors.toSet());
        for (Connection conn : connections) {
            Assert.orchestrationIsTrue(nodeIds.contains(conn.from), "连接中的源节点不存在: " + conn.from);
            Assert.orchestrationIsTrue(nodeIds.contains(conn.to), "连接中的目标节点不存在: " + conn.to);
        }
    }

    // ========== 内部类定义 ==========

    /**
     * 编排执行上下文
     *
     * <p>用于在节点执行过程中传递数据，包括：</p>
     * <ul>
     *   <li>变量：外部传入或节点间共享的数据</li>
     *   <li>结果：各节点的执行结果</li>
     * </ul>
     *
     * <p>线程安全：使用 ConcurrentHashMap 确保多线程环境下的安全性</p>
     */
    @RequiredArgsConstructor
    @Getter
    public static class Context {
        /**
         * 编排实例ID
         */
        private final String orchestrationId;

        /**
         * 变量存储：用于存储外部传入或节点间共享的变量
         */
        private final Map<String, Object> variables = new ConcurrentHashMap<>();

        /**
         * 结果存储：用于存储各节点的执行结果
         */
        private final Map<String, Object> results = new ConcurrentHashMap<>();

        /**
         * 设置变量
         *
         * @param key   变量名
         * @param value 变量值
         */
        public void setVariable(String key, Object value) {
            variables.put(key, value);
        }

        /**
         * 获取变量（类型安全）
         *
         * @param key  变量名
         * @param type 期望的变量类型
         * @param <T>  泛型类型
         * @return 变量值，如果不存在则返回 null
         */
        public <T> T getVariable(String key, Class<T> type) {
            return type.cast(variables.get(key));
        }

        /**
         * 设置节点执行结果
         *
         * @param nodeId 节点ID
         * @param result 执行结果
         */
        public void setResult(String nodeId, Object result) {
            results.put(nodeId, result);
        }

        /**
         * 获取节点执行结果（类型安全）
         *
         * @param nodeId 节点ID
         * @param type   期望的结果类型
         * @param <T>    泛型类型
         * @return 执行结果，如果不存在则返回 null
         * @throws OrchestrationException 当类型不匹配时
         */
        public <T> T getResult(String nodeId, Class<T> type) {
            Object result = results.get(nodeId);
            if (result == null) {
                return null;
            }
            Assert.orchestrationIsTrue(type.isInstance(result), "结果类型不匹配: " + nodeId + ", 期望: " + type.getSimpleName() + ", 实际: " + result.getClass().getSimpleName());
            return type.cast(result);
        }

        /**
         * 获取所有节点的执行结果
         *
         * @return 所有执行结果的副本
         */
        public Map<String, Object> getAllResults() {
            return new HashMap<>(results);
        }
    }

    /**
     * 编排执行结果
     *
     * <p>包含执行状态、时间信息和结果数据</p>
     */
    @RequiredArgsConstructor(access = lombok.AccessLevel.PRIVATE)
    @Getter
    public static class Result {
        /**
         * 编排实例ID
         */
        private final String orchestrationId;

        /**
         * 执行是否成功
         */
        private final boolean success;

        /**
         * 错误信息（仅在失败时有值）
         */
        private final String errorMessage;

        /**
         * 执行开始时间
         */
        private final LocalDateTime startTime;

        /**
         * 执行结束时间
         */
        private final LocalDateTime endTime;

        /**
         * 所有节点的执行结果（仅在成功时有值）
         */
        private final Map<String, Object> results;

        /**
         * 创建成功结果
         *
         * @param id      编排ID
         * @param start   开始时间
         * @param end     结束时间
         * @param results 执行结果
         * @return 成功结果实例
         */
        public static Result success(String id, LocalDateTime start, LocalDateTime end, Map<String, Object> results) {
            return new Result(id, true, null, start, end, results);
        }

        /**
         * 创建失败结果
         *
         * @param id    编排ID
         * @param error 错误信息
         * @param start 开始时间
         * @param end   结束时间
         * @return 失败结果实例
         */
        public static Result failure(String id, String error, LocalDateTime start, LocalDateTime end) {
            return new Result(id, false, error, start, end, null);
        }

        /**
         * 获取执行耗时（毫秒）
         *
         * @return 执行耗时
         */
        public long getExecutionTimeMillis() {
            return java.time.Duration.between(startTime, endTime).toMillis();
        }
    }

    /**
     * 节点连接关系
     *
     * <p>定义节点间的执行顺序和条件</p>
     */
    @RequiredArgsConstructor
    private static class Connection {
        /**
         * 源节点ID
         */
        final String from;

        /**
         * 目标节点ID
         */
        final String to;

        /**
         * 条件名称（用于PlantUML显示）
         */
        final String condition;

        /**
         * 条件检查函数
         */
        final Function<Context, Boolean> conditionCheck;

        /**
         * 判断是否应该执行此连接
         *
         * @param context 执行上下文
         * @return 如果应该执行返回 true，否则返回 false
         */
        boolean canExecute(Context context) {
            return conditionCheck == null || conditionCheck.apply(context);
        }
    }

    /**
     * 通用节点实现
     *
     * <p>统一所有节点类型的实现，通过不同的执行函数来区分行为：</p>
     * <ul>
     *   <li>Command节点：执行业务命令</li>
     *   <li>Query节点：执行查询操作</li>
     *   <li>Condition节点：执行条件判断</li>
     *   <li>Generic节点：执行自定义逻辑</li>
     * </ul>
     */
    @RequiredArgsConstructor
    @Getter
    private static class GenericNode {
        /**
         * 节点唯一标识
         */
        private final String id;

        /**
         * 节点显示名称
         */
        private final String name;

        /**
         * 节点形状标识（用于PlantUML）
         */
        private final String shape;

        /**
         * 节点执行逻辑
         */
        private final Function<Context, Object> executor;

        /**
         * 执行节点逻辑
         *
         * @param context 执行上下文
         * @return 执行结果
         */
        Object execute(Context context) {
            return executor.apply(context);
        }
    }
}