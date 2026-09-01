# JMeter 秒杀 QPS 对比测试配置

秒杀异步化 vs 普通流程的 QPS 对比压测配置。测试前置：先运行
`services/seckill-service` 的 `SeckillPressDataInitTest` 初始化压测活动
（ID 900001，RUNNING，3 个 SKU），再运行 `services/user-service` 的
`BatchRegisterAppTest` 生成压测用户池 CSV（含真实收货地址）。

## 文件说明

| 文件 | 说明 |
|------|------|
| `seckill-async.jmx` | 异步方案（`POST /seckill/purchase`）一次性并发模板：每线程抢 1 次，loop=1 |
| `seckill-plain.jmx` | 普通方案（`POST /seckill/plain/purchase`）一次性并发模板：每线程抢 1 次，loop=1 |
| `seckill-async-cont.jmx` | 异步方案持续压测模板：loop=-1 + 固定时长（默认 30s），测服务端吞吐上限 |
| `seckill-plain-cont.jmx` | 普通方案持续压测模板：loop=-1 + 固定时长（默认 30s） |
| `seckill-users-async.csv` | 异步方案用户池（`press_` 前缀），由 `BatchRegisterAppTest` 生成 |
| `seckill-users-plain.csv` | 普通方案用户池（`press_` 前缀），由 `BatchRegisterAppTest` 生成 |
| `run.sh` | 自动化压测脚本：重建活动 → 生成双用户池 → 交替压异步/普通 → 汇总 |

## 关键配置说明

### 请求链路
- 走网关 `http://localhost:9000`（必须，网关负责把 JWT 解析为 `X-Auth-User` 头）
- 认证：`Authorization: Bearer ${token}`（token 来自 CSV）
- 请求体由 JSR223 PreProcessor 动态构造（`addNonEncodedArgument`，避免 JMeter 对 JSON 做 URL 编码）：
  ```json
  {"activityId":900001,"skuId":${skuId},"addressId":${addressId}}
  ```
- 断言：响应包含 `"code":0` 视为成功（业务拒绝如 92031 一人一单、92003 库存不足不算成功）

### CSV 列
`username,password,token,userId,addressId,skuId`（无表头，jmx 以 variableNames 声明列名）
- `skuId` 由 `BatchRegisterAppTest` 按序号轮换（1/2/3），保证并发时不同线程抢不同 SKU，避免一人一单互相拦截
- `addressId` 为每个用户真实创建的收货地址（普通方案 Feign 校验地址归属，缺失会报 50020）

### 限流（压测前必须处理）
- Sentinel `seckill:purchase` 与网关 `/seckill/**` 默认 QPS 200，压测前调高到 5000
  （Nacos `seckill-service-flow-rules` / `gateway-flow-rules`），否则测的是限流器而非架构差异
- 用户级固定窗口限流（1 分钟 10 次）在异步方案存在，压测用多用户轮换绕开

### 一人一单约束
- 秒杀「一人一单」：每用户每 SKU 只能成功 1 次
- 一次性并发模板：并发数 ≤ 用户池大小（async/plain 各 200 用户）
- 持续压测模板：成功数被用户池耗尽后，QPS 统计的是服务端处理能力（含快速拒绝）

## 使用方法

### 一键压测（自动重新生成数据）
```bash
./run.sh 100 3 1 500 30
# 参数: [并发数] [轮数] [是否重新生成数据 1/0] [用户数] [持续时长s]
```
脚本自动完成：
1. `mvn test` 执行 `SeckillPressDataInitTest` —— 重建秒杀活动 900001
   （RUNNING，2 小时窗口，避免活动过期）+ 预热 Redis 库存
2. `mvn test` 执行 `BatchRegisterAppTest` 生成用户池（固定前缀 `press_`，
   `${USER_COUNT}` 个用户，登录 + 创建收货地址，CSV 含 skuId 轮换列），
   一次生成后复制为 async/plain 两份（两方案用户池内容一致，省一次
   Spring Boot 启动）
3. 交替压测异步/普通方案，汇总以表格列展示

**两种模式**（由第 5 个参数 `[持续时长s]` 决定）：
- `0`（默认）：**一次性模板**（loop=1，每线程抢 1 次）——请求数 = 并发数，
  测**响应延迟**；QPS 反映客户端发送节奏而非服务端吞吐
- `>0`（如 30）：**持续压测模板**（loop=-1 + 固定时长）——服务端持续满负荷，
  QPS = 总请求 / 实际时长，测**服务端真实吞吐**（异步化收益在此显现）

> 用户数也可单独覆盖：测试类支持 `-Dbatch.user.count` 系统属性（如
> `mvn test -Dtest=BatchRegisterAppTest -Dbatch.user.count=500`）。
>
> CSV 具有时效性（token 会过期、用户会积累订单），建议每次压测都带
> 参数 `1` 重新生成；`0` 表示沿用现有 CSV 快速压测。
>
> 注意：mvn 需要在项目根执行，脚本已自动处理工作目录切换；
> 生成依赖本地环境（Nacos/MySQL/Redis/RocketMQ 在线）。

### 一次性并发对比（测响应延迟）
```bash
./run.sh 100 3 0     # 100 并发，3 轮；每线程 1 次
```

### 持续压测（测服务端吞吐，推荐）
```bash
./run.sh 100 3 0 200 30   # 100 并发持续 30s，3 轮；QPS 才是服务端处理能力
```

### 重置数据（压测脚本自动执行，手动场景用）
```sql
-- MySQL
DELETE FROM cm_seckill_order WHERE activity_id=900001;
DELETE FROM cm_order WHERE order_no LIKE 'SK%';
```
```bash
# Redis（清两种一人一单标记 + 重置库存）
redis-cli --scan --pattern 'seckill:user:900001:*' | xargs redis-cli DEL
redis-cli --scan --pattern 'seckill:plain:user:*' | xargs redis-cli DEL
redis-cli MSET seckill:stock:900001:1 1000000 seckill:stock:900001:2 1000000 seckill:stock:900001:3 1000000
```

## 实测参考（本机 30s 持续压测）

| 并发 | 指标 | 异步 | 普通 | 提升 |
|------|------|------|------|------|
| 50 | 总 QPS | 2854 | 1910 | +49% |
| 100 | 总 QPS | 2843 | 1854 | +53% |
| 100 | 成功 P95 | 179ms | 534ms | 快 66% |

异步方案吞吐比普通高约 50%，延迟约为普通的一半；差异来自异步化把「建真实订单」
移出请求链路（Redis 快速拦截 + 落本地秒杀单 + MQ 异步建单），而普通方案在请求内
同步 Feign 查 SKU/地址并写 3 张订单表。
