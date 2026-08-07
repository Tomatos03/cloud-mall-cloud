# cloudmall-xxl-job-spring-boot-starter

XXL-JOB 分布式任务调度接入 starter，提供执行器自动装配与任务启动自动注册能力。基于 **xxl-job 3.4.2**（调度中心与执行器同版本，admin 为 Spring Boot 3 + JDK17 构建）。

## 功能

### 1. 执行器自动装配

`XxlJobAutoConfiguration`：从 `xxl.job.*` 配置装配 `XxlJobSpringExecutor` Bean（启动 netty 接收调度触发、与调度中心心跳注册）。

`@XxlJob` 注解扫描由 xxl-job 自身完成，业务侧只需在任意 Spring Bean 方法上标注：

```java
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderOutboxJobHandler {

    @XxlJob("orderOutboxPublish")
    public void publish(String param) {
        // 任务逻辑
    }
}
```

### 2. 启动自动注册（幂等）

`XxlJobAutoRegister`（`ApplicationRunner`）：启动时调用调度中心 REST API，按声明式配置注册任务。

- 流程：登录调度中心 → 确保执行器组存在（`/jobgroup/insert`，自动注册模式）→ 按 handler 查询（`/jobinfo/pageList`）→ 缺失则新增（`/jobinfo/insert`，BEAN 模式）并启动（`/jobinfo/start`）
- 幂等：任务已存在则跳过；重复启动不产生副作用
- 开关：`xxl.job.auto-register.enabled=false` 可关闭，改用手工在管理台注册

## 配置项

```yaml
xxl:
  job:
    enabled: true                  # 总开关，false 不装配执行器（默认 true）
    admin:
      addresses: http://localhost:8088   # 调度中心地址，多个逗号分隔（v3 admin context-path 为 /，不带 /xxl-job-admin 后缀）
      username: admin              # 调度中心登录账号（自动注册使用）
      password: "123456"           # 调度中心登录密码（自动注册使用）
    access-token: cloudmall-xxl-job  # 执行器与调度中心通信令牌，两端必须一致
    executor:
      app-name: order-service      # 执行器 AppName，对应管理台执行器
      ip: host.docker.internal     # 注册 IP（开发环境 admin 在容器内回连宿主机）
      port: 9995                   # 执行器端口
      log-path: /tmp/xxl-job/jobhandler
      log-retention-days: 30
    auto-register:
      enabled: true
      jobs:                        # 任务定义列表（key 为 @XxlJob handler 名）
        - handler: orderOutboxPublish
          desc: 订单Outbox事件发布
          schedule-type: FIX_DELAY
          schedule-conf: "5"
```

`jobs` 任务定义未声明的字段取 starter 默认值（与项目任务约定一致）：

| 字段 | 默认值 | 说明 |
|------|--------|------|
| `schedule-type` | `FIX_DELAY` | 调度类型：CRON / FIX_RATE / FIX_DELAY |
| `schedule-conf` | `60` | CRON 表达式或秒数 |
| `misfire-strategy` | `DO_NOTHING` | 错过调度不追补（任务均幂等） |
| `executor-route-strategy` | `FIRST` | 单机路由 |
| `executor-block-strategy` | `SERIAL_EXECUTION` | 单机串行，禁止覆盖杀线程 |
| `executor-timeout` | `0` | 不限制（全表扫描任务宁慢勿断） |
| `author` | `cloudmall` | 负责人 |

## 项目任务清单

| handler | 服务 | 调度类型 | 配置 | 路由 | 阻塞 |
|---------|------|----------|------|------|------|
| `authOutboxPublish` | auth-service | FIX_DELAY | 5s | FIRST | SERIAL |
| `orderOutboxPublish` | order-service | FIX_DELAY | 5s | FIRST | SERIAL |
| `orderTimeoutRecovery` | order-service | FIX_DELAY | 60s | FIRST | SERIAL |
| `couponLockExpire` | coupon-service | FIX_DELAY | 60s | FIRST | SERIAL |
| `userCouponExpire` | coupon-service | CRON | `0 */5 * * * ?` | FIRST | SERIAL |
| `searchProductIncrementalSync` | search-service | FIX_DELAY | 60s | FIRST | SERIAL |
| `searchProductFullRebuild` | search-service | CRON | `0 0 3 * * ?` | FIRST | SERIAL |
## 基础设施

- 调度中心：`docker compose` 中 `xxl-job-admin:3.4.2`，宿主端口 `8088`（默认 8080 已被 Nacos 占用），默认账号 `admin/123456`
- 数据库：`xxl_job` 库由 `docs/sql/xxl_job/01-schema.sql` 在 mysql 容器首次初始化时创建（挂载为 `03-xxl_job-schema.sql`）；已有 mysql-data 卷的环境需 `docker compose down -v` 重建或手动执行该 SQL
- 执行器端口映射（与各服务 HTTP 端口尾号对齐，落在 9990 段）：auth-service `9992`、coupon-service `9993`、order-service `9995`、search-service `9999`

## 注意事项

- **admin 是调度单点**：调度中心不可用时，Outbox 任务（5s 轮询）会暂停发布，事件堆积在 outbox 表，admin 恢复后自动追平；批处理任务同理延后。数据不丢失。
- **`xxl.job.*` 只在启动时读取一次**（executor 启动 netty 并注册），Nacos 动态刷新对该配置无效，修改后必须重启服务。
- **accessToken 两端同步**：`common.yaml` 的 `xxl.job.access-token` 必须与 compose 中 admin 的 `--xxl.job.accessToken` 一致，改一处必须同步另一处。
- **`executor.ip: host.docker.internal` 是开发环境取值**（admin 容器回连宿主机执行器），生产环境按实际网络覆盖。
- **自动注册依赖 admin 可用**：服务启动时 admin 未就绪会导致注册失败（执行器心跳仍会持续重试），此时需重启服务或手工在管理台注册。

## 依赖关系

```
cloudmall-xxl-job-spring-boot-starter
├── xxl-job-core（执行器运行时）
├── hutool-http / hutool-json（自动注册调用调度中心 REST API）
└── spring-boot-starter
```
