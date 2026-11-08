# mock-payment-service

模拟第三方支付渠道，以 **微信支付 API v3** 为蓝本实现下单、查询、关单和异步回调，供 Cloud Mall 在本地开发、联调和演示阶段替代真实微信支付/支付宝渠道。

端口：`9011`，注册名：`mock-payment-service`。

## 能力范围

| 能力 | 微信支付协议 | 说明 |
|---|---|---|
| Native 下单 | `POST /v3/pay/transactions/native` | 返回 `code_url`，可生成二维码 |
| JSAPI 下单 | `POST /v3/pay/transactions/jsapi` | 返回 `prepay_id` |
| 商户订单号查询 | `GET /v3/pay/transactions/out-trade-no/{out_trade_no}?mchid=...` | 返回 `trade_state`、金额、支付者等 |
| 微信订单号查询 | `GET /v3/pay/transactions/id/{transaction_id}?mchid=...` | 支付成功后可用 |
| 关闭订单 | `POST /v3/pay/transactions/out-trade-no/{out_trade_no}/close` | 未支付订单关闭，返回 204 |
| 支付回调 | 由本服务 POST 到下单时的 `notify_url` | AES-256-GCM 加密 + RSA-SHA256 签名 |
| 回调重试 | 定时任务 | 指数退避，默认最多 5 次 |

此外提供：

- 内部 Feign 接口 `/internal/mock-payment/**`（返回项目统一 `Result`，order-service 使用）。
- 模拟控制台 `/mock/payments/**`：浏览支付单、模拟支付成功、关单、手动重发回调。
- 可视化模拟收银台 `/mock/cashier/{outTradeNo}`：浏览器点击即可触发支付成功与回调。

## 处理流程

```text
业务前端                order-service                  mock-payment-service
   │  POST /orders/payment/{orderNo} │                            │
   │───────────────────────────────>│  POST /internal/mock-payment/transactions
   │                                │───────────────────────────>│ 落库 NOTPAY，生成 code_url
   │<──── codeUrl / cashierUrl ─────│<───────────────────────────│
   │ 打开收银台/扫码，点击模拟支付    │                            │
   │                                │  POST /mock/payments/{orderNo}/pay
   │                                │───────────────────────────>│ NOTPAY → SUCCESS
   │                                │                            │ 加密报文 + 签名
   │                                │<── POST notify_url ────────│
   │                                │ 验签 → 解密 → handlePaid     │
   │                                │ RocketMQ ORDER_EVENTS       │
   │ 轮询 GET /orders/payment/status │                            │
   │───────────────────────────────>│ 本地 PAID 或主动查询渠道       │
   │<──────────── true/false ───────│                            │
```

## 快速体验

### 方式一：独立调用模拟渠道

```bash
# 1. Native 下单（金额单位：分）
curl -X POST http://localhost:9011/v3/pay/transactions/native \
  -H 'Content-Type: application/json' \
  -d '{
    "appid": "wxmock000000000001",
    "mchid": "1900000001",
    "description": "测试商品",
    "out_trade_no": "ORDER-DEMO-001",
    "notify_url": "http://localhost:9005/orders/payment/notify/wechat",
    "amount": {"total": 100, "currency": "CNY"}
  }'

# 2. 查询（支付前）
curl 'http://localhost:9011/v3/pay/transactions/out-trade-no/ORDER-DEMO-001?mchid=1900000001'

# 3. 模拟用户支付成功（会向上面的 notify_url 发送加密回调）
curl -X POST http://localhost:9011/mock/payments/ORDER-DEMO-001/pay

# 4. 再次查询，trade_state=SUCCESS
curl 'http://localhost:9011/v3/pay/transactions/out-trade-no/ORDER-DEMO-001?mchid=1900000001'
```

浏览器打开 `http://localhost:9011/mock/cashier/ORDER-DEMO-001` 也可以完成模拟支付。

### 方式二：通过 order-service 完整链路

