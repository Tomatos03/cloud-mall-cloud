# Domain Conventions

## 分层与职责
- Controller 只做参数接收、调用 Service
- 业务规则、数据一致性放在 Service 层

## API 模块规范
- `api/*-api` 模块：定义 FeignClient 接口 + Request/Response DTO
- `*-biz` 模块：实现 Controller + Service + Entity + Mapper
- Controller 路径前缀与服务名对齐（如 `/auth/**`、`/users/**`）

## 认证
- JWT 无状态认证，使用 `JwtTokenProvider` 生成和验证 Token
- Token 包含：userId, username, userType
- Spring Security 配置在 `SecurityConfig`，当前全放通，鉴权在网关层处理

## 返回结果
- 统一使用 `Result<T>` 包装返回
- Controller 直接返回 `Result.success(...)` / `Result.error(…)`
- 业务异常使用 `BizException + BizErrorCode`

## 异常处理
- 业务异常统一 `BizException(BizErrorCode)`
- 禁止直接抛出通用 `Exception/RuntimeException`
- 条件校验优先使用 `AssertUtils` 工具类，禁止手写 `if (x) { throw new BizException(...); }`：
  - `AssertUtils.notNull(obj, errorCode)` — 替代 `if (obj == null)`
  - `AssertUtils.isNull(obj, errorCode)` — 替代 `if (obj != null)`
  - `AssertUtils.isTrue(expr, errorCode)` — 替代 `if (!expr)`
  - `AssertUtils.isFalse(expr, errorCode)` — 替代 `if (expr)`
  - `AssertUtils.notEmpty(collection, errorCode)` — 替代 `if (CollUtil.isEmpty(collection))`
  - 若 `AssertUtils` 缺少所需方法，先扩展工具类再使用

## 数据库
- 单库 `cm_mall`
- 表前缀区分模块：`cm_auth_user`、`cm_address`、`cm_store`、`cm_goods`、`cm_order` 等
- 所有表包含 `create_time`、`update_time`、`deleted` 公共字段
- 软删除：`deleted = 1` 表示已删除

## 分页
- 使用 MyBatis-Plus `Page` 分页

## API 路径规范 (RESTful Level 2)
- 资源名使用复数：`/users`、`/stores`、`/addresses`
- 操作通过 HTTP 方法表达：`GET` 查询、`POST` 创建、`PUT` 更新、`DELETE` 删除
- 认证接口属于过程操作，使用动词：`/auth/login`、`/auth/register`
