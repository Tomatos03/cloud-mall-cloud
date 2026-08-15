# Repository Guidelines

`cloud-mall-cloud` is a Spring Cloud Alibaba microservice platform for e-commerce built with Java 17, Spring Boot 3.5.7, and Maven multi-module builds.

## Project Structure & Module Organization

- `common/` — shared utilities and cross-service domain objects.
- `service-api/` — Feign client contracts, one module per service (`user-api`, `order-api`, etc.).
- `gateway/` — Spring Cloud Gateway entry point for routing and authentication.
- `starters/` — reusable Spring Boot starters (`cloudmall-jwt-spring-boot-starter`, `cloudmall-mybatis-plus-spring-boot-starter`, ...).
- `services/` — business services (`order-service`, `product-service`, ...), layered per module: `controller`, `service`/`impl`, `mapper`, `entity`, `model/req` and `model/resp`, `convert` (MapStruct), `enums`, `application`, `job`.
- `docs/` — design docs (`docs/design`), Docker compose (`docs/docker/compose.yaml`), Nacos configs (`docs/nacos/config`), SQL scripts (`docs/sql`).

## Build, Test, and Development Commands

- `mvn package` — compile and package all modules.
- `mvn -pl services/order-service -am package` — build one module with its dependencies.
- `mvn spring-boot:run -pl services/<name>` — run a service locally (requires the infrastructure in `docs/docker/compose.yaml`).
- `mvn test` — run tests (JUnit 5 via `spring-boot-starter-test`).

## Coding Style & Naming Conventions

- Java code formatting is handled by the `jstyle` CLI using `jstyle.toml` (4-space indent, 110-char line width); run `jstyle format` to format the code.
- 每次修改文件之后，必须在项目工作空间根目录执行一次格式化命令（`jstyle format`）。
- 以下为精简规范，完整示例与说明见 `.pi/rules/code-style.md`（规范原文保留）。

### 命名

- Service 接口命名 `I*Service`（如 `IAuthService`），实现去掉 `I` 前缀（`AuthService`）。
- 分页查询方法命名 `page*`，Controller 方法名与 Service 保持一致。

### 枚举

- 业务枚举必须带 `desc` 字段描述枚举作用（如 `CheckoutStepEnum.LOAD_SKUS("SKU 加载")`），禁止无 `desc` 的裸枚举。

### Controller

- 方法参数 ≥ 4 个时必须封装为对应的 `xxxReq` 对象，禁止逐一声明 `@RequestParam`。

### 内部接口

- 内部服务端点基路径统一为 `/internal/{服务名}`，服务名取模块短名（如 `order`、`coupon`、`product`、`seckill`、`search`），禁止使用业务路径（如 `/internal/order-events`）。
- 内部接口 Controller 类名统一为 `Internal{服务名}Controller`（如 `InternalOrderController`），禁止 `XxxInternalController` 命名。
- 内部接口 Feign 客户端类名统一为 `Internal{服务名}Client`（如 `InternalOrderClient`），`contextId` 为 `internal{服务名}Client`（如 `internalOrderClient`）。
- 同一服务的内部接口合并到同一个 `Internal{服务名}Controller` 与 `Internal{服务名}Client`，方法级路径保持不变；FeignClient 的 `path` 必须与 Controller 基路径一致。

### 集合判空

- 使用 Hutool `CollectionUtil.isEmpty()` / `CollectionUtil.isNotEmpty()`，禁止手写 `== null || .isEmpty()`。

### 条件校验

- 使用 `Asserts` 断言，禁止手写 `if (x) { throw new BizException(...) }`。
- 缺少断言方法时先扩展 `Asserts`。

### 实体与 DTO

- 注解固定为 `@Data @Builder @NoArgsConstructor @AllArgsConstructor`，统一使用 builder 构建，禁止 `new` 后逐字段 setter 赋值。
- 注解写在字段定义上方，禁止写在泛型参数旁（如 `List<@Valid Xxx>`）。
- 允许纯谓词方法（`is*` / `has*` / `can*`）：只读自身字段与入参、无副作用、不依赖 mapper 或上下文；落位见「多行布尔条件」。

### 注释

- 注释用文字描述，禁止数字序号（`1.`、`2.`）标记步骤；禁止 `=` / `-` 分隔线注释；用 Javadoc、空行或提取私有方法组织代码。

### MyBatis-Plus

- 使用 `Wrappers.lambdaQuery()` / `Wrappers.lambdaUpdate()`，禁止 `new LambdaQueryWrapper<>()`；更新使用单参数版本 `update(Wrappers.lambdaUpdate()...)`。
- 对 `status`、`enable` 等字段构造条件必须使用业务枚举（`*Enum`，如 `StatusEnum.ENABLED`），禁止魔数或布尔值。
- 分页搜索请求继承 `PageReq<T>` + `@QueryCondition`，禁止手写 `page` / `pageSize`；Service 只把 req 传给 Mapper `default` 方法，条件由 `req.buildLambdaWrapper()` 构建，Service 层不接触 `Wrapper`。
- `LIMIT` 一律使用 `LimitEnum`（`LimitEnum.of(n)` 或常量），禁止手写 `.last("LIMIT ...")`。
- 含变量拼接的 `inSql` / `setSql` 必须先抽为方法体内紧邻使用处的 `final` 局部变量再传入；动态值优先用 `set(实体::getXxx, value)`。
- Mapper 禁止注解脚本 SQL（`@Select` / `@Insert` / `@Update` / `@Delete` 及 Provider 系列），一律 `default` 方法 + Wrapper，逐行表达式用 `setSql`。

### 查询与转换分离

- Service 中查询（mapper）与转换（converter）分步书写，禁止嵌套在同一表达式；批量转换调用 converter 的 `toXxxList`，禁止手写 `stream().map(converter::toXxx)`。

### 工具方法

- 优先用 Hutool 替代三元表达式：`ObjectUtil.defaultIfNull`、`BooleanUtil.isTrue/isFalse`、`StrUtil.isBlank`。
- 按 code 查找枚举用 `Arrays.stream().filter().findFirst().orElseThrow()`，code 为 null 或未命中抛 `IllegalArgumentException`。

### 多行布尔条件

- 换行续排（行首 `||` / `&&`）的复合条件必须抽取为具名谓词方法，禁止在 `if` / `Asserts` 中裸写。
- 落位：实体字段 → 实体谓词（`uc.isOverdue()`）；Req/DTO 字段 → DTO 谓词；跨对象 → 主实体 + 上下文参数（`template.isNotStarted(now)`）；纯外部校验 → Service 私有方法。
- 等价抽取不翻转语义（bug 单独修复）；正面命名，取反在调用处（`!uc.isOverdue()`）；纯函数无副作用。

## Testing Guidelines

- Use JUnit 5 and `spring-boot-starter-test`; place tests under `src/test/java`.
- No test sources exist yet — add `XxxTest` classes when touching core service or mapper logic.
- Name test methods `should*`/`when*` to describe expected behavior.

## Commit & Pull Request Guidelines

- Use Conventional Commits with a Chinese summary: `feat(order): 新增...`, `refactor: ...`, `fix:`, `docs:`, `style:`, `chore:`.
- Add a module scope (`order`, `product`, `coupon`, `gateway`) when the change is module-specific.
- Pull requests should describe the change, reference related issues, and include screenshots for UI or API changes.

## Agent-Specific Instructions

- Add `AGENTS.md` files in subdirectories for module-specific guidance.
- Review `.agents/` and `.pi/rules/` before editing code; they may contain tooling and style constraints.
