# cloudmall-common-spring-boot-starter

通用 Web 基础设施 starter，提供用户上下文透传与全局异常处理，让业务服务开箱即用地获得统一登录态上下文与统一错误响应。

## 功能

### 1. 用户上下文过滤器（`UserContextFilter`）

从请求头 `X-Auth-User` 解析 `UserContext`（JSON）写入 `UserContextHolder`（ThreadLocal），请求结束在 `finally` 中自动 `clear()`，避免线程池内存泄漏。

- 注册为最高优先级过滤器（`Ordered.HIGHEST_PRECEDENCE + 10`），拦截 `/*`
- 请求头为空时跳过解析，`UserContextHolder.get()` 返回 `null`
- 仅在 Servlet Web 环境装配（Gateway WebFlux 自动跳过）

业务侧直接读取当前登录用户：

```java
Long userId = UserContextHolder.getUserId();
boolean admin = UserContextHolder.isAdmin();
Long shopId = UserContextHolder.getShopId();
```

### 2. 全局异常处理（`GlobalExceptionHandler`）

`@RestControllerAdvice` 将各类异常统一转换为 `Result<T>` 响应：

| 异常 | HTTP 状态 | 响应 |
|------|-----------|------|
| `BizException` | 业务错误码对应状态 | `Result.error(bizError)` |
| `FeignRemoteException` | 400 | 携带远程错误码与消息 |
| 参数校验失败（`@Valid` / `@Validated`） | 400 | 首个字段错误消息 |
| 缺少请求参数 | 400 | 缺参提示 |
| 请求体格式错误 | 400 | 参数格式错误 |
| 接口不存在（404） | 404 | 接口不存在 |
| 请求方法不支持 | 405 | 请求方法不支持 |
| 未知异常 | 500 | 服务器内部错误 |

## 自动装配

`CommonAutoConfiguration` 通过 `AutoConfiguration.imports` 自动注册，无需显式配置；`@ConditionalOnWebApplication(type = SERVLET)` 保证仅在 Servlet 环境生效（排除 Gateway WebFlux）。

## 依赖关系

```
cloudmall-common-spring-boot-starter
├── cloudmall-common（UserContext、UserContextHolder、Result、BizException 等）
├── hutool-all（字符串工具）
└── spring-boot-starter-web
```

## 注意事项

- 该 starter 面向 Servlet 业务服务；Gateway 等 WebFlux 应用不会装配过滤器与异常处理器。
- `UserContextFilter` 解析的 `X-Auth-User` 请求头由 feign starter 的 `FeignUserContextInterceptor` 写入，二者配合实现跨服务用户透传。
