# cloudmall-mybatis-plus-spring-boot-starter

MyBatis-Plus 自动配置与查询条件构建增强。

## 功能

- **MyBatis-Plus 自动配置**：分页插件、乐观锁插件、元对象字段填充（createTime / updateTime）、SQL 日志拦截器
- **注解驱动查询条件构建**：在 Req 类字段上声明 `@QueryCondition`，自动生成查询条件
- **`ConditionBuilder<T>` 接口**：通用条件构建能力接口，分页和非分页查询统一复用
- **`PageReq<T>` 基类**：继承 `Page<T>` 的分页请求模型，实现 `ConditionBuilder<T>`
- **`ConditionReq<T>` 基类**：非分页列表查询请求模型，实现 `ConditionBuilder<T>`

---

## 快速开始

### 分页查询

**1. 定义分页请求类：**

```java
@Getter @Setter
public class UserPageReq extends PageReq<UserDO> {

    @QueryCondition(property = "username", operator = Operator.LIKE)
    private String name;

    @QueryCondition(operator = Operator.EQ)
    private Boolean enabled;
}
```

**2. Service 层调用：**

```java
@Override
public IPage<UserResp> pageUser(UserPageReq req) {
    return userMapper.selectPage(req, req.buildLambdaWrapper())
                     .convert(userConverter::toResp);
}
```

### 非分页列表查询

**1. 定义列表查询请求类（继承 `ConditionReq<T>`）：**

```java
@Getter @Setter
public class GoodsListReq extends ConditionReq<GoodsDO> {

    @QueryCondition(operator = Operator.EQ)
    private Long categoryId;

    @QueryCondition(operator = Operator.EQ)
    private StatusEnum status;

    @QueryCondition(property = "name", operator = Operator.LIKE)
    private String keyword;
}
```

> `ConditionReq<T>` 已内建 `buildLambdaWrapper()` 实现，子类无需覆盖，只需声明 `@QueryCondition` 字段。

**2. Service 层调用（一个方法通吃所有列表查询）：**

```java
// Service 只依赖 ConditionBuilder 接口
public List<GoodsResp> list(ConditionBuilder<GoodsDO> filter) {
    return goodsMapper.selectList(filter.buildLambdaWrapper())
                      .stream().map(goodsConverter::toResp)
                      .toList();
}
```

**3. Controller 按需构造 Req：**

```java
@GetMapping
public Result<List<GoodsResp>> list(GoodsListReq req) {
    return Result.success(goodsService.list(req));
}

@GetMapping("/active")
public Result<List<GoodsResp>> listActive() {
    var req = new GoodsListReq().setStatus(StatusEnum.ENABLED);
    return Result.success(goodsService.list(req));
}
```

---

## 核心类关系

```
                     ConditionBuilder<T>
                     │  buildLambdaWrapper()
                     │
          ┌──────────┴──────────┐
          │                     │
     PageReq<T>            ConditionReq<T>
     extends Page<T>       实现 ConditionBuilder
     实现 ConditionBuilder  内建 buildLambdaWrapper()
     含分页参数              不含分页参数，轻量POJO
                               │
                               │
                          GoodsListReq (extends ConditionReq<GoodsDO>)
                          只声明 @QueryCondition 字段
```

**Service 层统一接收 `ConditionBuilder<T>`：**

| 场景 | 入参类型 | Mapper 调用 |
|------|----------|-------------|
| 分页查询 | `PageReq<T>` 子类 | `selectPage(req, wrapper)` |
| 列表查询 | `ConditionReq<T>` 子类 | `selectList(wrapper)` |

---

## `Operator` 枚举

查询操作符已从 `QueryCondition` 内部枚举迁移为顶层枚举 `Operator`，使用更简洁：

```java
// 简洁写法（推荐）
@QueryCondition(operator = Operator.EQ)

// 而非
@QueryCondition(operator = QueryCondition.Operator.EQ)
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

---

## `@QueryCondition` 使用细则

### 字段映射规则

字段名与实体属性名一致时，无需指定 `property`：

```java
// Req 字段名 "enabled" 对应实体属性 "enabled"，自动匹配
@QueryCondition(operator = Operator.EQ)
private Boolean enabled;
```

不一致时，用 `property` 显式映射：

```java
// Req 字段名 "name" 对应实体属性 "username"
@QueryCondition(property = "username", operator = Operator.LIKE)
private String name;
```

### 范围查询

拆为两个字段（分别用 `GE` / `LE`），比 `BETWEEN` 更清晰：

```java
@QueryCondition(property = "createTime", operator = Operator.GE)
private LocalDateTime createTimeStart;

@QueryCondition(property = "createTime", operator = Operator.LE)
private LocalDateTime createTimeEnd;
```

### 空值策略

`ignoreNull` 默认 `true`，字段值为 `null`、空字符串、空集合时自动跳过该条件，避免生成无效 SQL。

`IS_NULL` 和 `IS_NOT_NULL` 为自足操作符，不受 `ignoreNull` 影响——注解本身即表达查询语义，无需依赖字段值。

### 复杂条件的处理

`buildLambdaWrapper()` 中可混用 Lambda 条件，注解无法表达的逻辑在此补充：

```java
@Getter @Setter
public class GoodsListReq extends ConditionReq<GoodsDO> {

    @QueryCondition(operator = Operator.EQ)
    private Long categoryId;

    private String sortBy;  // 无注解，不参与自动构建

    @Override
    public LambdaQueryWrapper<GoodsDO> buildLambdaWrapper() {
        // 先处理 @QueryCondition 注解字段
        LambdaQueryWrapper<GoodsDO> wrapper = super.buildLambdaWrapper();
        // 排序逻辑手动补充
        if ("price_asc".equals(sortBy)) {
            wrapper.orderByAsc(GoodsDO::getPrice);
        }
        return wrapper;
    }
}
```

### 特殊场景：值变换

注解无法表达值变换逻辑（如 `enabled → deleted` 值反转），在 Service 层手动补充：

```java
@Override
public IPage<UserResp> pageUser(UserPageReq req) {
    var wrapper = req.buildLambdaWrapper();
    // enabled → deleted 值反转，注解无法直接表达
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
