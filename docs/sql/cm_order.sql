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
