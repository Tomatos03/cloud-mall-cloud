# Plan 6: Order Service 迁移

**Goal:** 订单 + 购物车独立为 order-service，协调 user/goods/coupon/seckill 服务。

**Architecture:** order-service 端口 8084，数据库 cm_order。通过 Feign 调用 user-service（地址）、goods-service（商品/库存）、coupon-service（优惠券）、seckill-service（秒杀资格）。

**Tech Stack:** Spring Boot 3.5.7, MyBatis-Plus, MySQL, Nacos, Redis, RocketMQ

## Global Constraints
Standard.

---
### Task 1: order-service 模块脚手架
Standard scaffold pattern. Root pom: add module + order-api dependencyManagement.
order-biz deps: order-api, common, nacos, mysql, mybatis-plus, redis, **rocketmq-spring-boot-starter**

### Task 2: order-api 合同
**files:** OrderCreateRequest, OrderResponse, CartResponse, OrderClient

**OrderCreateRequest.java:**
```java
package com.cloudmall.order.api.request;
import lombok.Data;
import java.util.List;
@Data
public class OrderCreateRequest {
    private Long userId;
    private Long addressId;
    private List<OrderItemRequest> items;
    private Long couponId;       // optional
    private Long seckillId;      // optional (if from seckill)
    private String remark;
    @Data
    public static class OrderItemRequest {
        private Long goodsId;
        private Long skuId;
        private Integer quantity;
        private String goodsName;
    }
}
```

**OrderResponse.java:** id, orderNo, totalAmount, status, createTime, items

**CartResponse.java:** id, goodsId, skuId, goodsName, image, price, quantity, selected

**OrderClient.java:**
```java
@FeignClient(name = "order-service", path = "/order")
public interface OrderClient {
    @GetMapping("/{id}")
    Result<OrderResponse> getById(@PathVariable Long id);
    @GetMapping("/list")
    Result<List<OrderResponse>> listByUser(@RequestParam Long userId);
}
```

### Task 3: DDL + Config
**docs/sql/cm_order.sql:**
```sql
CREATE DATABASE IF NOT EXISTS cm_order DEFAULT CHARACTER SET utf8mb4;
USE cm_order;
CREATE TABLE `cm_order` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `order_no` varchar(64) NOT NULL,
    `user_id` bigint NOT NULL,
    `total_amount` decimal(10,2) NOT NULL,
    `status` varchar(20) DEFAULT 'PENDING' COMMENT 'PENDING/PAID/SHIPPED/DELIVERED/CANCELLED',
    `address_snapshot` text COMMENT '地址快照JSON',
    `remark` varchar(500) DEFAULT NULL,
    `create_time` datetime DEFAULT NULL,
    `update_time` datetime DEFAULT NULL,
    `deleted` tinyint DEFAULT '0',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_no` (`order_no`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `cm_order_item` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `order_id` bigint NOT NULL,
    `goods_id` bigint NOT NULL,
    `sku_id` bigint DEFAULT NULL,
    `goods_name` varchar(200) DEFAULT NULL,
    `goods_image` varchar(500) DEFAULT NULL,
    `price` decimal(10,2) NOT NULL,
    `quantity` int NOT NULL,
    `subtotal` decimal(10,2) NOT NULL,
    PRIMARY KEY (`id`),
    KEY `idx_order_id` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `cm_cart` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `user_id` bigint NOT NULL,
    `goods_id` bigint NOT NULL,
    `sku_id` bigint DEFAULT NULL,
    `goods_name` varchar(200) DEFAULT NULL,
    `goods_image` varchar(500) DEFAULT NULL,
    `price` decimal(10,2) NOT NULL,
    `quantity` int NOT NULL DEFAULT '1',
    `selected` tinyint DEFAULT '1',
    `create_time` datetime DEFAULT NULL,
    `update_time` datetime DEFAULT NULL,
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

**application.yml:** port 8084, cm_order, Redis, Nacos, RocketMQ

### Task 4: order-biz — Entities + Mappers
Entities: OrderDO, OrderItemDO, CartDO
Mappers: OrderMapper, OrderItemMapper, CartMapper

### Task 5: order-biz — Cart Service
CartController + CartService: add, update quantity, delete, list by user. Redis for performance.

### Task 6: order-biz — Order Service (Core)
The main flow:
1. Create order → validate user/address via UserClient, validate goods via GoodsClient, validate coupon via CouponClient, validate seckill via SeckillClient
2. Deduct stock via GoodsClient.deductStock
3. Mark coupon used via CouponClient.markUsed
4. Save order + items to DB
5. Send RocketMQ event "order.created" for async processing

**OrderServiceImpl:**
```java
@Service @RequiredArgsConstructor
public class OrderServiceImpl implements IOrderService {
    public OrderResponse createOrder(OrderCreateRequest request) {
        // 1. Generate order number
        String orderNo = generateOrderNo();
        // 2. Calculate total
        BigDecimal total = calculateTotal(request.getItems());
        // 3. Deduct stock via GoodsClient
        for (var item : request.getItems()) {
            goodsClient.deductStock(item.getSkuId(), item.getQuantity());
        }
        // 4. Handle coupon
        if (request.getCouponId() != null) {
            couponClient.verifyCoupon(request.getCouponId(), request.getUserId());
            couponClient.markUsed(request.getCouponId());
        }
        // 5. Save order
        OrderDO order = new OrderDO();
        order.setOrderNo(orderNo);
        order.setUserId(request.getUserId());
        order.setTotalAmount(total);
        order.setStatus("PENDING");
        order.setRemark(request.getRemark());
        orderMapper.insert(order);
        // 6. Send MQ event
        // rocketMQTemplate.send("order-created", order.getId());
        return getById(order.getId());
    }
}
```

### Task 7: Gateway 路由
Add /order/**, /cart/** to gateway.
