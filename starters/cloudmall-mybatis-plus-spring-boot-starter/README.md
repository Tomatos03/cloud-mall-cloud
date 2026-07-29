# cloudmall-mybatis-plus-spring-boot-starter

MyBatis-Plus 自动配置与查询条件构建增强。

## 功能

- **MyBatis-Plus 自动配置**：分页插件、乐观锁插件、元对象字段填充（createTime / updateTime）、SQL 日志拦截器
- **注解驱动查询条件构建**：在 `PageReq` 子类字段上声明 `@QueryCondition`，自动生成查询条件
- **`PageReq` 基类**：统一分页请求模型，内建注解解析与 Wrapper 构建

---

## `@QueryCondition` 使用指南

### 基本概念

`@QueryCondition` 声明一个 Req 字段到实体属性的查询条件映射。配合 `PageReq.buildLambdaWrapper()` 自动构建 `LambdaQueryWrapper`，消除 Service 层手写条件代码。

### 快速开始

**1. 定义分页请求类：**

```java
@Getter @Setter
public class UserPageReq extends PageReq<UserDO> {

    @QueryCondition(property = "username", operator = QueryCondition.Operator.LIKE)
    private String name;

    @QueryCondition(operator = QueryCondition.Operator.EQ)
    private Boolean enabled;
}
```

**2. Service 层调用：**

```java
@Override
public IPage<UserInfoResp> pageUserInfos(UserPageReq req) {
    return userMapper.selectPage(req, req.buildLambdaWrapper())
                     .convert(userConverter::toResp);
}
```

### 操作符对照表

| 操作符 | SQL 片段 | 说明 |
|--------|----------|------|
| `EQ` | `column = ?` | 等值查询 |
| `NE` | `column <> ?` | 不等查询 |
| `LIKE` | `column LIKE '%?%'` | 模糊匹配（两侧） |
| `LEFT_LIKE` | `column LIKE '%?'` | 左模糊 |
| `RIGHT_LIKE` | `column LIKE '?%'` | 右模糊 |
| `GT` | `column > ?` | 大于 |
| `GE` | `column >= ?` | 大于等于 |
| `LT` | `column < ?` | 小于 |
| `LE` | `column <= ?` | 小于等于 |
| `BETWEEN` | `column BETWEEN ? AND ?` | 范围（值需为 `Object[2]`） |
| `IN` | `column IN (?, ?, ...)` | 集合包含（值需为 `Collection`） |
| `NOT_IN` | `column NOT IN (?, ?, ...)` | 集合不包含（值需为 `Collection`） |
| `IS_NULL` | `column IS NULL` | 为空判断（忽略值参数） |
| `IS_NOT_NULL` | `column IS NOT NULL` | 非空判断（忽略值参数） |

### 字段映射规则

当 Req 字段名与实体属性名一致时，无需指定 `property`：

```java
// Req 字段名 "enabled" 对应实体属性 "enabled"，自动匹配
@QueryCondition(operator = EQ)
private Boolean enabled;
```

当不一致时，用 `property` 显式映射：

```java
// Req 字段名 "name" 对应实体属性 "username"
@QueryCondition(property = "username", operator = LIKE)
private String name;
```

### 范围查询

范围查询建议拆为两个字段（分别用 `GE` / `LE`），比 `BETWEEN` 更清晰：

```java
@QueryCondition(property = "createTime", operator = QueryCondition.Operator.GE)
private LocalDateTime createTimeStart;

@QueryCondition(property = "createTime", operator = QueryCondition.Operator.LE)
private LocalDateTime createTimeEnd;
```

### 空值策略

`ignoreNull` 默认 `true`，字段值为 `null`、空字符串、空集合时自动跳过该条件，避免生成无效 SQL。

`IS_NULL` 和 `IS_NOT_NULL` 为自足操作符，不受 `ignoreNull` 影响——注解本身即表达查询语义，无需依赖字段值。

### 特殊场景：值变换

注解无法表达值变换逻辑（如 `enabled → deleted` 值反转），在 Service 层手动补充：

```java
@Override
public IPage<UserInfoResp> pageUserInfos(UserPageReq req) {
    var wrapper = req.buildLambdaWrapper();
    // enabled → deleted 值反转，注解无法表达，手动补充
    if (req.getEnabled() != null) {
        wrapper.eq("deleted", Boolean.FALSE.equals(req.getEnabled()));
    }
    return userMapper.selectPage(req, wrapper)
                     .convert(userConverter::toResp);
}
```

### 混用 Lambda 条件

`buildLambdaWrapper()` 返回 `LambdaQueryWrapper<T>`，可继续追加 Lambda 条件：

```java
var wrapper = req.buildLambdaWrapper();
wrapper.and(w -> w.eq(UserDO::getUsername, "admin")
                  .or().eq(UserDO::getPhone, phone));
```

### 完整示例

```java
@Getter @Setter
public class GoodsPageReq extends PageReq<GoodsDO> {

    @QueryCondition(property = "name", operator = QueryCondition.Operator.LIKE)
    private String keyword;

    @QueryCondition(operator = QueryCondition.Operator.EQ)
    private Long categoryId;

    @QueryCondition(property = "price", operator = QueryCondition.Operator.GE)
    private BigDecimal minPrice;

    @QueryCondition(property = "price", operator = QueryCondition.Operator.LE)
    private BigDecimal maxPrice;

    @QueryCondition(operator = QueryCondition.Operator.EQ)
    private Boolean enabled;
}

// Service
public IPage<GoodsResp> pageGoods(GoodsPageReq req) {
    return goodsMapper.selectPage(req, req.buildLambdaWrapper())
                      .convert(goodsConverter::toResp);
}
```
