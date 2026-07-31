---
description: Java代码风格约束。
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

### 链式调用

- 调用方法数量 ≤ 2 个：方法调用书写在同一行。
- 调用方法数量 > 2 个：每个方法单独占用一行，按 `.` 对齐：

```java
return Normalizer.normalize(text.trim(), Normalizer.Form.NFKC)
                 .replaceAll("\\s+", " ")
                 .toLowerCase();
```

- 链式调用展开后单行字符长度超过 100 时，书写风格改为每步独立一行，参数与 lambda 逐层展开：

```java
List<RoleResourceDO> list = resourceIds
        .stream()
        .map(resourceId -> RoleResourceDO.builder()
                .roleId(roleId)
                .resourceId(resourceId)
                .build())
        .toList();
```

### 方法调用参数换行

进行方法调用时，如果方法参数过多，单行字符数量超过 110，使用以下书写风格：每个参数单独占一行，右括号 `)` 单独占一行。

```java
AssertUtils.isTrue(
        values.add(normalize(value.getValue())),
        BizErrorCode.SPEC_VALUE_INVALID
);
```

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

## 查询与转换分离

Service 层中，数据库查询（mapper 调用）与对象转换（converter 调用）必须分步书写，禁止嵌套在同一个表达式中。
查询结果先落入具名变量，再做转换，便于阅读、断点调试与结果复用。

批量转换必须调用 converter 提供的 `toXxxList` 方法，禁止在 Service 层手写 `stream().map(converter::toXxx)`。

| 禁止 | 必须 |
|------|------|
| `return brandConverter.toRespList(brandMapper.selectList(...));` | `List<BrandDO> brands = brandMapper.selectList(...); return brandConverter.toRespList(brands);` |
| `resp.setItems(orderItemMapper.selectList(...).stream().map(...).toList());` | `List<OrderItemDO> items = orderItemMapper.selectList(...); resp.setItems(orderItemConverter.toItemRespList(items));` |
| `resp.setItems(items.stream().map(orderItemConverter::toItemResp).toList());` | `resp.setItems(orderItemConverter.toItemRespList(items));` |

```java
// 禁止：查询与转换揉在一个表达式里
return brandConverter.toRespList(
        brandMapper.selectList(
                Wrappers.<BrandDO>lambdaQuery()
                        .in(BrandDO::getId, ids)
        )
);

// 必须：先查询，后转换
List<BrandDO> brands = brandMapper.selectList(
        Wrappers.<BrandDO>lambdaQuery()
                .in(BrandDO::getId, ids)
);
return brandConverter.toRespList(brands);
```

```java
// 禁止：Service 层手写 stream 映射
resp.setItems(items.stream()
                   .map(orderItemConverter::toItemResp)
                   .toList());

// 必须：使用 converter 批量方法（converter 缺失时先补充）
resp.setItems(orderItemConverter.toItemRespList(items));
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
