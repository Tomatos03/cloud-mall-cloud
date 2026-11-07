# Cloud Mall Cloud

基于 **Spring Boot 3.5.7 + Spring Cloud 2025.0.0 + Spring Cloud Alibaba 2025.0.0.0** 的电商微服务平台，Java 17，Maven 多模块构建。

## 技术栈

| 类别 | 技术 | 版本 |
|------|------|------|
| 基础框架 | Spring Boot | 3.5.7 |
| 微服务 | Spring Cloud | 2025.0.0 |
| 微服务（阿里） | Spring Cloud Alibaba | 2025.0.0.0 |
| 注册中心/配置中心 | Nacos | v3.0.3 |
| 网关 | Spring Cloud Gateway | - |
| 数据库 | MySQL | 8.4 |
| ORM | MyBatis-Plus | 3.5.14 |
| 缓存 | Redis | 7 |
| 搜索引擎 | Elasticsearch | 8.15.5 |
| 消息队列 | RocketMQ | 5.3.1 |
| AI 能力 | Spring AI | 1.1.8 |
| DTO 转换 | MapStruct | 1.6.3 |
| 工具库 | Hutool / Lombok | 5.8.42 / - |
| 反向代理 | Nginx | Alpine |
| 任务调度 | XXL-Job | 3.4.2 |

## 模块结构

```
cloud-mall-cloud/
├── common/                   # 共享工具与跨服务对象
├── gateway/                  # API 网关（Spring Cloud Gateway）
├── service-api/              # Feign 接口 + DTO 聚合（父 POM）
│   ├── user-api
│   ├── product-api
│   ├── order-api
│   ├── coupon-api
│   ├── seckill-api
│   └── search-api
├── services/                 # 业务服务（父 POM）
│   ├── user-service          # 认证/RBAC + 用户资料
│   ├── product-service       # 商品管理
│   ├── order-service         # 订单管理
│   ├── coupon-service        # 优惠券
│   ├── seckill-service       # 秒杀
│   ├── im-service            # 即时通讯
│   ├── aigc-service          # AI 生成内容（agent-service）
│   ├── search-service        # 搜索服务
│   └── consumer-service      # 消费者服务
├── starters/                 # 自研 Spring Boot Starter
│   ├── cloudmall-common      # Result/BizException/BizErrorEnum/UserContextFilter
│   ├── cloudmall-mybatis-plus# MyMetaObjectHandler/PageReq/LimitEnum/StatusEnum
│   ├── cloudmall-jwt         # JWT 认证
│   ├── cloudmall-rocketmq    # MQProducer/TxMessageHeader
│   ├── cloudmall-outbox      # 本地消息 Outbox
│   ├── cloudmall-feign       # 用户上下文透传 + Result 自动拆箱
│   ├── cloudmall-elasticsearch # ES 集成
│   └── cloudmall-xxl-job     # XXL-Job 集成
└── docs/                     # 设计文档 / Docker Compose / Nacos 配置 / SQL
```

## 快速启动

### 1. 启动基础设施

```bash
docker compose -f docs/docker/compose.yaml up -d
```

启动以下容器（容器名统一 `cm-` 前缀）：

| 服务 | 宿主机端口 | 说明 |
|------|-----------|------|
| MySQL | 3306 | 业务库 `cm_mall`，首次启动自动执行建库脚本与 seed 数据 |
| Redis | 6379 | 缓存/限流 |
| Nacos | 8848 / 8080 | 注册中心 + 配置中心，控制台 `http://localhost:8080` |
| Sentinel Dashboard | 8858 | 限流/熔断监控（默认账号 `sentinel/sentinel`） |
| Elasticsearch | 9200 | 商品搜索，自动加载 IK 分词插件 |
| RocketMQ | 9876 / 10911 | 消息队列 |
| XXL-Job Admin | 8088 | 任务调度中心 |
| Nginx | 80 | 统一入口 |

### 2. 编译项目

```bash
mvn clean compile -DskipTests
```

### 3. 启动单个服务

```bash
mvn clean compile -pl services/user-service -am
mvn -pl services/user-service spring-boot:run
```

### 4. 停止基础设施

```bash
docker compose -f docs/docker/compose.yaml down       # 保留数据
docker compose -f docs/docker/compose.yaml down -v     # 清空数据（慎用）
```

## 服务端口

所有服务端口统一在 Nacos 配置中心管理（导入 `common` + `{app-name}` 配置即可）。

## 架构要点

