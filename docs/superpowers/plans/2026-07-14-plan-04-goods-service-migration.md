# Plan 4: Goods Service 迁移

**Goal:** 商品服务独立为 goods-service，包含商品 SPU/SKU/规格、分类、单位、评论、横幅、收藏。

**Architecture:** goods-service 独立端口 8083，独立数据库 cm_goods。被 order-service（查商品/扣库存）和 seckill-service（查秒杀商品）通过 Feign 调用。

**Tech Stack:** Spring Boot 3.5.7, MyBatis-Plus, MySQL, Nacos, Redis

## Global Constraints

- Java 17, Maven, Spring Boot 3.5.7 parent, Cloud 2025.0.0 + Alibaba 2025.0.0.0
- Request/Response/Client 命名, No wildcard import, 4 space indent
- MyBatis-Plus 3.5.14, Hutool 5.8.42

---
### Task 1: goods-service 模块脚手架

**Files:** `goods-service/pom.xml`, `goods-api/pom.xml`, `goods-biz/pom.xml`
- Same pattern as user-service / coupon-service
- Root pom.xml: add `<module>goods-service</module>` + `goods-api` to `<dependencyManagement>`

### Task 2: goods-api 合同

**Files (create in `goods-service/goods-api/.../`):**

`request/GoodsSearchRequest.java`:
```java
package com.cloudmall.goods.api.request;
import lombok.Data;
@Data
public class GoodsSearchRequest {
    private Long categoryId;
    private String keyword;
    private Integer page = 1;
    private Integer pageSize = 20;
    private String sortBy; // price_asc, price_desc, sales, newest
}
```

`response/GoodsResponse.java`:
```java
package com.cloudmall.goods.api.response;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;
@Data
public class GoodsResponse {
    private Long id;
    private String name;
    private String image;
    private BigDecimal price;
    private Long categoryId;
    private String categoryName;
    private Integer salesCount;
    private Integer stock;
    private String status;
    private List<String> images;
    private String description;
}
```

`response/CategoryResponse.java`:
```java
package com.cloudmall.goods.api.response;
import lombok.Data;
import java.util.List;
@Data
public class CategoryResponse {
    private Long id;
    private String name;
    private Long parentId;
    private Integer sortOrder;
    private List<CategoryResponse> children;
}
```

`response/CommentResponse.java`:
```java
package com.cloudmall.goods.api.response;
import lombok.Data;
import java.time.LocalDateTime;
@Data
public class CommentResponse {
    private Long id;
    private Long userId;
    private String username;
    private String content;
    private Integer rating;
    private LocalDateTime createTime;
}
```

`response/BannerResponse.java`:
```java
package com.cloudmall.goods.api.response;
import lombok.Data;
@Data
public class BannerResponse {
    private Long id;
    private String title;
    private String image;
    private String linkUrl;
    private Integer sortOrder;
}
```

`client/GoodsClient.java`:
```java
package com.cloudmall.goods.api.client;
import com.cloudmall.common.entity.Result;
import com.cloudmall.goods.api.response.GoodsResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "goods-service", path = "/goods")
public interface GoodsClient {
    @GetMapping("/{id}")
    Result<GoodsResponse> getById(@PathVariable("id") Long id);

    @PostMapping("/stock/deduct")
    Result<Void> deductStock(@RequestParam("skuId") Long skuId, @RequestParam("quantity") Integer quantity);

    @PostMapping("/stock/rollback")
    Result<Void> rollbackStock(@RequestParam("skuId") Long skuId, @RequestParam("quantity") Integer quantity);
}
```

### Task 3: DDL + Config

