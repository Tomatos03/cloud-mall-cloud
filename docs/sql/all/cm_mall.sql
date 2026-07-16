-- ============================================================
-- cm_mall — 共享数据库
-- 包含所有服务模块的表结构
-- ============================================================

CREATE DATABASE IF NOT EXISTS cm_mall DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE cm_mall;

-- ============================================================
-- 认证模块 (auth)
-- ============================================================

CREATE TABLE cm_auth_user
(
    id         bigint auto_increment comment '用户ID'
        primary key,
    username   varchar(50)                                          not null comment '用户名',
    password   varchar(255)                                         not null comment '用户密码（bcrypt 哈希）',
    nickname   varchar(100)                                         null comment '用户昵称',
    phone      varchar(30)                                          null comment '手机号',
    email      varchar(100)                                         null comment '电子邮箱',
    bio        varchar(255)                                         null comment '个人简介',
    avatar_url varchar(500)                                         null comment '用户头像URL',
    types      set ('NORMAL', 'ADMIN', 'MERCHANT') default 'NORMAL' null comment '用户类型',
    store_id   bigint                                               null comment '关联店铺ID',
    constraint uk_username
        unique (username)
) comment '用户认证表' engine = InnoDB
                       collate = utf8mb4_unicode_ci;

CREATE TABLE cm_auth_role
(
    id          bigint auto_increment comment '角色ID'
        primary key,
    name        varchar(50)                          not null comment '角色名称',
    built_in    tinyint(1) default 0                 not null comment '是否为内置角色(不可删除)',
    enable      tinyint(1) default 1                 null comment '是否启用',
    description varchar(255)                         null comment '角色描述',
    create_time datetime   default CURRENT_TIMESTAMP null comment '创建时间',
    constraint uk_role_name
        unique (name)
) comment '系统角色表' engine = InnoDB
                      collate = utf8mb4_unicode_ci;

CREATE TABLE cm_auth_user_role
(
    user_id     bigint                             not null comment '用户ID',
    role_id     bigint                             not null comment '角色ID',
    create_time datetime default CURRENT_TIMESTAMP null comment '创建时间',
    primary key (user_id, role_id),
    constraint fk_auth_user_role_user
        foreign key (user_id) references cm_auth_user (id)
            on delete cascade,
    constraint fk_auth_user_role_role
        foreign key (role_id) references cm_auth_role (id)
            on delete cascade
) comment '用户与角色关联表' engine = InnoDB
                            collate = utf8mb4_unicode_ci;

CREATE INDEX idx_auth_user_role_role_id ON cm_auth_user_role (role_id);

-- ============================================================
-- 用户模块 (user)
-- ============================================================