```bash
# 需要先登录并创建订单，携带 JWT
curl -X POST http://localhost:9000/orders/payment/{orderNo} \
  -H "Authorization: Bearer <token>"

# 返回中取 cashierUrl 在浏览器打开，或直接调用模拟渠道
curl -X POST http://localhost:9011/mock/payments/{orderNo}/pay

# 轮询支付状态
curl 'http://localhost:9000/orders/payment/status?orderNo={orderNo}' \
  -H "Authorization: Bearer <token>"
```

order-service 侧接口：

| 方法 | 路径 | 说明 |
|---|---|---|
| POST | `/orders/payment/{orderNo}` | 创建支付单（校验订单归属、待支付状态、金额） |
| GET | `/orders/payment/{orderNo}` | 主动查询渠道并返回支付状态，渠道已支付时补偿订单 |
| GET | `/orders/payment/status?orderNo=...` | 返回 `true/false`，供前端轮询 |
| POST | `/orders/payment/notify/wechat` | 接收微信风格回调（验签、解密、驱动订单 PAID） |

## 协议细节

### 下单请求

字段命名与微信支付一致（`snake_case`）：

```json
{
  "appid": "wxmock000000000001",
  "mchid": "1900000001",
  "description": "CloudMall订单-ORDER-1",
  "out_trade_no": "ORDER-1",
  "time_expire": "2026-01-01T12:00:00+08:00",
  "attach": "cloudmall",
  "notify_url": "http://localhost:9005/orders/payment/notify/wechat",
  "amount": {"total": 100, "currency": "CNY"},
  "payer": {"openid": "mock-openid-1"}
}
```

- `amount.total` 单位为**分**，与微信支付一致。
- `JSAPI` 接口强制要求 `payer.openid`；`NATIVE` 忽略 `payer`。
- `out_trade_no` 在同一商户下唯一；重复下单只要金额和描述一致即幂等返回原支付单。
- `time_expire` 遵循 RFC3339；不传默认 15 分钟。

### 查询响应

```json
{
  "appid": "wxmock000000000001",
  "mchid": "1900000001",
  "out_trade_no": "ORDER-1",
  "transaction_id": "MOCK...",
  "trade_type": "NATIVE",
  "trade_state": "SUCCESS",
  "trade_state_desc": "支付成功",
  "bank_type": "MOCK",
  "attach": "cloudmall",
  "success_time": "2026-01-01T12:00:00+08:00",
  "payer": {"openid": "mock-openid-1"},
  "amount": {
    "total": 100,
    "payer_total": 100,
    "currency": "CNY",
    "payer_currency": "CNY"
  }
}
```

`trade_state` 取值对齐微信：`NOTPAY`、`SUCCESS`、`CLOSED`、`REFUND`、`REVOKED`、`USERPAYING`、`PAYERROR`。

### 回调报文

回调 HTTP 头：

```text
Wechatpay-Signature: <Base64 RSA-SHA256 签名>
Wechatpay-Timestamp: 1724062075
Wechatpay-Nonce: <32 位十六进制随机串>
Wechatpay-Serial: MOCKPLATFORM20260101
Wechatpay-Signature-Type: WECHATPAY2-SHA256-RSA2048
Content-Type: application/json
```

回调报文：

```json
{
  "id": "MOCK-EV-...",
  "create_time": "2026-01-01T12:00:00+08:00",
  "resource_type": "encrypt-resource",
  "event_type": "TRANSACTION.SUCCESS",
  "summary": "支付成功",
  "resource": {
    "original_type": "transaction",
    "algorithm": "AEAD_AES_256_GCM",
    "ciphertext": "<Base64 密文>",
    "associated_data": "transaction",
    "nonce": "<12 位随机串>"
  }
}
```

验签串为三行，每行以 `\n` 结尾：

```text
Wechatpay-Timestamp\n
Wechatpay-Nonce\n
原始请求体\n
```

使用平台公钥执行 `SHA256withRSA` 验签；随后使用 APIv3 密钥（32 字节）对 `ciphertext` 做
`AES/GCM/NoPadding` 解密，`associated_data` 作为 AAD，解密结果为交易 JSON。

业务侧应答：

- 成功：HTTP 200/204，或 `{"code":"SUCCESS","message":"成功"}`。
- 失败：4xx/5xx（本服务的 order-service 回调失败返回 5xx，验签失败返回 401），本服务会按指数退避重试。

