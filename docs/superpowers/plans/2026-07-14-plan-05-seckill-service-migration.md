# Plan 5: Seckill Service 迁移

**Goal:** 秒杀活动 + 审核独立为 seckill-service。

**Architecture:** seckill-service 独立端口 8086，独立数据库 cm_seckill。调用 goods-service 获取商品信息。

**Tech Stack:** Spring Boot 3.5.7, MyBatis-Plus, MySQL, Nacos, Redis

## Global Constraints
Standard (Java 17, Request/Response/Client, No wildcard imports, etc.)

---
### Task 1: seckill-service 模块脚手架
**Files:** seckill-service/pom.xml, seckill-api/pom.xml, seckill-biz/pom.xml
Standard scaffold pattern. Root pom.xml: add module + dependencyManagement.

### Task 2: seckill-api 合同
**request/SeckillActivitySearchRequest.java**, **response/SeckillActivityResponse.java**, **client/SeckillClient.java**

**SeckillClient.java:**
```java
@FeignClient(name = "seckill-service", path = "/seckill")
public interface SeckillClient {
    @GetMapping("/activity/{id}")
    Result<SeckillActivityResponse> getActivity(@PathVariable("id") Long id);
    @PostMapping("/goods/verify")
    Result<Boolean> verifyGoods(@RequestParam("activityId") Long activityId, @RequestParam("goodsId") Long goodsId);
}
```

### Task 3: DDL + Config
```sql
CREATE DATABASE IF NOT EXISTS cm_seckill DEFAULT CHARACTER SET utf8mb4;
USE cm_seckill;
CREATE TABLE `cm_seckill_activity` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `name` varchar(200) NOT NULL,
    `start_time` datetime NOT NULL,
    `end_time` datetime NOT NULL,
    `status` varchar(20) DEFAULT 'DRAFT' COMMENT 'DRAFT/PENDING/APPROVED/REJECTED/FINISHED',
    `create_user` bigint DEFAULT NULL,
    `create_time` datetime DEFAULT NULL,
    `update_time` datetime DEFAULT NULL,
    `deleted` tinyint DEFAULT '0',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `cm_seckill_goods` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `activity_id` bigint NOT NULL,
    `goods_id` bigint NOT NULL,
    `seckill_price` decimal(10,2) NOT NULL,
    `stock` int DEFAULT '0',
    `sold_count` int DEFAULT '0',
    `audit_status` varchar(20) DEFAULT 'PENDING' COMMENT 'PENDING/APPROVED/REJECTED',
    PRIMARY KEY (`id`),
    KEY `idx_activity` (`activity_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

**application.yml:** port 8086, cm_seckill, Redis, Nacos.

### Task 4: seckill-biz 实现
Entities: SeckillActivityDO, SeckillGoodsDO
Mappers, Services, Controllers

Core flow:
- Activity CRUD (manager creates activities)
- Goods submit for seckill (merchant submits goods to activity)
- Audit flow (manager approves/rejects)
- SeckillClient used by order-service during checkout

### Task 5: Gateway 路由
Add `/seckill/**` route.
