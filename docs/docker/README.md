# Docker 环境说明

## 目录结构

| 路径 | 说明 |
|------|------|
| `compose.yaml` | 全部基础设施容器编排 |
| `nginx/conf.d/default.conf` | nginx 入口配置（API / WebSocket / 前端） |
| `sh/sync-nacos-config.sh` | Nacos 配置批量发布脚本 |
| `sh/README.md` | Nacos 配置同步说明 |
| `es-plugins/` | Elasticsearch IK 分词插件离线包与加载说明 |

## 服务清单

| 服务 | 镜像 | 容器名 | 宿主机端口 | 数据卷 / 挂载 | 说明 |
|------|------|--------|-----------|---------------|------|
| MySQL | `mysql:8.4` | `cm-mysql` | 3306 | `mysql-data` + 初始化 SQL | 首次启动自动执行建库脚本与 seed 数据 |
| Redis | `redis:7` | `cm-redis` | 6379 | - | 缓存 / 限流计数 |
| Nacos | `nacos/nacos-server:v3.0.3` | `cm-nacos` | 8848 / 9848 / 8080 | - | 注册中心 + 配置中心；控制台见下方说明 |
| nacos-init | `curlimages/curl:latest` | `cm-nacos-init` | - | `../nacos/config` 只读挂载 | 一次性容器，启动时自动同步 Nacos 配置 |
| Sentinel Dashboard | `bladex/sentinel-dashboard:1.8.9` | `cm-sentinel-dashboard` | 8858 | - | 限流/熔断监控面板，只做监控，规则走 Nacos |
| nginx | `nginx:alpine` | `cm-nginx` | 80 | `./nginx/conf.d` 只读挂载 | 统一入口，代理到宿主机服务 |
| RocketMQ Namesrv | `apache/rocketmq:5.3.1` | `cm-rocketmq-namesrv` | 9876 | `rocketmq-namesrv-logs` | 消息队列命名服务 |
| RocketMQ Broker | `apache/rocketmq:5.3.1` | `cm-rocketmq-broker` | 10911 / 10909 / 8081 | `rocketmq-broker-logs`、`rocketmq-broker-store` | 消息队列 Broker，启用 Proxy |
| Elasticsearch | `docker.elastic.co/elasticsearch/elasticsearch:8.15.5` | `cm-elasticsearch` | 9200 / 9300 | `elasticsearch-data` + `./es-plugins/ik` | 商品搜索，启动即加载 IK 分词插件 |
| xxl-job-admin | `xuxueli/xxl-job-admin:3.4.2` | `cm-xxl-job-admin` | 8088 → 容器 8080 | - | 任务调度中心，依赖 MySQL 健康 |

## 启动与停止

```bash
# 启动全部基础设施
docker compose -f docs/docker/compose.yaml up -d

# 查看状态与日志
docker compose -f docs/docker/compose.yaml ps
docker compose -f docs/docker/compose.yaml logs -f

# 停止（保留数据卷）
docker compose -f docs/docker/compose.yaml down

# 停止并清空全部数据卷（不可恢复，慎用）
docker compose -f docs/docker/compose.yaml down -v
```

启动注意事项：

- `nacos-init` 会在 Nacos 就绪后自动把 `docs/nacos/config/` 下所有配置发布到 Nacos，然后退出（`restart: "no"`）。
- `xxl-job-admin` 等待 MySQL 健康后启动，`xxl_job` 库由 MySQL 初始化脚本自动创建。
- `rocketmq-broker` 等待 Namesrv 启动后再拉起。
- Elasticsearch 首次启动较慢，可用 `curl http://localhost:9200/_cluster/health` 确认。

## 配置初始化

### MySQL

首次创建容器时依次执行 `docs/sql/` 下的脚本：

1. `cm_mall/01-schema.sql` — 业务库表结构
2. `cm_mall/02-seed.sql` — seed 数据
3. `xxl_job/01-schema.sql` — xxl-job 调度库（容器内挂载为 `03-xxl_job-schema.sql`）

### Nacos 配置同步

配置源统一存放在 `docs/nacos/config/`（各服务 `*.yaml` + Sentinel 规则 `*.json`）。修改后两种方式生效：