CREATE TABLE cm_address (
    id         bigint       NOT NULL AUTO_INCREMENT comment '地址ID',
    user_id    bigint       NOT NULL comment '用户ID',
    consignee  varchar(100) NOT NULL comment '收货人',
    phone      varchar(20)  NOT NULL comment '联系电话',
    province   varchar(50)  DEFAULT NULL comment '省份',
    city       varchar(50)  DEFAULT NULL comment '城市',
    district   varchar(50)  DEFAULT NULL comment '区县',
    detail     varchar(255) DEFAULT NULL comment '详细地址',
    zip_code   varchar(10)  DEFAULT NULL comment '邮政编码',
    is_default tinyint(1)   DEFAULT '0' comment '是否默认地址(0=否 1=是)',
    create_time datetime    DEFAULT NULL comment '创建时间',
    update_time datetime    DEFAULT NULL comment '更新时间',
    deleted    tinyint(1)   DEFAULT '0' comment '是否删除(0=否 1=是)',
    PRIMARY KEY (id),
    KEY idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 comment '用户收货地址表';

CREATE TABLE cm_store (
    id          bigint        NOT NULL AUTO_INCREMENT comment '店铺ID',
    user_id     bigint        NOT NULL comment '商家用户ID',
    store_name  varchar(200)  NOT NULL comment '店铺名称',
    store_logo  varchar(500)  DEFAULT NULL comment '店铺Logo URL',
    description text          DEFAULT NULL comment '店铺描述',
    status      tinyint(1)    DEFAULT '1' comment '店铺状态(0=关闭 1=营业)',
    create_time datetime      DEFAULT NULL comment '创建时间',
    update_time datetime      DEFAULT NULL comment '更新时间',
    deleted     tinyint(1)    DEFAULT '0' comment '是否删除(0=否 1=是)',
    PRIMARY KEY (id),
    KEY idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 comment '商家店铺表';

-- ============================================================
-- 商品模块 (goods)
-- ============================================================

CREATE TABLE cm_category (
    id         bigint       NOT NULL AUTO_INCREMENT comment '分类ID',
    name       varchar(100) NOT NULL comment '分类名称',
    parent_id  bigint       DEFAULT '0' comment '父分类ID(0=顶级分类)',
    level      tinyint      DEFAULT '1' comment '分类层级(1=一级 2=二级 3=三级)',
    icon       varchar(500) DEFAULT NULL comment '分类图标URL',
    sort_order int          DEFAULT '0' comment '排序值(越小越靠前)',
    status     tinyint      DEFAULT '1' comment '是否启用(0=禁用 1=启用)',
    create_time datetime    DEFAULT NULL comment '创建时间',
    update_time datetime    DEFAULT NULL comment '更新时间',
    deleted    tinyint      DEFAULT '0' comment '是否删除(0=否 1=是)',
    PRIMARY KEY (id),
    KEY idx_parent_id (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 comment '商品分类表';

CREATE TABLE cm_goods (
    id           bigint        NOT NULL AUTO_INCREMENT comment '商品ID',
    name         varchar(200)  NOT NULL comment '商品名称',
    image        varchar(500)  DEFAULT NULL comment '商品主图URL',
    images       text          DEFAULT NULL comment '商品详情图(JSON数组)',
    description  text          DEFAULT NULL comment '商品描述',
    category_id  bigint        DEFAULT NULL comment '所属分类ID',
    brand        varchar(100)  DEFAULT NULL comment '品牌名称',
    price        decimal(10,2) NOT NULL comment '销售价格',
    market_price decimal(10,2) DEFAULT NULL comment '市场价(划线价)',
    stock        int           DEFAULT '0' comment '库存数量',
    sales_count  int           DEFAULT '0' comment '累计销量',
    status       varchar(20)   DEFAULT 'OFF' comment '商品状态(ON=上架 OFF=下架)',
    create_time  datetime      DEFAULT NULL comment '创建时间',
    update_time  datetime      DEFAULT NULL comment '更新时间',
    deleted      tinyint       DEFAULT '0' comment '是否删除(0=否 1=是)',
    PRIMARY KEY (id),
    KEY idx_category (category_id),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 comment '商品信息表';

CREATE TABLE cm_goods_sku (
    id        bigint        NOT NULL AUTO_INCREMENT comment 'SKU ID',
    goods_id  bigint        NOT NULL comment '所属商品ID',
    spec_ids  varchar(200)  DEFAULT NULL comment '规格组合ID(JSON数组)',
    image     varchar(500)  DEFAULT NULL comment 'SKU图片URL',
    price     decimal(10,2) NOT NULL comment 'SKU价格',
    stock     int           DEFAULT '0' comment 'SKU库存',
    create_time datetime    DEFAULT NULL comment '创建时间',
    update_time datetime    DEFAULT NULL comment '更新时间',
    deleted   tinyint       DEFAULT '0' comment '是否删除(0=否 1=是)',
    PRIMARY KEY (id),
    KEY idx_goods_id (goods_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 comment '商品SKU表';

CREATE TABLE cm_goods_spec (
    id         bigint       NOT NULL AUTO_INCREMENT comment '规格ID',
    goods_id   bigint       NOT NULL comment '所属商品ID',
    name       varchar(50)  NOT NULL comment '规格名(如颜色、尺码)',
    value      varchar(100) NOT NULL comment '规格值(如红色、XL)',
    sort_order int          DEFAULT '0' comment '排序值(越小越靠前)',
    PRIMARY KEY (id),
    KEY idx_goods_id (goods_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 comment '商品规格表';

CREATE TABLE cm_comment (
    id        bigint   NOT NULL AUTO_INCREMENT comment '评论ID',
    goods_id  bigint   NOT NULL comment '商品ID',
    user_id   bigint   NOT NULL comment '评论用户ID',
    order_id  bigint   DEFAULT NULL comment '关联订单ID',
    content   text     DEFAULT NULL comment '评论内容',
    rating    tinyint  DEFAULT '5' comment '评分(1-5)',
    images    text     DEFAULT NULL comment '评论图片(JSON数组)',
    status    tinyint  DEFAULT '1' comment '状态(0=隐藏 1=显示)',
    create_time datetime DEFAULT NULL comment '创建时间',
    PRIMARY KEY (id),
    KEY idx_goods_id (goods_id),
    KEY idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 comment '商品评论表';

CREATE TABLE cm_banner (
    id         bigint       NOT NULL AUTO_INCREMENT comment '轮播图ID',
    title      varchar(200) DEFAULT NULL comment '轮播图标题',
    image      varchar(500) NOT NULL comment '轮播图URL',
    link_url   varchar(500) DEFAULT NULL comment '跳转链接URL',
    sort_order int          DEFAULT '0' comment '排序值(越小越靠前)',
    status     tinyint      DEFAULT '1' comment '是否启用(0=禁用 1=启用)',
    create_time datetime    DEFAULT NULL comment '创建时间',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 comment '首页轮播图表';

CREATE TABLE cm_favorite (
    id         bigint   NOT NULL AUTO_INCREMENT comment '收藏ID',
    user_id    bigint   NOT NULL comment '用户ID',
    goods_id   bigint   NOT NULL comment '商品ID',
    create_time datetime DEFAULT NULL comment '收藏时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_goods (user_id, goods_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 comment '用户收藏表';

CREATE TABLE cm_unit (
    id         bigint       NOT NULL AUTO_INCREMENT comment '单位ID',
    name       varchar(50)  NOT NULL comment '单位名称(件/个/斤)',
    status     tinyint      DEFAULT '1' comment '是否启用(0=禁用 1=启用)',
    sort_order int          DEFAULT '0' comment '排序值(越小越靠前)',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 comment '商品单位表';

-- ============================================================
-- 优惠券模块 (coupon)
-- ============================================================

CREATE TABLE cm_coupon (
    id            bigint        NOT NULL AUTO_INCREMENT comment '优惠券ID',
    name          varchar(100)  NOT NULL comment '优惠券名称',
    type          varchar(20)   NOT NULL comment '优惠类型(FULL_REDUCTION=满减 DISCOUNT=折扣 CASH=现金)',
    threshold     decimal(10,2) DEFAULT NULL comment '满减门槛金额',
    discount      decimal(10,2) NOT NULL comment '优惠金额或折扣率',
    total_count   int           DEFAULT '0' comment '发行总量',
    claimed_count int           DEFAULT '0' comment '已领取数量',
    status        tinyint(1)    DEFAULT '1' comment '是否启用(0=禁用 1=启用)',
    start_time    datetime      DEFAULT NULL comment '生效时间',
    expire_time   datetime      DEFAULT NULL comment '过期时间',
    create_time   datetime      DEFAULT NULL comment '创建时间',
    update_time   datetime      DEFAULT NULL comment '更新时间',
    deleted       tinyint(1)    DEFAULT '0' comment '是否删除(0=否 1=是)',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 comment '优惠券模板表';

CREATE TABLE cm_user_coupon (
    id           bigint      NOT NULL AUTO_INCREMENT comment '用户优惠券ID',
    user_id      bigint      NOT NULL comment '用户ID',
    coupon_id    bigint      NOT NULL comment '优惠券模板ID',
    status       varchar(20) DEFAULT 'UNUSED' comment '使用状态(UNUSED=未使用 USED=已使用 EXPIRED=已过期)',
    claimed_time datetime    DEFAULT NULL comment '领取时间',
    used_time    datetime    DEFAULT NULL comment '使用时间',
    order_id     bigint      DEFAULT NULL comment '关联订单ID',
    PRIMARY KEY (id),
    KEY idx_user_id (user_id),
    KEY idx_coupon_id (coupon_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 comment '用户优惠券记录表';

-- ============================================================
-- 订单模块 (order)
-- ============================================================

CREATE TABLE cm_order (
    id               bigint        NOT NULL AUTO_INCREMENT comment '订单ID',
    order_no         varchar(64)   NOT NULL comment '订单编号',
    user_id          bigint        NOT NULL comment '下单用户ID',
    total_amount     decimal(10,2) NOT NULL comment '订单总金额',
    status           varchar(20)   DEFAULT 'PENDING' comment '订单状态(PENDING=待付款 PAID=已付款 SHIPPED=已发货 DELIVERED=已收货 CANCELLED=已取消)',
    address_snapshot text          DEFAULT NULL comment '收货地址快照(JSON)',
    remark           varchar(500)  DEFAULT NULL comment '订单备注',
    create_time      datetime      DEFAULT NULL comment '创建时间',
    update_time      datetime      DEFAULT NULL comment '更新时间',
    deleted          tinyint       DEFAULT '0' comment '是否删除(0=否 1=是)',
    PRIMARY KEY (id),
    UNIQUE KEY uk_order_no (order_no),
    KEY idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 comment '订单主表';

CREATE TABLE cm_order_item (
    id           bigint        NOT NULL AUTO_INCREMENT comment '订单项ID',
    order_id     bigint        NOT NULL comment '所属订单ID',
    goods_id     bigint        NOT NULL comment '商品ID',
    sku_id       bigint        DEFAULT NULL comment 'SKU ID',
    goods_name   varchar(200)  DEFAULT NULL comment '商品名称(快照)',
    goods_image  varchar(500)  DEFAULT NULL comment '商品图片(快照)',
    price        decimal(10,2) NOT NULL comment '成交单价',
    quantity     int           NOT NULL comment '购买数量',
    subtotal     decimal(10,2) NOT NULL comment '小计金额',
    PRIMARY KEY (id),
    KEY idx_order_id (order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 comment '订单明细表';

CREATE TABLE cm_cart (
    id           bigint        NOT NULL AUTO_INCREMENT comment '购物车项ID',
    user_id      bigint        NOT NULL comment '用户ID',
    goods_id     bigint        NOT NULL comment '商品ID',
    sku_id       bigint        DEFAULT NULL comment 'SKU ID',
    goods_name   varchar(200)  DEFAULT NULL comment '商品名称',
    goods_image  varchar(500)  DEFAULT NULL comment '商品图片URL',
    price        decimal(10,2) NOT NULL comment '加入时单价',
    quantity     int           NOT NULL DEFAULT '1' comment '数量',
    selected     tinyint       DEFAULT '1' comment '是否选中(0=未选中 1=已选中)',
    create_time  datetime      DEFAULT NULL comment '加入时间',
    update_time  datetime      DEFAULT NULL comment '更新时间',
    PRIMARY KEY (id),
    KEY idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 comment '购物车表';

-- ============================================================
-- 秒杀模块 (seckill)
-- ============================================================

CREATE TABLE cm_seckill_activity (
    id          bigint       NOT NULL AUTO_INCREMENT comment '秒杀活动ID',
    name        varchar(200) NOT NULL comment '活动名称',
    start_time  datetime     NOT NULL comment '活动开始时间',
    end_time    datetime     NOT NULL comment '活动结束时间',
    status      varchar(20)  DEFAULT 'DRAFT' comment '活动状态(DRAFT=草稿 PENDING=待审核 APPROVED=已通过 REJECTED=已驳回 FINISHED=已结束)',
    create_user bigint       DEFAULT NULL comment '创建人用户ID',
    create_time datetime     DEFAULT NULL comment '创建时间',
    update_time datetime     DEFAULT NULL comment '更新时间',
    deleted     tinyint      DEFAULT '0' comment '是否删除(0=否 1=是)',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 comment '秒杀活动表';

CREATE TABLE cm_seckill_goods (
    id            bigint        NOT NULL AUTO_INCREMENT comment '秒杀商品ID',
    activity_id   bigint        NOT NULL comment '所属活动ID',
    goods_id      bigint        NOT NULL comment '商品ID',
    seckill_price decimal(10,2) NOT NULL comment '秒杀价格',
    stock         int           DEFAULT '0' comment '秒杀库存',
    sold_count    int           DEFAULT '0' comment '已售数量',
    audit_status  varchar(20)   DEFAULT 'PENDING' comment '审核状态(PENDING=待审核 APPROVED=已通过 REJECTED=已驳回)',
    PRIMARY KEY (id),
    KEY idx_activity (activity_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 comment '秒杀商品表';
