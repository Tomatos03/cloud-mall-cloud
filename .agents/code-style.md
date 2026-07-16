# Code Style

## 命名
- 类：`PascalCase`
- 方法/变量：`camelCase`
- 常量：`UPPER_SNAKE_CASE`
- Service 接口：`I` 前缀（如 `IAuthService`）
- Service 实现：去掉 `I`（如 `AuthService`）

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

## MyBatis-Plus 查询风格
- 构建条件 wrapper 统一使用 `Wrappers.lambdaQuery()`，禁止 `new LambdaQueryWrapper<>()`
