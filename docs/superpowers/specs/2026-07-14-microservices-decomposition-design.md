# Cloud Mall — 微服务拆分设计方案

> 基于 cloud-mall 单体项目重构为 Spring Cloud 微服务架构

## 1. 目标

将当前 Spring Boot 3 多模块单体电商项目拆分为完整的微服务架构，覆盖以下学习领域：

- 服务拆分方法论（按业务域切割）
- Spring Cloud Gateway 统一网关
- Nacos 服务注册发现与配置中心
- OpenFeign 服务间同步通信
- RocketMQ 异步解耦与事件驱动
- Sentinel 限流熔断降级
- Seata 分布式事务
- SkyWalking 链路追踪
- Docker Compose / K8s 容器化部署

## 2. 仓库与目录结构

单仓库 (Mono-repo)，新建项目目录：

```
/home/Tomatos/Projects/GithubProjects/cloud-mall-cloud/
```

### 顶层结构

```
cloud-mall-cloud/
├── pom.xml                          ← 父 POM（聚合所有模块）
├── common/                          ← 公共基础设施 jar
├── api/                             ← 统一 DTO + Feign 接口 jar
├── gateway/                         ← Spring Cloud Gateway
├── auth-service/                    ← 认证授权服务
├── user-service/                    ← 用户 + 地址 + RBAC 服务
├── goods-service/                   ← 商品 + 分类 + 搜索 + 评论服务
├── order-service/                   ← 订单 + 购物车服务
├── coupon-service/                  ← 优惠券服务
├── seckill-service/                 ← 秒杀活动 + 审核服务
├── im-service/                      ← 即时通讯服务（从原项目迁移）
├── docs/                            ← 文档
│   ├── docker/                      ← Docker Compose 文件
│   ├── sql/                         ← 建表脚本
│   └── superpowers/specs/           ← 设计文档
```

## 3. 模块职责与端口分配

| 模块 | 端口 | 职责说明 | 数据存储 |
|------|------|---------|---------|
| `gateway` | 8080 | 路由转发、JWT token 校验、限流 | 无 |
| `auth-service` | 8081 | 登录/注册、JWT 颁发、角色权限查询 | cm_auth |
| `user-service` | 8082 | 用户管理、地址管理、店铺信息、RBAC 体系 | cm_user |
| `goods-service` | 8083 | 商品 SPU/SKU/规格、分类、单位、评论、搜索(ES)、收藏、横幅 | cm_goods + ES |
| `order-service` | 8084 | 订单流程、购物车、统计 | cm_order |
| `coupon-service` | 8085 | 优惠券领券/核销 | cm_coupon |
| `seckill-service` | 8086 | 秒杀活动/商品、审核流程 | cm_seckill |
| `im-service` | 8087 | 即时通讯、客服聊天 | cm_im |

## 4. 模块依赖关系

```
gateway  → 无（纯路由转发，@Operation 绕过）
     │
     │ 路由到各个服务
     ▼
各服务  →  common（基础设施 jar）
       →  api（DTO + Feign 接口 jar）
```

### 服务间同步调用 (OpenFeign)

```
order-service ──Feign──→ user-service（查用户地址/店铺）
order-service ──Feign──→ goods-service（查商品/扣库存）
order-service ──Feign──→ coupon-service（核销优惠券）
order-service ──Feign──→ seckill-service（校验秒杀资格）
seckill-service ──Feign──→ goods-service（查秒杀商品信息）
```

### 服务间异步事件 (RocketMQ)

| 事件 | 发布者 | 消费者 | 说明 |
|------|--------|--------|------|
| 订单已创建 | order | goods | 异步扣减库存 |
| 订单已支付 | order | seckill | 更新秒杀结果 |
| 订单已支付 | order | coupon | 标记优惠券已使用 |
| 订单超时取消 | order (自消费) | order | RocketMQ 延时消息释放库存 |
| 评价已提交 | order | goods | 更新商品评分 |

## 5. 关键技术栈

| 类别 | 技术 | 版本 |
|------|------|------|
| 框架 | Spring Boot 3 + Spring Cloud 2024.x | 3.5.7 |
| 注册配置 | Nacos | latest |
| 网关 | Spring Cloud Gateway | - |
| RPC | OpenFeign + Spring Cloud LoadBalancer | - |
| 流量治理 | Sentinel | - |
| 分布式事务 | Seata (AT) | - |
| 链路追踪 | SkyWalking | - |
| 消息 | RocketMQ | 2.3.5（已有） |
| 数据库 | MySQL + MyBatis-Plus | 8.x |
| 缓存 | Redis | - |
| 搜索 | Elasticsearch | 8.18.8 |
| 文件 | MinIO | - |
| 容器化 | Docker Compose → K8s | - |

## 6. 架构流程图

