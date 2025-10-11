# easy-ddd-bom

提供 Maven BOM（Bill of Materials），统一管理 easy-ddd 各模块的版本，简化依赖对齐。

## 使用方式（父 POM）
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
  <!-- 其它依赖版本在 BOM 中统一声明，无需在子模块重复写 version -->
</dependencyManagement>
```

## 关联模块
- [easy-ddd-common](../easy-ddd-common/README.md)
- [easy-ddd-domain](../easy-ddd-domain/README.md)
- [easy-ddd-application](../easy-ddd-application/README.md)
- [easy-ddd-infrastructure](../easy-ddd-infrastructure/README.md)