**File:** `docs/sql/cm_goods.sql`
```sql
CREATE DATABASE IF NOT EXISTS cm_goods DEFAULT CHARACTER SET utf8mb4;
USE cm_goods;

CREATE TABLE `cm_category` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `name` varchar(100) NOT NULL,
    `parent_id` bigint DEFAULT '0',
    `level` tinyint DEFAULT '1',
    `icon` varchar(500) DEFAULT NULL,
    `sort_order` int DEFAULT '0',
    `status` tinyint DEFAULT '1',
    `create_time` datetime DEFAULT NULL,
    `update_time` datetime DEFAULT NULL,
    `deleted` tinyint DEFAULT '0',
    PRIMARY KEY (`id`),
    KEY `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `cm_goods` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `name` varchar(200) NOT NULL,
    `image` varchar(500) DEFAULT NULL,
    `images` text COMMENT 'JSON数组',
    `description` text,
    `category_id` bigint DEFAULT NULL,
    `brand` varchar(100) DEFAULT NULL,
    `price` decimal(10,2) NOT NULL,
    `market_price` decimal(10,2) DEFAULT NULL,
    `stock` int DEFAULT '0',
    `sales_count` int DEFAULT '0',
    `status` varchar(20) DEFAULT 'OFF' COMMENT 'ON/OFF',
    `create_time` datetime DEFAULT NULL,
    `update_time` datetime DEFAULT NULL,
    `deleted` tinyint DEFAULT '0',
    PRIMARY KEY (`id`),
    KEY `idx_category` (`category_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `cm_goods_sku` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `goods_id` bigint NOT NULL,
    `spec_ids` varchar(200) DEFAULT NULL COMMENT '规格组合JSON',
    `image` varchar(500) DEFAULT NULL,
    `price` decimal(10,2) NOT NULL,
    `stock` int DEFAULT '0',
    `create_time` datetime DEFAULT NULL,
    `update_time` datetime DEFAULT NULL,
    `deleted` tinyint DEFAULT '0',
    PRIMARY KEY (`id`),
    KEY `idx_goods_id` (`goods_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `cm_goods_spec` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `goods_id` bigint NOT NULL,
    `name` varchar(50) NOT NULL COMMENT '规格名(如颜色)',
    `value` varchar(100) NOT NULL COMMENT '规格值(如红色)',
    `sort_order` int DEFAULT '0',
    PRIMARY KEY (`id`),
    KEY `idx_goods_id` (`goods_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `cm_comment` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `goods_id` bigint NOT NULL,
    `user_id` bigint NOT NULL,
    `order_id` bigint DEFAULT NULL,
    `content` text,
    `rating` tinyint DEFAULT '5',
    `images` text COMMENT 'JSON',
    `status` tinyint DEFAULT '1',
    `create_time` datetime DEFAULT NULL,
    PRIMARY KEY (`id`),
    KEY `idx_goods_id` (`goods_id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `cm_banner` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `title` varchar(200) DEFAULT NULL,
    `image` varchar(500) NOT NULL,
    `link_url` varchar(500) DEFAULT NULL,
    `sort_order` int DEFAULT '0',
    `status` tinyint DEFAULT '1',
    `create_time` datetime DEFAULT NULL,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `cm_favorite` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `user_id` bigint NOT NULL,
    `goods_id` bigint NOT NULL,
    `create_time` datetime DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_goods` (`user_id`,`goods_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `cm_unit` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `name` varchar(50) NOT NULL COMMENT '单位名(件/个/斤)',
    `status` tinyint DEFAULT '1',
    `sort_order` int DEFAULT '0',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

**File:** `goods-service/goods-biz/src/main/resources/application.yml`
```yaml
server:
  port: 8083
spring:
  application:
    name: goods-service
  datasource:
    url: jdbc:mysql://localhost:3306/cm_goods?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai
    username: root
    password: root123
  data:
    redis:
      host: localhost
      port: 6379
mybatis-plus:
  mapper-locations: classpath:mapper/*.xml
  type-aliases-package: com.cloudmall.goods.entity
  global-config:
    db-config:
      id-type: auto
      logic-delete-field: deleted
      logic-delete-value: 1
      logic-not-delete-value: 0
spring.cloud.nacos.discovery.server-addr: localhost:8848
```

### Task 4: goods-biz — Category + Banner

Entities (6 DOs → CategoryDO, GoodsDO, GoodsSkuDO, GoodsSpecDO, CommentDO, BannerDO, FavoriteDO, UnitDO)

Mappers for each.

Services:
- `ICategoryService` — list tree, get by id
- `IBannerService` — list active, sorted
- `IUnitService` — list all

Controllers at `/category/**`, `/banner/**`, `/unit/**`

**CategoryService:**
```java
public interface ICategoryService {
    List<CategoryResponse> listTree();
    CategoryResponse getById(Long id);
}
```
Implement by selecting all non-deleted categories, building tree in memory.

**BannerService:**
```java
public interface IBannerService {
    List<BannerResponse> listActive();
}
```

### Task 5: goods-biz — Goods + Sku + Spec + Stock

- `IGoodsService` — query by id, search (basic), list by category
- Stock deduct + rollback (supports order-service/seckill-service via GoodsClient)
- Sku query by goods id

**GoodsService core methods:**
```java
GoodsResponse getById(Long id);
List<GoodsResponse> listByCategory(Long categoryId, int page, int size);
Boolean deductStock(Long skuId, Integer quantity);
Boolean rollbackStock(Long skuId, Integer quantity);
```

### Task 6: goods-biz — Comment + Favorite

- `ICommentService` — list by goods id, create
- `IFavoriteService` — add/remove/list by user

Controllers at `/comment/**`, `/favorite/**`

### Task 7: Gateway 路由

Add to gateway:
```yaml
        - id: goods-service
          uri: lb://goods-service
          predicates:
            - Path=/goods/**,/category/**,/banner/**,/comment/**,/favorite/**,/unit/**
```
