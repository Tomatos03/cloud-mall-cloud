# Plan 3: Coupon Service 迁移

**Goal:** 优惠券服务独立为 coupon-service，支持领券、核销、查询。

**Architecture:** coupon-service 独立端口 8085，独立数据库 cm_coupon。被 order-service 通过 Feign 调用（核销优惠券）。

**Tech Stack:** Spring Boot 3.5.7, MyBatis-Plus, MySQL, Nacos

## Global Constraints

- Java 17, Maven, Spring Boot 3.5.7 parent
- Spring Cloud 2025.0.0 + Alibaba 2025.0.0.0
- Request/Response/Client 命名约定
- No wildcard imports, 4 space indent

---
### Task 1: coupon-service 模块脚手架

**Files:**
- Create: `coupon-service/pom.xml` (aggregator)
- Create: `coupon-service/coupon-api/pom.xml`
- Create: `coupon-service/coupon-biz/pom.xml`
- Modify: `pom.xml` — add `<module>coupon-service</module>` + `coupon-api` to `<dependencyManagement>`

**coupon-service/pom.xml:**
```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" ...>
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>com.cloudmall</groupId>
        <artifactId>cloud-mall-cloud</artifactId>
        <version>1.0-SNAPSHOT</version>
    </parent>
    <artifactId>coupon-service</artifactId>
    <packaging>pom</packaging>
    <modules>
        <module>coupon-api</module>
        <module>coupon-biz</module>
    </modules>
</project>
```

**coupon-service/coupon-api/pom.xml:**
```xml
<?xml version="1.0" encoding="UTF-8"?>
<project ...>
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>com.cloudmall</groupId>
        <artifactId>cloud-mall-cloud</artifactId>
        <version>1.0-SNAPSHOT</version>
        <relativePath>../../pom.xml</relativePath>
    </parent>
    <artifactId>coupon-api</artifactId>
    <packaging>jar</packaging>
    <dependencies>
        <dependency>
            <groupId>com.cloudmall</groupId>
            <artifactId>common</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-openfeign</artifactId>
        </dependency>
        <dependency>
            <groupId>jakarta.validation</groupId>
            <artifactId>jakarta.validation-api</artifactId>
        </dependency>
    </dependencies>
</project>
```

**coupon-service/coupon-biz/pom.xml:**
```xml
<?xml version="1.0" encoding="UTF-8"?>
<project ...>
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>com.cloudmall</groupId>
        <artifactId>cloud-mall-cloud</artifactId>
        <version>1.0-SNAPSHOT</version>
        <relativePath>../../pom.xml</relativePath>
    </parent>
    <artifactId>coupon-biz</artifactId>
    <packaging>jar</packaging>
    <dependencies>
        <dependency>
            <groupId>com.cloudmall</groupId>
            <artifactId>coupon-api</artifactId>
            <version>1.0-SNAPSHOT</version>
        </dependency>
        <dependency>
            <groupId>com.cloudmall</groupId>
            <artifactId>common</artifactId>
        </dependency>
        <dependency>
            <groupId>com.alibaba.cloud</groupId>
            <artifactId>spring-cloud-starter-alibaba-nacos-discovery</artifactId>
        </dependency>
        <dependency>
            <groupId>com.mysql</groupId>
            <artifactId>mysql-connector-j</artifactId>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>com.baomidou</groupId>
            <artifactId>mybatis-plus-spring-boot3-starter</artifactId>
            <version>${mybatis-plus-version}</version>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-redis</artifactId>
        </dependency>
    </dependencies>
    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
```

- [ ] Create 3 POMs + directories
```bash
mkdir -p coupon-service/coupon-api/src/main/java/com/cloudmall/coupon/api/{request,response,client}
mkdir -p coupon-service/coupon-biz/src/main/java/com/cloudmall/coupon/{controller,service/impl,entity,mapper,utils}
mkdir -p coupon-service/coupon-biz/src/main/resources
```
- [ ] Update root `pom.xml` (module + dependencyManagement)
- [ ] `mvn clean compile -DskipTests` → BUILD SUCCESS
- [ ] Commit

---

### Task 2: coupon-api 合同

**Files:**
- Create: `coupon-service/coupon-api/.../request/CouponClaimRequest.java`
- Create: `coupon-service/coupon-api/.../response/CouponResponse.java`
- Create: `coupon-service/coupon-api/.../client/CouponClient.java`

**CouponClaimRequest.java:**
```java
package com.cloudmall.coupon.api.request;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
@Data
public class CouponClaimRequest {
    @NotNull private Long couponId;
    @NotNull private Long userId;
}
```

**CouponResponse.java:**
```java
package com.cloudmall.coupon.api.response;
import lombok.Data;
import java.time.LocalDateTime;
@Data
public class CouponResponse {
    private Long id;
    private String name;
    private String type;        // FULL_REDUCTION, DISCOUNT, CASH
    private BigDecimal threshold;
    private BigDecimal discount;
    private LocalDateTime expireTime;
    private Boolean claimed;    // 当前用户是否已领取
}
```

