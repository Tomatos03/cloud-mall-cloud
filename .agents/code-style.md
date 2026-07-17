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

## 集合判空
- 使用 Hutool：`CollUtil.isEmpty(...)` / `CollUtil.isNotEmpty(...)`
- 禁止手写 `collection == null || collection.isEmpty()`

## 对象构建
- 所有实体/DTO/Req/Resp 类必须使用 `@Builder` + `@Data` + `@NoArgsConstructor` + `@AllArgsConstructor`
- 构建对象优先使用 builder 模式：`Xxx.builder().field1(v1).field2(v2).build()`
- 禁止 `new Xxx()` 后连续调用 setter 方法

## MyBatis-Plus 查询风格
- 构建条件 wrapper 统一使用 `Wrappers.lambdaQuery()`，禁止 `new LambdaQueryWrapper<>()`