- **Nacos** (localhost:8848)：服务注册发现 + 配置管理
- **Spring Cloud Gateway**：统一入口，路由分发，JWT 鉴权
- **JWT 无状态认证**：携带 userId / username / userType
- **Feign 拦截器**：自动透传用户上下文到下游服务
- **请求链路**：`Nginx(:80)` → `Gateway(:9000)` → 各微服务

## API 规范

- RESTful Level 2：资源名复数（`/users`），HTTP 方法表达操作（GET 查 / POST 建 / PUT 改 / DELETE 删）
- 认证接口使用动词（`/auth/login`）
- 统一返回 `Result<T>`，业务异常使用 `BizException` + `BizErrorEnum`

## 对象模型与目录边界

业务服务内的对象按职责分为四类，**目录与命名必须遵循下表**：

```
services/{service}/
├── controller/          # 只依赖 model/req、model/resp、service
├── application/         # 用例编排
├── service/impl/        # 业务规则、事务
├── mapper/              # MyBatis-Plus
├── model/
│   ├── entity/          # XxxDO：与数据库表一一对应
│   ├── req/             # XxxReq：本服务 HTTP 入参
│   ├── resp/            # XxxResp：本服务 HTTP 出参
│   └── bo/              # 领域对象：Service 内部聚合对象
├── convert/             # MapStruct 转换
└── enums/
```

| 目录 | 后缀 | 职责 | 允许出现的位置 |
|---|---|---|---|
| `model/entity/` | `XxxDO` | 持久化映射，字段即表列 | Mapper、Service 内部 |
| `model/req/` | `XxxReq` | 本服务 HTTP 入参 | Controller、Service |
| `model/resp/` | `XxxResp` | 本服务 HTTP 出参 | Controller、Service |
| `model/bo/` | 领域名（不加 `BO` 后缀） | 多表聚合、跨领域组合、复杂计算结果 | Service、Application 内部 |

边界规则：

- **DO 不出 Service 层**：Controller 签名、Feign 接口、`service-api` 中禁止出现 `XxxDO`。
- **Req/Resp 只服务本服务 HTTP 边界**：跨服务调用的入参/出参放 `service-api` 对应模块，不在本服务 `model` 中重复定义。
- **BO 只在 Service/Application 内部流转**：禁止作为 Controller 入参/出参，禁止被 `service-api` 引用。
- **转换统一走 `convert/XxxConverter`**（MapStruct）：DO ↔ BO ↔ Req/Resp，禁止在 Controller / Service 内联手写转换。
- **没有聚合需求不建 BO**：BO 用于多表聚合或跨领域组合，不要为每张表机械地造一个 BO。
- **BO 使用领域名，不强制 `BO` 后缀**：如 `CheckoutContext`、`ProductSkuPrice`；仅在与其它层对象重名时加限定词区分。

## 数据库约定

- 单库 `cm_mall`，业务表统一前缀 `cm_`
- 通用字段：`create_time` / `update_time` / `deleted`
- 软删除：`deleted = 1`
- SQL 脚本：`docs/sql/cm_mall/01-schema.sql`（建表）/ `02-seed.sql`（种子数据）

## 常用命令

```bash
# 启动本地依赖
docker compose -f docs/docker/compose.yaml up -d

# 全量编译
mvn clean compile -DskipTests

# 单模块编译（含依赖）
mvn clean compile -pl services/user-service -am

# 打包
mvn clean package

# 运行
mvn -pl services/user-service spring-boot:run

# 测试
mvn test

# Nacos 配置同步
./docs/docker/sh/sync-nacos-config.sh localhost docs/nacos/config

# 代码格式化（修改文件后必须执行）
jstyle format
```

## 错误码分区

| 范围 | 模块 |
|------|------|
| 1xxx | 通用 |
| 10xxx | 认证 |
| 20xxx | 商品 |
| 30xxx | 地址 |
| 40xxx | 搜索 |
| 45xxx | 轮播图 |
| 50xxx | 订单 |
| 60xxx | 购物车 |
| 70xxx | 评论 |
| 80xxx | 文件 |
| 85xxx | 资源 |
| 90xxx | 审核 |
| 91xxx | AI Agent |
| 92xxx | 秒杀 |
| 93xxx | 优惠券 |
| 94xxx | IM |
| 500xx | 权限与店铺 |

## 文档

- [Docker 环境说明](docs/docker/README.md)
- [设计文档](docs/design/README.md)

## License

Private — All rights reserved.
