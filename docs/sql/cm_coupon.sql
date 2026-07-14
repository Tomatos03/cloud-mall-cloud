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
