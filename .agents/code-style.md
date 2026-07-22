# Code Style

## 命名
- 类：`PascalCase`
- 方法/变量：`camelCase`
- 常量：`UPPER_SNAKE_CASE`
- Service 接口类名符合命名约束`I*Service`，例如`IAuthService`
- Service 实现类类名必须符合命名约束`*Service`，例如 `AuthService`

## Import 顺序
1. JDK
2. 第三方（按字母序）
3. 项目内包（按字母序）

要求：
- 分组之间空行
- 禁止 `*` 通配符导入

## 格式
- 4 空格缩进
- 大括号使用 K&R 风格
- 方法参数列表过长时，每个参数独占一行，参数类型和名称同行，右括号与方法名首字母对齐：
  ```java
  public GoodsResp getGoodsBySpu(
          @ToolParam(description = "商品SPU编号") String spu,
          ToolContext toolContext
  ) {
      // ...
  }
  ```

## 链式调用换行
- 占用行太长的链式调用，换行并对齐：
  ```java
  MarkdownDocumentReaderConfig config = MarkdownDocumentReaderConfig
          .builder()
          .withHorizontalRuleCreateDocument(true)
          .withIncludeCodeBlock(true)
          .build();
  ```
- 占用行长度短的链式调用，按上下文长度决定是否换行：
  ```java
  documents = documents.stream()
                       .map(doc -> doc
                               .mutate()
                               .id(UUID.randomUUID().toString())
                               .build()
                       )
                       .toList();
  ```
- 如果调用方法只有两个且占用行不是很长，写在一行
- 方法调用传参时，若参数通过链式构建且方法调用超过 2 个，应提取为局部变量后再传入：
  ```java
  List<String> documentIdList = documents.stream()
                                         .map(Document::getId)
                                         .toList();
  documentIds.addAll(documentIdList);
  ```
  若链式构建直接在 `return` 语句中作为返回值，不受此约束限制：
  ```java
  return ChatResp.builder()
                 .data(content)
                 .eventType(ChatEventType.MESSAGE)
                 .build();
  ```

## 集合判空
- 使用 Hutool：`CollectionUtil.isEmpty(...)` / `CollectionUtil.isNotEmpty(...)`
- 禁止手写 `collection == null || collection.isEmpty()`

## 对象构建
- 所有实体/DTO/Req/Resp 类必须使用 `@Builder` + `@Data` + `@NoArgsConstructor` + `@AllArgsConstructor`
- 构建对象优先使用 builder 模式：`Xxx.builder().field1(v1).field2(v2).build()`
- 禁止 `new Xxx()` 后连续调用 setter 方法

## 方法注释
- 每个方法必须添加多行 Javadoc 注释，包含：
  - 方法作用摘要（第一行）
  - `@param` 每个参数说明
  - `@return` 返回值说明（void 方法可省略）
  - `@throws` 异常说明（如有）
- Controller、Service 接口、Service 实现类均需遵守
- 私有方法同样需要注释

## MyBatis-Plus 查询风格
- 构建条件 wrapper 统一使用 `Wrappers.lambdaQuery()`，禁止 `new LambdaQueryWrapper<>()`
