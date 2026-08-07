---
description: Java代码风格约束。
paths: "**/*.java"
---

## 命名

| 元素 | 约定 | 示例 |
|------|------|------|
| Service 接口 | `I*Service` | `IAuthService` |
| Service 实现 | `*Service`（去掉 `I` 前缀） | `AuthService` |
| 分页查询方法 | `page*` | `pageUserInfos` |

## Controller

- 方法命名与 Service 保持一致（分页方法使用 `page*`）
- 方法参数超过 **3 个**（即 ≥ 4 个）时，必须封装为对应的 `xxxReq` 对象，禁止逐一声明 `@RequestParam`

## 集合判空

使用 Hutool 的 `CollectionUtil.isEmpty()` / `CollectionUtil.isNotEmpty()` 判断集合，禁止手写 `== null || .isEmpty()`。

## 实体与 DTO

- 注解固定为 `@Data @Builder @NoArgsConstructor @AllArgsConstructor`
- 统一使用 builder 构建对象，禁止 `new Xxx()` 后逐字段 setter 赋值
- 允许添加纯谓词方法（`is*` / `has*` / `can*`）：只读自身字段与入参、无副作用、不依赖 mapper 或上下文；落位与命名见「多行布尔条件」

### 注解位置

注解禁止写在泛型参数旁边，必须写在字段定义语句上方：

| 禁止写法 | 必须使用 |
|----------|----------|
| `private List<@Valid Xxx> items;` | `@Valid private List<Xxx> items;` |

```java
// 禁止
private List<@Valid QuoteOrderItemReq> items;

// 必须
@Valid
private List<QuoteOrderItemReq> items;
```

## 注释

- 内容使用文字描述，禁止用数字序号（`1.`、`2.`、`3.` 等）标记步骤或条目
- 禁止分隔线注释：禁止使用 `=` 或 `-` 填充的分隔线（如 `// ===== 认证 =====`、`// ---------- 内部方法 ----------`）
- 方法之间使用 Javadoc 或自然空行分隔；类内部不同职责区通过提取私有方法或拆分小类表达

| 禁止写法 | 必须使用 |
|----------|----------|
| `// ===== 认证 =====` | Javadoc 描述职责 |
| `// ---------- 内部方法 ----------` | 提取为私有方法 |

## MyBatis-Plus

- 使用 `Wrappers.lambdaQuery()` / `Wrappers.lambdaUpdate()`，禁止 `new LambdaQueryWrapper<>()`
- 更新使用单参数版本 `update(Wrappers.lambdaUpdate()...)`，禁止 `update(null, Wrappers...)`

### 条件值使用枚举

对 `status`、`enable` 等数据库字段构造 Wrapper 条件时，必须使用对应的业务枚举（命名遵循 `*Enum` 后缀），禁止使用魔数或布尔值：

| 禁止写法 | 必须使用 |
|----------|----------|
| `.eq(ResourceDO::getEnable, 1)` | `.eq(ResourceDO::getEnable, StatusEnum.ENABLED)` |
| `.eq(ResourceDO::getEnable, true)` | `.eq(ResourceDO::getEnable, StatusEnum.ENABLED)` |
| `.eq(ResourceDO::getStatus, false)` | `.eq(ResourceDO::getStatus, StatusEnum.DISABLED)` |
| `.eq(ResourceDO::getStatus, 0)` | `.eq(ResourceDO::getStatus, StatusEnum.DISABLED)` |

枚举定义位于 `@starters/cloudmall-mybatis-plus-spring-boot-starter/.../enums/StatusEnum.java`：

```java
DISABLED(0, "关闭"),
ENABLED(1, "开启");
```

### 分页请求与 LIMIT

分页搜索请求必须继承 starter 提供的 `PageReq<T>`（`com.cloudmall.mybatisplus.model.PageReq`），
查询条件通过 `@QueryCondition` 注解声明，由 `buildLambdaWrapper()` 自动生成。

| 禁止写法 | 必须使用 |
|----------|----------|
| req 手写 `private Integer page; private Integer pageSize;` | `class XxxPageReq extends PageReq<XxxDO>` + `@QueryCondition` 字段 |
| Service 构造分页 `Wrapper` 传给 Mapper | Service 只传 req，Mapper `default` 方法内 `req.buildLambdaWrapper()` |

`LIMIT` 子句一律使用 `LimitEnum`（`LimitEnum.TWO_HUNDRED.getSql()` 等常量或 `LimitEnum.of(n)`），
禁止手写 `.last("LIMIT ...")` 字符串拼接。

| 禁止写法 | 必须使用 |
|----------|----------|
| `.last("LIMIT " + limit)` | `.last(LimitEnum.of(limit))` |
| `.last("LIMIT 200")` | `.last(LimitEnum.TWO_HUNDRED.getSql())` |

### 手写 SQL 常量抽取

使用 `inSql` 且 SQL 片段**包含变量拼接**时，禁止在参数位内联拼接 SQL 字符串表达式。必须先抽取为方法体内紧邻使用处之上的 `final` 局部变量（camelCase，命名见「命名」），再作为参数传入。纯字面量（不含变量拼接）的 `inSql` 可豁免。

```java
// 必须：先抽为 final 局部，紧邻使用处
final String skuIdInSql = "SELECT id FROM cm_product_sku WHERE product_id = " + productId;
List<ProductSkuSpecDO> specs = productSkuSpecMapper.selectList(
        Wrappers.<ProductSkuSpecDO>lambdaQuery()
                .in(ProductSkuSpecDO::getSpecValueId, specValueIds)
                .inSql(ProductSkuSpecDO::getSkuId, skuIdInSql)
);
```