```
                         ┌─────────────────────────────────┐
                         │   Spring Cloud Gateway :8080    │
                         │  路由 /web/** /manager/** /merchant/** │
                         └──────┬──────────┬───────────────┘
                                │          │
                    ┌───────────┴──────────┴───────────────┐
                    │         Nacos (注册+配置中心)          │
                    └───────────┬──────────┬───────────────┘
                                │          │
          ┌─────────┬───────────┼──────────┼──────────┬──────────┐
          │         │           │          │          │          │
          ▼         ▼           ▼          ▼          ▼          ▼
      ┌────────┐ ┌────────┐ ┌────────┐ ┌────────┐ ┌─────────┐ ┌────────┐
      │ Auth   │ │ User   │ │ Goods  │ │ Order  │ │ Coupon  │ │Seckill │
      │:8081   │ │:8082   │ │:8083   │ │:8084   │ │:8085    │ │:8086   │
      └───┬────┘ └────────┘ └───┬────┘ └───┬────┘ └────┬───┘ └───┬────┘
          │                     │          │           │          │
          └─────────────────────┴──────────┴───────────┴──────────┘
                                        │
          ┌─────────────────────────────┼──────────────────────────────┐
          │                             │                              │
          ▼                             ▼                              ▼
    ┌──────────┐                 ┌───────────┐                 ┌───────────┐
    │  MySQL   │                 │   Redis   │                 │     ES    │
    │ (各服务  │                 │ (缓存)    │                 │ (搜索)    │
    │  独立DB) │                 └───────────┘                 └───────────┘
    └──────────┘

    Sentinel Dashboard (流量治理)
    Seata Server (分布式事务)
    SkyWalking (链路追踪)
    RocketMQ (消息)
    MinIO (文件存储)
```

## 7. 微服务基础设施 — 学习阶段

| Phase | 内容 | 可验证成果 |
|-------|------|-----------|
| 1 | Nacos 单机（Docker）+ 配置中心 | 服务启动注册到 Nacos，从 Nacos 读取配置 |
| 2 | 7 个服务 + 依赖注册 Nacos，Gateway 统一路由 | `/web/goods/1` 经 Gateway 路由到 goods-service |
| 3 | Sentinel 限流 + Seata 分布式事务 | 下单场景跨 order + coupon + goods 的事务保护 |
| 4 | SkyWalking 链路追踪 | 调用链可视化展现 |
| 5 | Docker Compose 全栈编排 | `docker compose up` 一键启动全栈 |
| 6 | K8s 部署（可选） | Minikube/K3s 集群部署 |

## 8. 服务代码结构（以 order-service 为例）

```
order-service/
├── pom.xml
└── src/main/java/com/cloudmall/order/
    ├── OrderApplication.java
    ├── controller/
    │   ├── OrderController.java
    │   └── CartController.java
    └── service/
        ├── IOrderService.java
        └── impl/
            └── OrderService.java
```

业务逻辑尽量从当前 `cloud-mall-framework` 的对应 domain 包**直接搬迁**，保持原有设计不重构。

## 9. common 模块内容

```
common/
├── entity/                  ← CommonDO, Result, PageResult
├── enums/                   ← BizErrorCode（通用业务码）
├── exception/               ← BizException
├── utils/                   ← AssertUtils, BeanContext, AuthUserContext
├── config/                  ← Jackson 配置, MyBatisPlus 分页配置
├── context/                 ← 请求上下文工具
└── mq/                      ← RocketMQ 通用消息结构（可选）
```

不含任何业务 DTO 或 Feign 接口，纯基础设施。

## 10. 命名约定

统一使用 Request/Response/Client 模型，明确网络边界：

| 层 | 命名 | 示例 |
|----|------|------|
| Controller 请求体 | `XxxRequest` | `OrderCreateRequest` |
| Controller / Feign 响应 | `XxxResponse` | `OrderResponse` |
| Feign 接口（远程调用客户端） | `XxxClient` | `OrderClient`（不绑定 Feign，可替换实现） |
| Service 内部 | Entity 直接传递 | `OrderDO`（MyBatis-Plus 实体） |

优点：
- 意图自文档化：`OrderCreateRequest` 一看就是请求，`OrderResponse` 一看就是响应数据
- 防止内部字段泄露：VO 只包含前端需要的展示字段（脱敏、转描述），Request 只包含下单需要的参数
- 与 SpringDoc/Swagger 亲和，接口文档自动生成清晰

## 11. api 模块内容

```
api/
├── auth/
│   ├── request/             ← LoginRequest, RegisterRequest
│   ├── response/            ← LoginResponse, UserInfoResponse
│   └── client/              ← AuthClient
├── user/
│   ├── request/             ← AddressCreateRequest, AddressUpdateRequest
│   ├── response/            ← UserResponse, AddressResponse, StoreResponse
│   └── client/              ← UserClient
├── goods/
│   ├── request/             ← GoodsCreateRequest, GoodsSearchRequest
│   ├── response/            ← GoodsResponse, SkuResponse, CategoryResponse
│   └── client/              ← GoodsClient
├── order/
│   ├── request/             ← OrderCreateRequest, OrderQueryRequest
│   ├── response/            ← OrderResponse, CartResponse
│   └── client/              ← OrderClient
├── coupon/
│   ├── request/             ← CouponClaimRequest
│   ├── response/            ← CouponResponse
│   └── client/              ← CouponClient
└── seckill/
    ├── request/             ← SeckillGoodsAuditRequest
    ├── response/            ← SeckillActivityResponse, SeckillGoodsResponse
    └── client/              ← SeckillClient
```

按包按领域分组，后续可按模块独立抽出为独立 `-api` jar。

## 12. 数据库策略

每个微服务使用独立的数据库实例或独立的 schema：

```
cm_auth      ← auth-service
cm_user      ← user-service
cm_goods     ← goods-service
cm_order     ← order-service
cm_coupon    ← coupon-service
cm_seckill   ← seckill-service
cm_im        ← im-service
```

跨服务不允许直接访问对方数据库，只能通过 API 通信。分布式事务使用 Seata AT 模式保护。
