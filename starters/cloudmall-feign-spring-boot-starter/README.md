# cloudmall-feign-spring-boot-starter

微服务 Feign 调用基础设施，提供用户上下文透传与 Result 自动拆箱能力。

## 功能

### 1. 用户上下文透传拦截器

`FeignUserContextInterceptor`：自动将当前线程的 `UserContextHolder` 写入请求头 `X-Auth-User`，实现服务间用户身份透传。

### 2. Result 自动拆箱解码器

`ResultDecoder`：自动拆箱服务端统一返回的 `Result<T>` 包装。

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
- `ResultParser` — Result 信封解析（解码器/错误解码器共用）
- `ResultDecoder` — Result 自动拆箱

## 成功链路

正常响应统一走 `ResultParser`：读取响应体、校验 `Result` 信封、`code=0` 时把 `data` 反序列化为目标类型；`code≠0` 仍按既有语义抛 `FeignRemoteException`。非 2xx 响应由 Feign 错误解码器处理，不经过拆箱解码器。

传输层默认启用 Apache HttpClient 5 连接池，超时与连接池参数统一在 Nacos `common.yaml` 的 `spring.cloud.openfeign` 节点配置，避免各服务配置漂移。

## 依赖关系

```
cloudmall-feign-spring-boot-starter
├── cloudmall-common（Result、FeignRemoteException）
├── jackson-databind（JSON 反序列化）
├── spring-cloud-starter-openfeign
├── feign-hc5（HTTP 连接池）
└── spring-cloud-starter-loadbalancer
```
