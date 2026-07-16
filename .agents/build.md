# Build & Dev Commands

## 模块结构

| 模块 | 说明 | 端口 | 启动类 |
|------|------|------|--------|
| `gateway` | Spring Cloud Gateway | 8080 | - |
| `auth-service/auth-biz` | 认证服务 | 8081 | `AuthApplication` |
| `user-service/user-biz` | 用户服务 | 8082 | `UserApplication` |
| `coupon-service/coupon-biz` | 优惠券服务 | - | - |
| `goods-service/goods-biz` | 商品服务 | - | - |
| `seckill-service/seckill-biz` | 秒杀服务 | - | - |
| `order-service/order-biz` | 订单服务 | - | - |
| `im-service` | 即时通讯 | - | - |
| `api/*-api` | Feign 接口 + DTO | - | - |

## 构建

```bash
# 全量
mvn clean compile -DskipTests
mvn clean package

# 单模块
mvn clean compile -pl auth-service/auth-biz -am
mvn clean compile -pl user-service/user-biz -am
mvn clean compile -pl gateway -am

# 快速确认编译
mvn clean compile -DskipTests 2>&1 | tail -20
```

## 测试

```bash
mvn test
mvn test -pl user-service/user-biz
```

## 本地开发

### 启动依赖服务
```bash
docker compose -f docs/docker/docker-compose-infra.yml up -d
```

### 启动应用
```bash
# 单模块启动
mvn -pl auth-service/auth-biz spring-boot:run
mvn -pl user-service/user-biz spring-boot:run
mvn -pl gateway spring-boot:run
```

### 数据库
- 单库 cm_mall，所有表使用模块前缀：`cm_auth_*`、`cm_address`、`cm_store`、`cm_goods_*` 等
- 初始化 SQL：`docs/sql/all/cm_mall.sql`