### Mapper 方法禁止注解脚本 SQL

Mapper 方法一律定义为 `default` 方法并用 `Wrappers.lambdaQuery()` / `Wrappers.lambdaUpdate()` 构建，**禁止在接口上书写任何手动定义 SQL 的注解**。

禁止清单（含其 XML `<script>` 等价写法）：

| 分类 | 禁止注解 |
|------|----------|
| 脚本 SQL | `@Select`、`@Insert`、`@Update`、`@Delete` |
| Provider 动态 SQL | `@SelectProvider`、`@InsertProvider`、`@UpdateProvider`、`@DeleteProvider` |
| 脚本注解内嵌 | `<script>` / `<foreach>` / `<if>` 等 XML 标签拼在注解字符串中 |

需要逐行计算的 SQL 表达式（如批量自增、按列推导状态）时，使用 `setSql` 片段：

- 纯字面量（不含变量拼接）的 `setSql` 可内联
- 包含变量拼接的 `setSql` 必须先抽取为方法体内紧邻使用处之上的 `final` 局部变量（与「手写 SQL 常量抽取」一致）
- 动态值优先用 `set(实体::getXxx, value)` 参数绑定，禁止拼进 `setSql`

```java
// 必须：default 方法 + Wrapper，setSql 仅承载无法用 DSL 表达的逐行表达式
default int markFailedBatch(Collection<Long> ids, int maxRetry, int retryBaseSeconds) {
    if (CollUtil.isEmpty(ids)) {
        return 0;
    }
    final String nextRetryTimeSql = String.format(
            "next_retry_time = DATE_ADD(NOW(), INTERVAL (retry_count + 1) * %d SECOND)",
            retryBaseSeconds
    );
    return update(
            Wrappers.<OutboxDO>lambdaUpdate()
                    .setSql("retry_count = retry_count + 1")
                    .setSql(nextRetryTimeSql)
                    .set(OutboxDO::getLastError, lastError)
                    .in(OutboxDO::getId, ids)
    );
}
```

| 禁止写法 | 必须使用 |
|----------|----------|
| `@Select("SELECT ... WHERE ...")` / `@Update("<script>UPDATE ... </script>")` 等一切脚本 SQL | `default` 方法 + `Wrappers.lambdaQuery()/lambdaUpdate()` |
| `@SelectProvider` / `@UpdateProvider` 等 Provider 动态 SQL | `default` 方法 + `Wrappers`（表达式用 `setSql` 片段） |
| 变量拼接内联在 `setSql` 参数位 | 先抽为 `final` 局部变量，紧邻使用处 |

## 查询与转换分离

Service 层中，数据库查询（mapper 调用）与对象转换（converter 调用）必须分步书写，禁止嵌套在同一个表达式中。查询结果先落入具名变量，再做转换，便于阅读、断点调试与结果复用。

批量转换必须调用 converter 提供的 `toXxxList` 方法，禁止在 Service 层手写 `stream().map(converter::toXxx)`。

| 禁止写法 | 必须使用 |
|----------|----------|
| `return brandConverter.toRespList(brandMapper.selectList(...));` | `List<BrandDO> brands = brandMapper.selectList(...); return brandConverter.toRespList(brands);` |
| `resp.setItems(orderItemMapper.selectList(...).stream().map(...).toList());` | `List<OrderItemDO> items = orderItemMapper.selectList(...); resp.setItems(orderItemConverter.toItemRespList(items));` |
| `resp.setItems(items.stream().map(orderItemConverter::toItemResp).toList());` | `resp.setItems(orderItemConverter.toItemRespList(items));` |

```java
// 禁止：查询与转换揉在同一个表达式
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

| 场景 | 禁止写法 | 必须使用 |
|------|----------|----------|
| null 默认值 | `x != null ? x : defaultValue` | `ObjectUtil.defaultIfNull(x, defaultValue)` |
| Boolean 判 true | `x != null && x` | `BooleanUtil.isTrue(x)` |
| Boolean 判 false | `x == null \|\| !x` | `BooleanUtil.isFalse(x)` |
| 字符串判空 | `s == null \|\| s.isEmpty()` | `StrUtil.isBlank(s)` |

### 枚举查找

按 code 查找枚举使用 `Arrays.stream().filter().findFirst().orElseThrow()`，禁止 for 循环；code 为 null 或未命中时抛 `IllegalArgumentException`。

## 多行布尔条件

布尔条件出现换行续排（行首 `||` / `&&`）时，必须抽取为具名谓词方法，禁止在 `if` / `Asserts` 中裸写多行复合条件。

落位按条件主语决定：

| 主语 | 落位 | 示例 |
|------|------|------|
| 实体字段 | 实体谓词方法 | `uc.isOverdue()` |
| Req/DTO 字段 | DTO 谓词方法 | `req.isDiscountAmountValid()` |
| 跨对象 | 主实体 + 上下文参数 | `template.isNotStarted(now)` |
| 纯外部校验 | Service 私有方法 | `isTokenValid(token)` |

要求：

- 等价抽取：方法忠实表达原条件，禁止顺带翻转语义；确有 bug 时单独修复，不混入重构
- 正面命名：`is*` / `has*` / `can*` 表达真实意图，取反一律在调用处（`!uc.isOverdue()`）
- 纯函数：只读自身字段与入参，无副作用，不依赖 mapper 或上下文

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
