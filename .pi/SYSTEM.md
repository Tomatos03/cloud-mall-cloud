# cloud-mall-cloud

基于 **Spring Boot 3.5.7 + Spring Cloud 2025.0.0 + Spring Cloud Alibaba 2025.0.0.0** 的电商微服务项目。Java 17。

## 技术栈

| 类别 | 选型 |
|------|------|
| 注册中心 / 配置中心 | Nacos v3.0.3 |
| 网关 | Spring Cloud Gateway |
| 数据库 | MySQL 8.4 + MyBatis-Plus 3.5.14 |
| 缓存 | Redis 7 |
| 搜索引擎 | Elasticsearch 8.15.5 |
| 消息队列 | RocketMQ |
| AI | Spring AI 1.1.8（OpenAI + ES Vector Store + RAG） |
| DTO 转换 | MapStruct 1.6.3 |
| 工具库 | Hutool 5.8.42, Lombok |
| 反向代理 | Nginx |

## 模块结构

### 顶层模块

| 模块 | 说明 |
|------|------|
| `gateway` | Spring Cloud Gateway（依赖 Nacos 配置端口和路由） |
| `service-api` | Feign 接口 + DTO 聚合模块（父 POM） |
| `services` | 业务服务聚合模块（父 POM） |
| `starters` | 自研 Spring Boot Starter 聚合模块（父 POM） |

### 业务服务（`services/`）

| 模块 | 应用名 | 启动类 |
|------|--------|--------|
| `auth-service` | `auth-service` | `AuthApplication` |
| `user-service` | `user-service` | `UserApplication` |
| `product-service` | `product-service` | `ProductApplication` |
| `order-service` | `order-service` | `OrderApplication` |
| `coupon-service` | `coupon-service` | `CouponApplication` |
| `seckill-service` | `seckill-service` | `SeckillApplication` |
| `im-service` | `im-service` | `IMApplication` |
| `aigc-service` | `agent-service` | `AIGCApplication` |

> 服务端口统一在 Nacos 配置中心管理。`agent-service` 本地默认端口 8087。
> 所有服务通过 Nacos 进行服务发现和配置管理（导入 `common` + `{app-name}` 配置）。

### 服务接口（`service-api/`）

| 模块 | 说明 |
|------|------|
| `auth-api` | 认证 Feign 接口 + DTO |
| `user-api` | 用户 Feign 接口 + DTO |
| `product-api` | 商品 Feign 接口 + DTO |
| `order-api` | 订单 Feign 接口 + DTO |
| `coupon-api` | 优惠券 Feign 接口 + DTO |
| `seckill-api` | 秒杀 Feign 接口 + DTO |

### 自研 Starter（`starters/`）

| 模块 | 说明 |
|------|------|
| `cloudmall-common-spring-boot-starter` | 公共自动配置（`Result`、`BizException`、`BizError`、`UserContextFilter`） |
| `cloudmall-mybatis-plus-spring-boot-starter` | MyBatis-Plus 自动配置（MyMetaObjectHandler、SqlLoggingInterceptor） |
| `cloudmall-jwt-spring-boot-starter` | JWT 令牌生成与验证 |
| `cloudmall-feign-spring-boot-starter` | Feign 拦截器（用户上下文传递） |
| `cloudmall-context` | 用户上下文（`UserContext`、`UserContextHolder`） |
| `cloudmall-auth-api` | 认证 & RBAC 权限服务抽象层 |
| `cloudmall-security-rbac-spring-boot-starter` | RBAC 安全自动配置 |

## 本地开发

### 启动依赖

```bash
docker compose -f docs/docker/compose.yaml up -d
```

一键启动：MySQL 8.4、Redis 7、Nacos v3.0.3（含初始化配置）、Elasticsearch 8.15.5、Nginx。

### 构建

```bash
# 全量编译
mvn clean compile -DskipTests

# 单模块编译（含依赖）
mvn clean compile -pl services/auth-service -am

# 打包
mvn clean package
```

### 运行

```bash
# 方式一：Maven 插件
mvn -pl services/auth-service spring-boot:run

# 方式二：直接启动 jar
java -jar services/auth-service/target/auth-service-*.jar
```

### 测试

```bash
# 全量
mvn test

# 单模块
mvn test -pl services/user-service

# 快速编译验证
mvn clean compile -DskipTests 2>&1 | tail -20
```

## 代码风格

- 每次 `mvn` 编译通过之后，运行 `jstyle format` 执行一次格式化

## 数据库

- 单库 `cm_mall`
- 业务表统一前缀 `cm_`（如 `cm_user`、`cm_order`、`cm_role`、`cm_product_*`）
- 通用字段：`create_time`、`update_time`、`deleted`
- 关联表（如 `cm_user_role`）：仅 `create_time`
- 软删除：`deleted = 1`
- 启动自动初始化：`docs/sql/all/cm_mall.sql`

## API 规范

- RESTful Level 2，资源名使用复数（`/users`、`/orders`、`/addresses`）
- Controller 路径前缀与服务名对齐（`/auth/**`、`/users/**`、`/product/**`）
- 认证接口使用动词（`/auth/login`、`/auth/register`）
- 统一返回 `Result<T>`，业务异常使用 `BizException` + `BizError`

## 架构要点

- **Nacos**：所有服务通过 Nacos (localhost:8848) 进行服务注册发现和配置管理，导入 `common` + `{app-name}` 配置
- **JWT 无状态认证**：`cloudmall-jwt-spring-boot-starter` 生成/验证 Token（含 userId, username, userType）
- **Spring Security**：配置在 `SecurityConfig`，当前全放通，鉴权在网关层处理
- **Feign 拦截器**：`cloudmall-feign-spring-boot-starter` 在服务间调用时自动传递用户上下文
- **分页**：统一使用 MyBatis-Plus `Page`

## aigc-service 特性

AI Agent 服务（应用名 `agent-service`），集成 Spring AI 1.1.8：

- **大模型**：OpenAI（`spring-ai-starter-model-openai`）
- **向量存储**：Elasticsearch（索引 `cm-agent-knowledge`，1536 维，cosine 相似度）
- **RAG**：`RetrievalAugmentationAdvisor`、`QuestionAnswerAdvisor`
- **文档解析**：PDF、Markdown、Word/Excel/HTML（Tika Document Reader）
- **聊天记忆**：Redis
- **TTS 语音**：OpenAI TTS（`tts-1` 模型，`alloy` 音色）
- **DTO 转换**：MapStruct
- 项目内新增 `org/springframework/ai/` 目录包含 Spring AI 补丁类（`Tool`、`ToolParam`、`Message` 等）

## 错误码体系

错误码按模块分区：

| 范围 | 模块 |
|------|------|
| 1xxx | 通用错误（参数无效、数据不存在、超时等） |
| 10xxx | 用户认证（登录、注册、权限） |
| 20xxx | 商品分类（SPU/SKU、库存、规格） |
| 30xxx | 地址 |
| 40xxx | 搜索 |
| 45xxx | 轮播图 |
| 50xxx | 订单（创建、状态、评价、退款） |
| 60xxx | 购物车 |
| 70xxx | 商品评论 |
| 80xxx | 文件上传 |
| 85xxx | 资源 |
| 90xxx | 审核 |
| 91xxx | AI Agent（91010 会话不存在或已过期） |
| 92xxx | 秒杀 |
| 93xxx | 优惠券 |
| 94xxx | IM 聊天（94000 消息状态、94001 消息类型、94002 会话不存在、94003 消息内容为空、94005 非会话成员） |
| 500xx | 权限与店铺 |
