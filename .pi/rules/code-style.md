---
description: Java 代码风格约束，适用于所有 Java 源文件。
paths: "**/*.java"
---

## 命名

| 元素 | 约定 | 示例 |
|------|------|------|
| 类/接口 | PascalCase | `UserService` |
| 业务枚举（DB 字段有限值集合） | `*Enum`（以 Enum 后缀结尾） | `StatusEnum`、`LimitEnum` |
| 方法/变量 | camelCase | `getUserById` |
| 常量 | UPPER_SNAKE_CASE | `MAX_RETRY` |
| Service 接口 | `I*Service` | `IAuthService` |
| Service 实现 | `*Service`（去 I） | `AuthService` |
| 分页查询方法 | `page*` | `pageUserInfos` |

## Controller

- Controller 方法命名遵守与 Service 相同的规范（分页方法使用 `page*`）
- Controller 方法参数数量超过 **3 个**时，必须封装为对应的 `xxxReq` 对象，禁止逐一声明 `@RequestParam`

## Import

顺序：JDK → 第三方（字母序）→ 项目内（字母序）。组间空行分隔。禁止 `import *`。

## 格式

- 4 空格缩进，K&R 大括号风格（左大括号不换行）
- 链式调用 > 2 个：换行并对齐 `.`
- 流式调用：每步独立一行，按 `.` 对齐

### 方法声明换行

当方法声明（修饰符 + 返回值 + 方法名 + 参数列表 + 左大括号）超过 100 字符时：

1. 每个参数单独占一行，缩进 8 空格（即 2 层缩进）；
2. 右括号 `)` 单独占一行，左大括号 `{` 紧跟右括号后不换行。

```java
public Long createResource(
        String type,
        String code,
        String name,
        Long parentId,
        Integer sort,
        boolean enabled
) {
    // ...
}
```

## 集合判空

使用 `CollUtil.isEmpty()` / `CollUtil.isNotEmpty()`（Hutool）。禁止手写 `== null \|\| .isEmpty()`。

## 实体/DTO

注解：`@Data @Builder @NoArgsConstructor @AllArgsConstructor`。使用 builder 构建。禁止 `new Xxx()` 后连续 setter。

## Javadoc

每个方法：摘要 + `@param` + `@return` + `@throws`。

## 注释

注释内容使用文字描述，禁止使用数字序号（`1.`、`2.`、`3.` 等）标记步骤或条目。

### 禁止分隔线注释

禁止使用 `=` 或 `-` 填充的分隔线注释（如 `// ===== 认证 =====`、`// ---------- 内部方法 ----------`）。
方法之间应使用 Javadoc 或自然空行分隔，类内部不同职责区应通过提取私有方法或拆分小类来表达。

| 禁止 | 必须 |
|------|------|
| `// ===== 认证 =====` | Javadoc 描述职责 |
| `// ---------- 内部方法 ----------` | 提取为私有方法 |

## MyBatis-Plus

使用 `Wrappers.lambdaQuery()` / `Wrappers.lambdaUpdate()`。禁止 `new LambdaQueryWrapper<>()`。

### 条件值使用枚举

对于 `status`、`enable` 等数据库字段，构造 Wrapper 条件时必须使用对应的业务枚举（命名遵循 `*Enum` 后缀），禁止使用魔数或布尔值：

| 禁止 | 必须 |
|------|------|
| `.eq(ResourceDO::getEnable, 1)` | `.eq(ResourceDO::getEnable, StatusEnum.ENABLED)` |
| `.eq(ResourceDO::getEnable, true)` | `.eq(ResourceDO::getEnable, StatusEnum.ENABLED)` |
| `.eq(ResourceDO::getStatus, false)` | `.eq(ResourceDO::getStatus, StatusEnum.DISABLED)` |
| `.eq(ResourceDO::getStatus, 0)` | `.eq(ResourceDO::getStatus, StatusEnum.DISABLED)` |

枚举定义位于 `@starters/cloudmall-mybatis-plus-spring-boot-starter/.../enums/StatusEnum.java`：

```java
DISABLED(0, "关闭"),
ENABLED(1, "开启");
```

## 工具方法

### 三元表达式简化

优先使用 Hutool 工具类替代手写三元表达式：

| 场景 | 禁止 | 必须 |
|------|------|------|
| null 默认值 | `x != null ? x : defaultValue` | `ObjectUtil.defaultIfNull(x, defaultValue)` |
| Boolean 判 true | `x != null && x` | `BooleanUtil.isTrue(x)` |
| Boolean 判 false | `x == null \|\| !x` | `BooleanUtil.isFalse(x)` |
| 字符串判空 | `s == null \|\| s.isEmpty()` | `StrUtil.isBlank(s)` |

### 枚举查找

使用 `Arrays.stream().filter().findFirst().orElseThrow()`。禁止 for 循环。code 为 null 时抛 `IllegalArgumentException`。

### Bean 属性拷贝

将源对象非 null 属性拷贝到目标对象时，使用 `BeanUtil.copyProperties(source, target, CopyOptions.create().setIgnoreNullValue(true))`。禁止逐字段手写 `if (xx != null) { target.setXx(source.getXx()); }`。

| 禁止 | 必须 |
|------|------|
| 逐字段 `if (x != null) setX()` | `BeanUtil.copyProperties(src, tgt, CopyOptions.create().setIgnoreNullValue(true))` |
