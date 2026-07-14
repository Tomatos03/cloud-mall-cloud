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
