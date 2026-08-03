---
description: Java代码风格约束。
paths: "**/*.java"
---

## 命名

| 元素 | 约定 | 示例 |
|------|------|------|
| Service 接口 | `I*Service` | `IAuthService` |
| Service 实现 | `*Service`（去 I） | `AuthService` |
| 分页查询方法 | `page*` | `pageUserInfos` |

## Controller

- Controller 方法命名遵守与 Service 相同的规范（分页方法使用 `page*`）
- Controller 方法参数数量超过 **3 个**时，必须封装为对应的 `xxxReq` 对象，禁止逐一声明 `@RequestParam`

## 集合判空

使用 `CollUtil.isEmpty()` / `CollUtil.isNotEmpty()`（Hutool）。禁止手写 `== null \|\| .isEmpty()`。

## 实体/DTO

注解：`@Data @Builder @NoArgsConstructor @AllArgsConstructor`。使用 builder 构建。禁止 `new Xxx()` 后连续 setter。

允许添加纯谓词方法（`is*` / `has*` / `can*`，只读自身字段与入参、无副作用、不依赖 mapper / 上下文），落位与命名见 `## 多行布尔条件`。

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

更新使用单参数版本 `update(Wrappers.lambdaUpdate()...)`，禁止 `update(null, Wrappers...)`。

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

### 手写 SQL 常量抽取

使用 `inSql` 且 SQL 片段**包含变量拼接**时，禁止在参数位内联拼接 SQL 字符串表达式。
必须先抽取为方法体内局部 `final` 变量（camelCase，命名见 `## 命名`），紧邻使用处之上声明，再作为参数传入。
纯字面量（不含变量拼接）的 `inSql` 可豁免。

```java
// 必须：先抽为 final 局部，紧邻使用处
final String skuIdInSql = "SELECT id FROM cm_product_sku WHERE product_id = " + productId;
List<ProductSkuSpecDO> specs = productSkuSpecMapper.selectList(
        Wrappers.<ProductSkuSpecDO>lambdaQuery()
                .in(ProductSkuSpecDO::getSpecValueId, specValueIds)
                .inSql(ProductSkuSpecDO::getSkuId, skuIdInSql)
);
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
resp.setItems(
        items.stream()
             .map(orderItemConverter::toItemResp)
             .toList()
);

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

## 多行布尔条件

布尔条件出现换行续排（行首 `||` / `&&`）时，必须抽取为具名谓词方法，禁止在 `if` / `AssertUtils` 中裸写多行复合条件。

落位按条件主语决定：

| 主语 | 落位 | 示例 |
|------|------|------|
| 实体字段 | 实体谓词方法 | `uc.isOverdue()` |
| Req/DTO 字段 | DTO 谓词方法 | `req.isDiscountAmountValid()` |
| 跨对象 | 主实体 + 上下文参数 | `template.isNotStarted(now)` |
| 纯外部校验 | Service 私有方法 | `isTokenValid(token)` |

要求：

- 等价抽取：方法忠实表达原条件，禁止顺带翻转语义；确有 bug 时单独修复，不混入重构。
- 正面命名：`is*` / `has*` / `can*`，表达真实意图，取反一律在调用处（`!uc.isOverdue()`）。
- 纯函数：只读自身字段与入参，无副作用，不依赖 mapper / 上下文。

```java
// 禁止：if 中裸写多行复合条件
if (uc.getStatus() != UserCouponStatusEnum.AVAILABLE
        || uc.getExpireTime() == null
        || !uc.getExpireTime().isBefore(LocalDateTime.now())) {
    return;
}

// 必须：等价抽取为实体谓词，取反在调用处
if (!uc.isOverdue()) {
    return;
}
```