### 请求签名（可选）

`mock-payment.signature.verify-request=true` 时，`/v3/**` 请求必须携带：

```text
Authorization: WECHATPAY2-SHA256-RSA2048 mchid="1900000001",nonce_str="...",signature="...",timestamp="...",serial_no="MOCKMERCHANT20260101"
```

签名串为五行：

```text
HTTP方法\n
URL（含查询串，不含域名）\n
时间戳\n
随机串\n
请求体\n
```

仓库内的 `src/main/resources/keys/merchant-private.pem` 是模拟商户私钥，`merchant-public.pem` 是服务端验签公钥；生产环境不会这样保存密钥，此处仅用于本地演示。

## 配置

`docs/nacos/config/mock-payment-service.yaml` 与 `application.yml` 中的默认值：

| 配置 | 默认值 | 说明 |
|---|---|---|
| `mock-payment.appid` | `wxmock000000000001` | 模拟公众号 |
| `mock-payment.mchid` | `1900000001` | 模拟商户号 |
| `mock-payment.public-base-url` | `http://localhost:9011` | 收银台地址前缀 |
| `mock-payment.api-v3-key` | `cloudmall-mock-payment-apiv3-key` | 32 字节 APIv3 密钥 |
| `mock-payment.platform-serial-no` | `MOCKPLATFORM20260101` | 平台证书序列号 |
| `mock-payment.signature.verify-request` | `false` | 是否校验 `/v3/**` 请求签名 |
| `mock-payment.notify.max-retry` | `5` | 回调最大重试次数 |
| `mock-payment.notify.retry-interval-ms` | `30000` | 重试扫描间隔 |
| `mock-payment.default-expire-minutes` | `15` | 默认支付有效期 |

order-service 的对应配置在 `docs/nacos/config/order-service.yaml` 的 `cloudmall.payment` 下。

## 数据表

- `cm_mock_payment_order`：模拟支付单（金额单位分，状态机 `NOTPAY → SUCCESS/CLOSED`）。
- `cm_mock_payment_notify`：回调投递记录（加密报文、签名、重试次数、下次重试时间、应答与错误）。

建表脚本见 `docs/sql/cm_mall/01-schema.sql`。

## 设计取舍

- **金额单位**：渠道协议统一使用分；order-service 在创建支付时由元转分，回调/查询时由分转元，避免浮点误差。
- **幂等**：同一 `out_trade_no` 重复创建返回原支付单；支付、关单使用条件更新，避免并发重复迁移；回调处理先查本地订单状态，已支付直接应答成功。
- **补偿**：回调可能丢失，`GET /orders/payment/status` 和 `GET /orders/payment/{orderNo}` 会主动查询渠道，发现 `SUCCESS` 后补发订单支付事件。
- **未实现**：退款、账单下载、平台证书轮换、应答签名（本服务只对回调签名）。这些不影响“下单/查询/回调”主链路，后续可按同一套 `WechatPayCrypto` 扩展。

## 参考文档

- [JSAPI 下单](https://pay.weixin.qq.com/doc/v3/merchant/4012791856)
- [Native 下单](https://pay.weixin.qq.com/doc/v3/merchant/4012791877)
- [商户订单号查询订单](https://pay.weixin.qq.com/doc/v3/merchant/4012526919)
- [关闭订单](https://pay.weixin.qq.com/doc/v3/merchant/4012791860)
- [支付成功回调通知](https://pay.weixin.qq.com/doc/v3/merchant/4012791861)
- [如何验证签名](https://pay.weixin.qq.com/doc/v3/merchant/4013053420)
- [如何解密回调报文](https://pay.weixin.qq.com/doc/v3/merchant/4012071382)

## 测试

```bash
mvn -pl service-api/payment-api,services/mock-payment-service,services/order-service -am \
  -Dtest=WechatPayCryptoTest,WechatPayServiceTest,WechatPayCallbackVerifierTest \
  -Dsurefire.failIfNoSpecifiedTests=false test
```

覆盖签名/验签、AES-GCM 加解密、支付单创建与幂等、过期关单、已支付关单拒绝、回调验签与篡改拒绝。
