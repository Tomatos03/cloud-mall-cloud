# Build & Dev Commands

## 模块结构

### 顶层模块

| 模块 | 说明 | 端口 | 启动类 |
|------|------|------|--------|
| `common` | 公共依赖（DTO、工具类） | - | - |
| `gateway` | Spring Cloud Gateway | 8080 | `GatewayApplication` |

### 业务服务

| 模块 | 说明 | 端口 | 启动类 |
|------|------|------|--------|
| `auth-service` | 认证服务 | 8081 | `AuthApplication` |
| `user-service` | 用户服务 | 8082 | `UserApplication` |
| `goods-service` | 商品服务 | 8083 | `GoodsApplication` |
| `order-service` | 订单服务 | 8084 | `OrderApplication` |
| `coupon-service` | 优惠券服务 | 8085 | `CouponApplication` |
| `seckill-service` | 秒杀服务 | 8086 | `SeckillApplication` |
| `im-service` | 即时通讯 | 7010 | `IMApplication` |
| `aigc-service` | AI Agent 服务 | 8087 | `AIGCApplication` |

### 服务接口

| 模块 | 说明 |
|------|------|
| `*-api` | Feign 接口 + DTO |

### 自动配置

| 模块 | 说明 |
|------|------|
| `cloudmall-mybatis-plus-spring-boot-starter` | MyBatis-Plus 自动配置 |

## 构建

```bash
# 全量
mvn clean compile -DskipTests
mvn clean package

# 单模块
mvn clean compile -pl services/auth-service -am
mvn clean compile -pl services/user-service -am
mvn clean compile -pl services/agent-service -am
mvn clean compile -pl gateway -am

# 快速确认编译
mvn clean compile -DskipTests 2>&1 | tail -20
```

## 测试

```bash
mvn test
mvn test -pl services/user-service
```

## 本地开发

### 启动依赖服务
```bash
docker compose up -d
```

### 启动应用
```bash
# 单模块启动
mvn -pl services/auth-service spring-boot:run
mvn -pl services/user-service spring-boot:run
mvn -pl services/agent-service spring-boot:run
mvn -pl gateway spring-boot:run
```

### 数据库
- 单库 cm_mall，业务表统一前缀 `cm_`，如 `cm_user`、`cm_role`、`cm_goods_*`、`cm_order_*`、`cm_address`、`cm_store` 等
- 初始化 SQL：`docs/sql/all/cm_mall.sql`
