---
description: 领域架构约定，适用于所有 Java 源文件。
paths: "**/*.java"
---

## 分层职责

- Controller：接收参数、调用 Service。禁止编写业务逻辑。
- Service：业务规则、数据一致性、事务管理。

## FeignClient 一致性

Controller 端点变更（路径、方法、参数）必须同步更新 FeignClient。禁止不一致。

## 返回与异常

- 统一 `Result<T>` 包装。Controller 返回 `Result.success()` / `Result.error()`
- 业务异常使用 `BizException` + `BizErrorCode`。禁止裸 `Exception` / `RuntimeException`

## 条件校验

使用 `AssertUtils`，禁止手写 `if (x) { throw ... }`：

| 禁止写法 | 必须使用 |
|----------|----------|
| `if (obj == null)` | `AssertUtils.notNull(obj, code)` |
| `if (obj != null)` | `AssertUtils.isNull(obj, code)` |
| `if (!expr)` | `AssertUtils.isTrue(expr, code)` |
| `if (expr)` | `AssertUtils.isFalse(expr, code)` |
| `if (CollUtil.isEmpty(coll))` | `AssertUtils.notEmpty(coll, code)` |

缺少方法时先扩展 `AssertUtils`。

## 数据库

- 表前缀 `cm_`（如 `cm_user`、`cm_role`、`cm_product_*`）
- 通用字段：`create_time`、`update_time`、`deleted`
- 关联表（如 `cm_user_role`）：仅 `create_time`
- 软删除：`deleted = 1`
- 分页使用 MyBatis-Plus `Page`。禁止手写 `LIMIT`

## API 路径

- 资源名使用复数：`/users`、`/stores`、`/addresses`
- 操作通过 HTTP 方法表达：GET 查询、POST 创建、PUT 更新、DELETE 删除
- 认证接口使用动词：`/auth/login`、`/auth/register`
- 禁止资源名单数（`/user`）或用动词替代 HTTP 方法（`/getUsers`）
