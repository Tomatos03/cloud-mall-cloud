CREATE DATABASE IF NOT EXISTS cm_user DEFAULT CHARACTER SET utf8mb4;
USE cm_user;

CREATE TABLE `cm_address` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `user_id` bigint NOT NULL COMMENT '用户ID',
    `consignee` varchar(100) NOT NULL COMMENT '收货人',
    `phone` varchar(20) NOT NULL COMMENT '电话',
    `province` varchar(50) DEFAULT NULL,
    `city` varchar(50) DEFAULT NULL,
    `district` varchar(50) DEFAULT NULL,
    `detail` varchar(255) DEFAULT NULL,
    `zip_code` varchar(10) DEFAULT NULL,
    `is_default` tinyint(1) DEFAULT '0',
    `create_time` datetime DEFAULT NULL,
    `update_time` datetime DEFAULT NULL,
    `deleted` tinyint(1) DEFAULT '0',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `cm_store` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `user_id` bigint NOT NULL COMMENT '商家用户ID',
    `store_name` varchar(200) NOT NULL,
    `store_logo` varchar(500) DEFAULT NULL,
    `description` text,
    `status` tinyint(1) DEFAULT '1',
    `create_time` datetime DEFAULT NULL,
    `update_time` datetime DEFAULT NULL,
    `deleted` tinyint(1) DEFAULT '0',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
