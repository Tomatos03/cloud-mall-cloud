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
