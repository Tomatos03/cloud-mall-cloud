# benchmark 目录

存放压测 / 实验辅助产物，不入库（`.gitignore` 忽略 `benchmark/`）。

## 子目录

- `seckill-purchase/`：秒杀 QPS 对比压测（异步 vs 普通，JMeter 脚本 +
  用户池 + 一键压测脚本 + README）。详见 `seckill-purchase/README.md`。
- `seckill-purchase-qps/`：秒杀接口 QPS 梯度扫描实验（并发递增找拐点）。
  详见 `seckill-purchase-qps/README.md`。

## 用户池 CSV 生成

`qps-users-async.csv` 与 `qps-users-plain.csv`（或 `seckill-purchase/`
下的 `seckill-users-*.csv`）：秒杀压测用户池（异步/普通方案各一份，
固定 `press_` 前缀）。由 `services/user-service` 的 JUnit 测试
`BatchRegisterAppTest`（IDEA 中可直接运行）生成：批量注册 NORMAL 用户 →
逐个登录 → 为每个用户创建收货地址 → 导出 CSV。

CSV 列（无表头，列序固定）：

| 列 | 说明 |
|----|------|
| `username` | 注册的用户名（`press_{序号}`） |
| `password` | 注册密码（默认 `123456`） |
| `token` | 登录返回的 JWT（`Authorization: Bearer {token}`） |
| `userId` | 注册返回的用户 ID |
| `addressId` | 为该用户创建的收货地址 ID（压测下单必填，普通方案校验归属） |
| `skuId` | 预分配 SKU（1/2/3 轮换，避免并发同 SKU 互相拦截） |

可通过系统属性覆盖（供压测脚本调用）：

| 属性 | 说明 |
|------|------|
| `-Dbatch.output.dir` | CSV 输出目录 |
| `-Dbatch.output.file` | CSV 输出文件名 |
| `-Dbatch.user.count` | 生成用户数量（默认 200） |
