# easy-ddd-bom

提供 Maven BOM（Bill of Materials），统一管理 easy-ddd 各模块版本，简化依赖对齐与升级。

## 核心作用

- 在父 POM 的 `dependencyManagement` 中引入 BOM 后，子模块无需逐个声明 `version`。
- 保障 `easy-ddd-*` 相关组件版本兼容性，避免依赖冲突。

## 引入方式（父 POM）

```xml

<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>io.github.anthem37</groupId>
            <artifactId>easy-ddd-bom</artifactId>
            <version>${easy.ddd.version}</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

## 子模块依赖示例

在子模块直接声明 artifact，无需写 `version`：

```xml

<dependencies>
    <dependency>
        <groupId>io.github.anthem37</groupId>
        <artifactId>easy-ddd-common</artifactId>
    </dependency>
    <dependency>
        <groupId>io.github.anthem37</groupId>
        <artifactId>easy-ddd-domain</artifactId>
    </dependency>
    <dependency>
        <groupId>io.github.anthem37</groupId>
        <artifactId>easy-ddd-application</artifactId>
    </dependency>
    <dependency>
        <groupId>io.github.anthem37</groupId>
        <artifactId>easy-ddd-infrastructure</artifactId>
    </dependency>
</dependencies>
```

## 版本管理建议

- 在顶层 `pom.xml` 定义属性 `easy.ddd.version`，统一控制 BOM 版本：

```xml

<properties>
    <easy.ddd.version>1.0.0</easy.ddd.version>
</properties>
```

- 升级时仅需调整该属性，所有子模块的 `easy-ddd-*` 依赖自动对齐。

## 关联模块

- [easy-ddd-common](../easy-ddd-common/README.md)
- [easy-ddd-domain](../easy-ddd-domain/README.md)
- [easy-ddd-application](../easy-ddd-application/README.md)
- [easy-ddd-infrastructure](../easy-ddd-infrastructure/README.md)