**CouponClient.java:**
```java
package com.cloudmall.coupon.api.client;
import com.cloudmall.common.entity.Result;
import com.cloudmall.coupon.api.response.CouponResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "coupon-service", path = "/coupon")
public interface CouponClient {
    @PostMapping("/verify")
    Result<CouponResponse> verifyCoupon(@RequestParam("couponId") Long couponId,
                                         @RequestParam("userId") Long userId);
    @PostMapping("/use/{id}")
    Result<Void> markUsed(@PathVariable("id") Long id);
}
```

- [ ] Create 3 files + compile → BUILD SUCCESS + commit

---

### Task 3: DDL + Config

**File:** `docs/sql/cm_coupon.sql`
```sql
CREATE DATABASE IF NOT EXISTS cm_coupon DEFAULT CHARACTER SET utf8mb4;
USE cm_coupon;
CREATE TABLE `cm_coupon` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `name` varchar(100) NOT NULL COMMENT '优惠券名称',
    `type` varchar(20) NOT NULL COMMENT 'FULL_REDUCTION/DISCOUNT/CASH',
    `threshold` decimal(10,2) DEFAULT NULL COMMENT '满减门槛',
    `discount` decimal(10,2) NOT NULL COMMENT '优惠金额/折扣',
    `total_count` int DEFAULT '0' COMMENT '发行总量',
    `claimed_count` int DEFAULT '0' COMMENT '已领取数量',
    `status` tinyint(1) DEFAULT '1' COMMENT '0=禁用 1=启用',
    `start_time` datetime DEFAULT NULL,
    `expire_time` datetime DEFAULT NULL,
    `create_time` datetime DEFAULT NULL,
    `update_time` datetime DEFAULT NULL,
    `deleted` tinyint(1) DEFAULT '0',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE `cm_user_coupon` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `user_id` bigint NOT NULL,
    `coupon_id` bigint NOT NULL,
    `status` varchar(20) DEFAULT 'UNUSED' COMMENT 'UNUSED/USED/EXPIRED',
    `claimed_time` datetime DEFAULT NULL,
    `used_time` datetime DEFAULT NULL,
    `order_id` bigint DEFAULT NULL,
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_coupon_id` (`coupon_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

**File:** `coupon-service/coupon-biz/src/main/resources/application.yml`
```yaml
server:
  port: 8085
spring:
  application:
    name: coupon-service
  datasource:
    url: jdbc:mysql://localhost:3306/cm_coupon?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai
    username: root
    password: root123
  data:
    redis:
      host: localhost
      port: 6379
mybatis-plus:
  mapper-locations: classpath:mapper/*.xml
  type-aliases-package: com.cloudmall.coupon.entity
  global-config:
    db-config:
      id-type: auto
      logic-delete-field: deleted
      logic-delete-value: 1
      logic-not-delete-value: 0
spring.cloud.nacos.discovery.server-addr: localhost:8848
```

- [ ] Create 2 files + commit

---

### Task 4: coupon-biz 实现

**Files:**
- Create: `CouponApplication.java`
- Create: `entity/CouponDO.java`
- Create: `entity/UserCouponDO.java`
- Create: `mapper/CouponMapper.java`
- Create: `mapper/UserCouponMapper.java`
- Create: `service/ICouponService.java`
- Create: `service/impl/CouponServiceImpl.java`
- Create: `controller/CouponController.java`

**CouponApplication.java:** @SpringBootApplication @EnableDiscoveryClient, port from yml

**CouponDO.java:** @TableName("cm_coupon"), fields match DDL

**UserCouponDO.java:** @TableName("cm_user_coupon"), fields match DDL

**CouponMapper.java:** extends BaseMapper<CouponDO>

**UserCouponMapper.java:** extends BaseMapper<UserCouponDO>

**ICouponService.java:**
```java
public interface ICouponService {
    List<CouponResponse> listAvailable();
    CouponResponse getById(Long id);
    Boolean claim(CouponClaimRequest request);
    CouponResponse verifyCoupon(Long couponId, Long userId);
    void markUsed(Long id);
}
```

**CouponServiceImpl.java:** 
- `listAvailable()` — select enabled + not expired
- `claim()` — check stock + not already claimed → insert user_coupon + increment claimed_count (Redis Lua for atomicity)
- `verifyCoupon()` — check user_coupon exists and status UNUSED
- `markUsed()` — update user_coupon status to USED

**CouponController.java:** REST at `/coupon/**`
- `GET /list` → listAvailable
- `GET /{id}` → getById
- `POST /claim` → claim
- `POST /verify` → verifyCoupon
- `POST /use/{id}` → markUsed

- [ ] Create all files + compile → BUILD SUCCESS + commit

---

### Task 5: Gateway 路由

**File:** `gateway/src/main/resources/application.yml`

Add route:
```yaml
        - id: coupon-service
          uri: lb://coupon-service
          predicates:
            - Path=/coupon/**
```
- [ ] compile + commit
