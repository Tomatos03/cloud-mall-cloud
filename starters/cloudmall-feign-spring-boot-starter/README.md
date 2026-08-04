# cloudmall-feign-spring-boot-starter

微服务 Feign 调用基础设施，提供用户上下文透传与 Result 自动拆箱能力。

## 功能

### 1. 用户上下文透传拦截器

`FeignUserContextInterceptor`：自动将当前线程的 `UserContextHolder` 写入请求头 `X-Auth-User`，实现服务间用户身份透传。

### 2. Result 自动拆箱解码器

`ResultUnwrappingDecoder`：自动拆箱服务端统一返回的 `Result<T>` 包装。

- `code = SUCCESS` → 直接返回 `Result.data`（反序列化为目标类型 `T`）
- `code ≠ SUCCESS` → 抛出 `FeignRemoteException`，携带远程错误码与消息，由全局异常处理器统一转换为 `Result` 响应

Feign 接口声明时直接返回目标类型，无需 `Result<T>` 包装：

```java
// 改前（需要调用端手动拆箱）
Result<CouponOrderCalcResp> lock(@RequestBody CouponOrderLockReq req);

// 改后（Decoder 自动拆箱）
CouponOrderCalcResp lock(@RequestBody CouponOrderLockReq req);
```

调用端不再需要 `AssertUtils.isTrue(result != null && result.getCode() == ...)` 模板代码。

## 自动装配

通过 `spring.factories` 或 `AutoConfiguration.imports` 自动注册，无需显式配置：

- `FeignUserContextInterceptor` — 用户上下文透传
- `ResultUnwrappingDecoder` — Result 自动拆箱

## 依赖关系

```
cloudmall-feign-spring-boot-starter
├── cloudmall-common（Result、FeignRemoteException）
├── jackson-databind（JSON 反序列化）
└── spring-cloud-starter-openfeign
```
