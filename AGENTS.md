# Repository Guidelines

`cloud-mall-cloud` is a Spring Cloud Alibaba microservice platform for e-commerce built with Java 17, Spring Boot 3.5.7, and Maven multi-module builds.

## Project Structure & Module Organization

- `common/` — shared utilities and cross-service domain objects.
- `service-api/` — Feign client contracts, one module per service (`auth-api`, `order-api`, etc.).
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
- Service interfaces are named `I*Service` (e.g., `IAuthService`); implementations drop the `I` prefix (`AuthService`).
- Pagination methods are named `page*`.
- Entities and DTOs use `@Data @Builder @NoArgsConstructor @AllArgsConstructor` and builder construction.
- Use Hutool `CollUtil.isEmpty()`/`isNotEmpty()` for collection checks and `Wrappers.lambdaQuery()` for MyBatis-Plus queries.
- 分页搜索请求一律继承 starter 提供的 `PageReq<T>`，配合 `@QueryCondition` 注解声明查询条件，禁止手写 `page`/`pageSize` 字段或手工构建分页条件。
- 分页调用链保持简单：Service 只把 req 传给 Mapper 的 `default` 方法，条件由 Mapper 内部 `req.buildLambdaWrapper()` 构建，Service 层不接触 `Wrapper`。
- `LIMIT` 子句一律使用 `LimitEnum`（常量或 `LimitEnum.of(n)`），禁止手写 `.last("LIMIT ...")` 字符串。
- Full conventions live in `.pi/rules/code-style.md`.

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