```bash
# 方式一：手动同步到 localhost:8848
./docs/docker/sh/sync-nacos-config.sh localhost docs/nacos/config

# 方式二：重启一次性同步容器
docker compose -f docs/docker/compose.yaml restart nacos-init
```

### Sentinel 规则

`gateway-flow-rules.json`、`seckill-service-flow-rules.json`、`seckill-service-degrade-rules.json` 随同步脚本发布为 Nacos 配置，网关与 seckill-service 通过 Nacos 数据源动态加载。

- 规则修改**只改 Nacos 文件再同步**，不要在 Dashboard 上改，避免两套写入源互相覆盖。
- Dashboard（`http://localhost:8858`，默认账号 `sentinel/sentinel`）仅用于查看实时 QPS / 拒绝数 / RT。

## 请求链路

```
浏览器 / 客户端
  └─ http://localhost:80
       ├─ /users/**、/addresses/**、/product/**、/orders/** ...   → host.docker.internal:9000（宿主机 Gateway）→ 各微服务
       ├─ /chat/**（REST + WebSocket）                            → host.docker.internal:9000（Gateway，含 WS 升级头）
       └─ /（前端）                                               → host.docker.internal:9090（web 主商城 Vite Dev Server，含 HMR WS）
```

`nginx/conf.d/default.conf` 中：

- 前端三个应用（web/manager/merchant）共用同一套后端 API，API baseURL 统一为 `http://localhost`，由网关按路径前缀分发，无需应用前缀。
- API 前缀与 Gateway 的 Nacos 路由定义对齐（users、roles、resources、addresses、shops、favorites、coupons、merchant、operator、product、categories、banners、brands、comments、units、skus、seckill、orders、carts、search）。
- `/chat/` 单独配置 `Upgrade` / `Connection` 头以支持 WebSocket（`/chat/ws?token=xxx`），并延长 `proxy_read_timeout` 保证长连接不被掐断。
- 根路径转发到 web 主商城 Vite Dev Server（9090），`/` 与 HMR WebSocket 共用升级配置。

`host.docker.internal` 由 compose 的 `extra_hosts` 显式声明为 `host-gateway`，Linux 与 Docker Desktop 均可使用。

## 关键约定

- **容器命名统一 `cm-` 前缀**，便于识别。
- **MySQL 密码**：root/root（仅限本地开发）。
- **xxl-job accessToken**：`cloudmall-xxl-job`，必须与 `docs/nacos/config/common.yaml` 中 `xxl.job.access-token` 一致。
- **Nacos 鉴权**：`NACOS_AUTH_TOKEN` / `NACOS_AUTH_IDENTITY_*` 在 compose 中配置，业务服务直连 `localhost:8848`。
- **Elasticsearch 禁用 xpack 安全**，本地开发直接 HTTP 访问 9200。

## 常见问题

### nginx 转发不到宿主机服务

确认宿主机服务确实在监听，且端口与 `nginx/conf.d/default.conf` 一致（当前 Gateway 指向 9000，即 `gateway/src/main/resources/application.yml` 中的 `server.port`；若 Gateway 在 IDE 中以其他端口启动，需统一两处端口）。

### 修改网关路由/白名单后不生效

`docs/nacos/config/gateway.yaml` 修改后需同步到 Nacos（`./docs/docker/sh/sync-nacos-config.sh localhost docs/nacos/config` 或重启 `nacos-init`）。路由随 Nacos 配置热刷新（`refreshEnabled=true`）；但**白名单绑定在 AuthProperties（无 @RefreshScope），必须重启 gateway 才生效**。

### 端口被占用

`ss -ltnp | grep <端口>` 查看占用进程；本地开发常用端口 3306、6379、8848、9200、8858、8088、9876、80 均被 compose 占用。

### 想重置全部中间件数据

```bash
docker compose -f docs/docker/compose.yaml down -v
docker compose -f docs/docker/compose.yaml up -d
```

MySQL、ES、RocketMQ 数据与日志卷会被清空并重新初始化。

### Elasticsearch 无法加载 IK 分词

见 `es-plugins/README.md`，用 `curl http://localhost:9200/_cat/plugins` 验证插件，用 `_analyze` 接口验证分词效果。

### Nacos 控制台打不开

本地开发建议访问 `http://localhost:8080`（compose 映射）；配置 API 走 `http://localhost:8848`，脚本与业务服务均使用 8848。
