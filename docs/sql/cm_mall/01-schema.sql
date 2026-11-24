-- ============================================================
-- cm_mall — 共享数据库
-- 包含所有服务模块的表结构
-- ============================================================

SET NAMES utf8mb4;

CREATE DATABASE IF NOT EXISTS cm_mall DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE cm_mall;

-- ============================================================
-- 认证模块 (auth)
-- ============================================================

CREATE TABLE IF NOT EXISTS cm_user
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
    create_time datetime    DEFAULT NULL comment '创建时间',
    update_time datetime    DEFAULT NULL comment '更新时间',
    deleted    tinyint(1)   DEFAULT '0' comment '是否删除(0=否 1=是)',
    constraint uk_username
        unique (username)
) comment '用户认证表' engine = InnoDB
                       collate = utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS cm_role
(
    id          bigint auto_increment comment '角色ID'
        primary key,
    name        varchar(50)                          not null comment '角色名称',
    built_in    tinyint(1) default 0                 not null comment '是否为内置角色(不可删除)',
    enabled     tinyint(1) default 1                 null comment '是否启用',
    description varchar(255)                         null comment '角色描述',
    create_time datetime   default CURRENT_TIMESTAMP null comment '创建时间',
    update_time datetime    DEFAULT NULL comment '更新时间',
    deleted    tinyint(1)   DEFAULT '0' comment '是否删除(0=否 1=是)',
    constraint uk_role_name
        unique (name)
) comment '系统角色表' engine = InnoDB
                      collate = utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS cm_user_role
(
    user_id     bigint                             not null comment '用户ID',
    role_id     bigint                             not null comment '角色ID',
    create_time datetime default CURRENT_TIMESTAMP null comment '创建时间',
    primary key (user_id, role_id),
    key idx_role_id (role_id),
    constraint fk_auth_user_role_user
        foreign key (user_id) references cm_user (id)
            on delete cascade,
    constraint fk_auth_user_role_role
        foreign key (role_id) references cm_role (id)
            on delete cascade
) comment '用户与角色关联表' engine = InnoDB
                            collate = utf8mb4_unicode_ci;

-- ============================================================
-- RBAC 权限模块
-- ============================================================

CREATE TABLE IF NOT EXISTS cm_resource
(
    id          bigint auto_increment comment '资源ID',
    code        varchar(100)   not null comment '权限编码，如 product:create',
    name        varchar(100)   not null comment '资源名称',
    type        varchar(20)    not null comment '资源类型: api / menu / button',
    parent_id   bigint         default null comment '父资源ID',
    sort        int            default 0 comment '排序号',
    enabled     tinyint(1)     default 1 comment '是否启用',
    create_time datetime       default current_timestamp null comment '创建时间',
    update_time datetime       default null comment '更新时间',
    deleted     tinyint(1)     default 0 comment '是否删除(0=否 1=是)',
    primary key (id),
    unique key uk_code (code),
    key idx_parent_id (parent_id)
) comment '资源/权限编码定义表' engine = InnoDB
                                     collate = utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS cm_role_resource
(
    role_id     bigint   not null comment '角色ID',
    resource_id bigint   not null comment '资源ID',
    create_time datetime default current_timestamp null comment '创建时间',
    primary key (role_id, resource_id),
    constraint fk_role_resource_role
        foreign key (role_id) references cm_role (id) on delete cascade,
    constraint fk_role_resource_resource
        foreign key (resource_id) references cm_resource (id) on delete cascade
) comment '角色与权限关联表' engine = InnoDB
                                collate = utf8mb4_unicode_ci;

-- ============================================================
-- 用户模块 (user)
-- ============================================================

CREATE TABLE IF NOT EXISTS cm_address (
    id         bigint       NOT NULL AUTO_INCREMENT comment '地址ID',
    user_id    bigint       NOT NULL comment '用户ID',
    consignee  varchar(100) NOT NULL comment '收货人',
    phone      varchar(20)  NOT NULL comment '联系电话',
    province   varchar(50)  DEFAULT NULL comment '省份',
    city       varchar(50)  DEFAULT NULL comment '城市',
    district   varchar(50)  DEFAULT NULL comment '区县',
    detail        varchar(255) DEFAULT NULL comment '详细地址',
    zip_code      varchar(10)  DEFAULT NULL comment '邮政编码',
    province_code varchar(12)  DEFAULT NULL comment '省级代码',
    city_code     varchar(12)  DEFAULT NULL comment '市级代码',
    district_code varchar(12)  DEFAULT NULL comment '区县级代码',
    is_default    tinyint(1)   DEFAULT '0' comment '是否默认地址(0=否 1=是)',
    slot       tinyint      NOT NULL comment '地址槽位(1-10)',
    create_time datetime    DEFAULT NULL comment '创建时间',
    update_time datetime    DEFAULT NULL comment '更新时间',
    PRIMARY KEY (id),
    KEY idx_user_id (user_id),
    UNIQUE KEY uk_address_user_slot (user_id, slot),
    CONSTRAINT chk_address_slot CHECK (slot BETWEEN 1 AND 10)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 comment '用户收货地址表';

CREATE TABLE IF NOT EXISTS cm_shop (
    id          bigint        NOT NULL AUTO_INCREMENT comment '店铺ID',
    user_id     bigint        NOT NULL comment '商家用户ID',
    name   varchar(200)  NOT NULL comment '店铺名称',
    logo   varchar(500)  DEFAULT NULL comment '店铺Logo URL',
    description text          DEFAULT NULL comment '店铺描述',
    status      tinyint(1)    DEFAULT '1' comment '店铺状态(0=关闭 1=营业)',
    create_time datetime      DEFAULT NULL comment '创建时间',
    update_time datetime      DEFAULT NULL comment '更新时间',
    deleted     tinyint(1)    DEFAULT '0' comment '是否删除(0=否 1=是)',
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 comment '商家店铺表（一个商家最多一个店铺）';

-- 商家入驻申请（一人一条，驳回后可覆盖重提）
CREATE TABLE IF NOT EXISTS cm_store_application (
    id                   bigint       NOT NULL AUTO_INCREMENT comment '入驻申请ID',
    user_id              bigint       NOT NULL comment '申请用户ID',
    status               varchar(20)  NOT NULL DEFAULT 'PENDING' comment '状态(NONE/PENDING/APPROVED/REJECTED/SUCCESS)',
    subject_type         varchar(20)  NOT NULL DEFAULT 'personal' comment '主体类型(personal/individual/enterprise)',
    real_name            varchar(50)  NOT NULL comment '真实姓名',
    id_card              varchar(32)  NOT NULL comment '身份证号',
    id_card_valid_start  date         NOT NULL comment '身份证有效期开始',
    id_card_valid_end    date         NOT NULL comment '身份证有效期结束',
    id_card_front        varchar(500) NOT NULL comment '身份证人像面照片',
    id_card_back         varchar(500) NOT NULL comment '身份证国徽面照片',
    license_number       varchar(64)  DEFAULT NULL comment '营业执照统一社会信用代码',
    license_name         varchar(200) DEFAULT NULL comment '营业执照名称',
    establishment_date   date         DEFAULT NULL comment '成立日期',
    registered_address   varchar(500) DEFAULT NULL comment '注册地址',
    license_photo        varchar(500) DEFAULT NULL comment '营业执照照片',
    store_name           varchar(200) NOT NULL comment '店铺名称',
    categories           json         NOT NULL comment '经营类目ID列表',
    shipping_address     varchar(500) NOT NULL comment '发货地址',
    bank_account_name    varchar(100) NOT NULL comment '银行账户名',
    bank_card_number     varchar(64)  NOT NULL comment '银行卡号',
    bank_name            varchar(100) NOT NULL comment '开户银行',
    bank_branch_name     varchar(200) NOT NULL comment '开户支行',
    bank_mobile          varchar(30)  NOT NULL comment '银行预留手机号',
    agreement            tinyint(1)   NOT NULL DEFAULT 0 comment '是否同意入驻协议',
    audit_remark         varchar(500) DEFAULT NULL comment '审核意见/驳回原因',
    auditor_id           bigint       DEFAULT NULL comment '审核人用户ID',
    audit_time           datetime     DEFAULT NULL comment '审核时间',
    shop_id              bigint       DEFAULT NULL comment '开通的店铺ID',
    submit_time          datetime     DEFAULT NULL comment '提交时间',
    create_time          datetime     DEFAULT NULL comment '创建时间',
    update_time          datetime     DEFAULT NULL comment '更新时间',
    deleted              tinyint(1)   NOT NULL DEFAULT 0 comment '是否删除(0=否 1=是)',
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_id (user_id),
    KEY idx_status_submit_time (status, submit_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 comment '商家入驻申请表';

-- ============================================================
-- 商品模块 (product)
-- ============================================================

CREATE TABLE IF NOT EXISTS cm_category (
    id         bigint       NOT NULL AUTO_INCREMENT comment '分类ID',
    name       varchar(100) NOT NULL comment '分类名称',
    parent_id  bigint       DEFAULT '0' comment '父分类ID(0=顶级分类)',
    level      tinyint      DEFAULT '1' comment '分类层级(1=一级 2=二级 3=三级)',
    icon       varchar(500) DEFAULT NULL comment '分类图标URL',
    sort int          DEFAULT '0' comment '排序值(越小越靠前)',
    status     tinyint      DEFAULT '1' comment '是否启用(0=禁用 1=启用)',
    create_time datetime    DEFAULT NULL comment '创建时间',
    update_time datetime    DEFAULT NULL comment '更新时间',
    deleted    tinyint      DEFAULT '0' comment '是否删除(0=否 1=是)',
    PRIMARY KEY (id),
    KEY idx_parent_id (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 comment '商品分类表';

CREATE TABLE IF NOT EXISTS cm_brand (
    id          bigint        NOT NULL AUTO_INCREMENT comment '品牌ID',
    name        varchar(100)  NOT NULL comment '品牌名称',
    name_en     varchar(100)  DEFAULT NULL comment '品牌英文名',
    logo        varchar(500)  DEFAULT NULL comment '品牌Logo URL',
    description text          DEFAULT NULL comment '品牌描述/故事',
    country     varchar(50)   DEFAULT NULL comment '品牌国别',
    website     varchar(500)  DEFAULT NULL comment '官方网站',
    status      tinyint(1)    DEFAULT 1 comment '状态(0=禁用 1=启用)',
    sort  int           DEFAULT 0 comment '排序值(越小越靠前)',
    create_time datetime      DEFAULT NULL comment '创建时间',
    update_time datetime      DEFAULT NULL comment '更新时间',
    deleted     tinyint(1)    DEFAULT 0 comment '是否删除(0=否 1=是)',
    PRIMARY KEY (id),
    UNIQUE KEY uk_name (name),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 comment '品牌表';

CREATE TABLE IF NOT EXISTS cm_product (
    id          bigint        NOT NULL AUTO_INCREMENT comment '商品ID',
    name        varchar(200)  NOT NULL comment '商品名称',
    spu_code    varchar(50)   NOT NULL comment 'SPU编码',
    description text          DEFAULT NULL comment '商品描述',
    category_id bigint        DEFAULT NULL comment '所属分类ID',
    brand_id    bigint        DEFAULT NULL comment '品牌ID',
    shop_id     bigint        DEFAULT NULL comment '所属店铺ID（关联cm_shop）',
    status      tinyint(1)    DEFAULT 1 comment '商品状态(0=下架 1=上架)',
    create_time datetime      DEFAULT NULL comment '创建时间',
    update_time datetime      DEFAULT NULL comment '更新时间',
    deleted     tinyint(1)    DEFAULT 0 comment '是否删除(0=否 1=是)',
    PRIMARY KEY (id),
    KEY idx_status (status),
    KEY idx_category (category_id),
    KEY idx_brand_id (brand_id),
    KEY idx_shop_id (shop_id),
    UNIQUE KEY uk_spu_code (spu_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 comment '商品信息表(SPU)';

CREATE TABLE IF NOT EXISTS cm_product_sku (
    id           bigint        NOT NULL AUTO_INCREMENT comment 'SKU ID',
    product_id   bigint        NOT NULL comment '所属商品ID',
    sku_code     varchar(64)   DEFAULT NULL comment 'SKU编码',
    price        decimal(10,2) NOT NULL comment '销售价格',
    market_price decimal(10,2) DEFAULT NULL comment '市场价(划线价)',
    stock        int           DEFAULT 0 comment '库存数量',
    weight       decimal(10,2) DEFAULT 0.00 comment '重量(kg)',
    image        varchar(500)  DEFAULT NULL comment 'SKU图片URL',
    spec_hash    varchar(255)  DEFAULT NULL comment '规格值ID升序拼接(用于同一SPU下组合防重)',
    create_time  datetime      DEFAULT NULL comment '创建时间',
    update_time  datetime      DEFAULT NULL comment '更新时间',
    deleted      tinyint(1)    DEFAULT 0 comment '是否删除(0=否 1=是)',
    PRIMARY KEY (id),
    KEY idx_product_id (product_id),
    UNIQUE KEY uk_sku_code (sku_code),
    UNIQUE KEY uk_product_spec (product_id, spec_hash)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 comment '商品SKU表';

CREATE TABLE IF NOT EXISTS cm_product_spec_group (
    id          bigint       NOT NULL AUTO_INCREMENT comment '规格组ID',
    product_id  bigint       NOT NULL comment '所属商品ID',
    name        varchar(50)  NOT NULL comment '规格名(如颜色、尺码)',
    sort  int          DEFAULT 0 comment '排序值(越小越靠前)',
    create_time datetime     DEFAULT NULL comment '创建时间',
    update_time datetime     DEFAULT NULL comment '更新时间',
    deleted     tinyint(1)   DEFAULT 0 comment '是否删除(0=否 1=是)',
    PRIMARY KEY (id),
    KEY idx_product_id (product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 comment '商品规格组表';

CREATE TABLE IF NOT EXISTS cm_product_spec_value (
    id          bigint       NOT NULL AUTO_INCREMENT comment '规格值ID',
    group_id    bigint       NOT NULL comment '所属规格组ID',
    value       varchar(100) NOT NULL comment '规格值(如红色、XL)',
    image       varchar(500) DEFAULT NULL comment '规格图片(色块/图标)',
    sort  int          DEFAULT 0 comment '排序值(越小越靠前)',
    create_time datetime     DEFAULT NULL comment '创建时间',
    update_time datetime     DEFAULT NULL comment '更新时间',
    deleted     tinyint(1)   DEFAULT 0 comment '是否删除(0=否 1=是)',
    PRIMARY KEY (id),
    KEY idx_group_id (group_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 comment '商品规格值表';

CREATE TABLE IF NOT EXISTS cm_product_sku_spec (
    sku_id        bigint   NOT NULL comment 'SKU ID',
    spec_value_id bigint   NOT NULL comment '规格值ID',
    create_time   datetime DEFAULT NULL comment '创建时间',
    PRIMARY KEY (sku_id, spec_value_id),
    KEY idx_spec_value_id (spec_value_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 comment 'SKU与规格值关联表';

CREATE TABLE IF NOT EXISTS cm_product_image (
    id          bigint        NOT NULL AUTO_INCREMENT comment '图片ID',
    product_id  bigint        NOT NULL comment '商品ID',
    url         varchar(500)  NOT NULL comment '图片URL',
    is_main     tinyint(1)    NOT NULL DEFAULT 0 comment '是否主图(0=否 1=是)',
    sort  int           NOT NULL DEFAULT 0 comment '排序（详情页轮播顺序）',
    create_time datetime      DEFAULT CURRENT_TIMESTAMP comment '创建时间',
    PRIMARY KEY (id),
    KEY idx_product_id (product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 comment '商品图片表';

CREATE TABLE IF NOT EXISTS cm_comment (
    id            bigint   NOT NULL AUTO_INCREMENT comment '评论ID',
    product_id    bigint   NOT NULL comment '商品SPU ID',
    sku_id        bigint   DEFAULT NULL comment '商品SKU ID',
    shop_id       bigint   NOT NULL comment '店铺ID',
    user_id       bigint   NOT NULL comment '评论用户ID',
    order_id      bigint   NOT NULL comment '订单ID',
    order_item_id bigint   NOT NULL comment '订单项ID',
    content       text     DEFAULT NULL comment '评论内容',
    rating        tinyint  NOT NULL comment '评分(1-5)',
    images        text     DEFAULT NULL comment '评论图片(JSON数组)',
    useful_count  int      NOT NULL DEFAULT 0 comment '有用数',
    status        tinyint  NOT NULL DEFAULT 1 comment '状态(0=隐藏 1=显示)',
    create_time   datetime NOT NULL comment '创建时间',
    update_time   datetime NOT NULL comment '更新时间',
    deleted       tinyint  NOT NULL DEFAULT 0 comment '是否删除(0=否 1=是)',
    PRIMARY KEY (id),
    UNIQUE KEY uk_order_item_id (order_item_id),
    KEY idx_product_comment (product_id, status, deleted, create_time),
    KEY idx_shop_comment (shop_id, status, deleted),
    KEY idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 comment '商品评论表';

CREATE TABLE IF NOT EXISTS cm_product_qa (
    id            bigint        NOT NULL AUTO_INCREMENT comment '问答ID',
    product_id    bigint        NOT NULL comment '商品SPU ID',
    question      varchar(500)  NOT NULL comment '问题内容',
    answer        varchar(2000) DEFAULT NULL comment '回答内容',
    helpful_count int           NOT NULL DEFAULT 0 comment '有用数',
    status        tinyint       NOT NULL DEFAULT 1 comment '状态(0=隐藏 1=显示)',
    create_time   datetime      NOT NULL comment '创建时间',
    update_time   datetime      NOT NULL comment '更新时间',
    deleted       tinyint       NOT NULL DEFAULT 0 comment '是否删除(0=否 1=是)',
    PRIMARY KEY (id),
    KEY idx_product_qa (product_id, status, deleted, create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 comment '商品问答表';

CREATE TABLE IF NOT EXISTS cm_banner (
    id         bigint       NOT NULL AUTO_INCREMENT comment '轮播图ID',
    title      varchar(200) DEFAULT NULL comment '轮播图标题',
    image      varchar(500) NOT NULL comment '轮播图URL',
    link_url   varchar(500) DEFAULT NULL comment '跳转链接URL',
    sort int          DEFAULT '0' comment '排序值(越小越靠前)',
    status     tinyint      DEFAULT '1' comment '是否启用(0=禁用 1=启用)',
    create_time datetime    DEFAULT NULL comment '创建时间',
    update_time datetime    DEFAULT NULL comment '更新时间',
    deleted    tinyint      DEFAULT '0' comment '是否删除(0=否 1=是)',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 comment '首页轮播图表';

CREATE TABLE IF NOT EXISTS cm_notice (
    id         bigint       NOT NULL AUTO_INCREMENT comment '公告ID',
    content    varchar(500) NOT NULL comment '公告内容',
    sort int          DEFAULT '0' comment '排序值(越小越靠前)',
    status     tinyint      DEFAULT '1' comment '是否启用(0=禁用 1=启用)',
    create_time datetime    DEFAULT NULL comment '创建时间',
    update_time datetime    DEFAULT NULL comment '更新时间',
    deleted    tinyint      DEFAULT '0' comment '是否删除(0=否 1=是)',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 comment '首页公告表';

CREATE TABLE IF NOT EXISTS cm_favorite (
    id         bigint   NOT NULL AUTO_INCREMENT comment '收藏ID',
    user_id    bigint   NOT NULL comment '用户ID',
    product_id   bigint   NOT NULL comment '商品ID',
    create_time datetime DEFAULT NULL comment '收藏时间',
    update_time datetime    DEFAULT NULL comment '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_product (user_id, product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 comment '用户收藏表';

CREATE TABLE IF NOT EXISTS cm_shop_follow (
    id         bigint   NOT NULL AUTO_INCREMENT comment '主键',
    user_id    bigint   NOT NULL comment '用户ID',
    shop_id    bigint   NOT NULL comment '店铺ID',
    create_time datetime NOT NULL comment '关注时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_shop (user_id, shop_id),
    KEY idx_user_id (user_id),
    KEY idx_shop_id (shop_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 comment '用户关注店铺关系表';

CREATE TABLE IF NOT EXISTS cm_shop_stat (
    shop_id                bigint   NOT NULL comment '店铺ID',
    product_count          int      NOT NULL DEFAULT 0 comment '出售中商品数量',
    total_review_count     int      NOT NULL DEFAULT 0 comment '有效评价数量',
    positive_review_count  int      NOT NULL DEFAULT 0 comment '好评数量',
    total_score            int      NOT NULL DEFAULT 0 comment '评价总评分',
    follow_count           bigint   NOT NULL DEFAULT 0 comment '关注数量',
    update_time            datetime NOT NULL comment '更新时间',
    PRIMARY KEY (shop_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 comment '店铺统计表';

CREATE TABLE IF NOT EXISTS cm_unit (
    id         bigint       NOT NULL AUTO_INCREMENT comment '单位ID',
    name       varchar(50)  NOT NULL comment '单位名称(件/个/斤)',
    status     tinyint      DEFAULT '1' comment '是否启用(0=禁用 1=启用)',
    sort int          DEFAULT '0' comment '排序值(越小越靠前)',
    create_time datetime    DEFAULT NULL comment '创建时间',
    update_time datetime    DEFAULT NULL comment '更新时间',
    deleted    tinyint      DEFAULT '0' comment '是否删除(0=否 1=是)',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 comment '商品单位表';

-- ============================================================
-- 优惠券模块 (coupon)
-- ============================================================

-- 优惠券模板（cm_coupon 表语义为模板，不是用户持有的券资产）
CREATE TABLE IF NOT EXISTS cm_coupon (
    id                    bigint        NOT NULL AUTO_INCREMENT comment '优惠券模板ID',
    name                  varchar(100)  NOT NULL comment '券展示名称',
    owner_type            varchar(20)   NOT NULL comment '券归属(PLATFORM=平台券 SHOP=店铺券)',
    shop_id               bigint        DEFAULT NULL comment '店铺ID(平台券为NULL，店铺券必须有值)',
    created_by            bigint        DEFAULT NULL comment '创建人ID(仅用于审计)',
    type                  varchar(20)   NOT NULL comment '券类型(FULL_REDUCTION=满减 DISCOUNT=折扣 CASH=立减)',
    scope_type            varchar(20)   NOT NULL comment '适用范围(ALL=全范围 PRODUCT=指定商品 CATEGORY=指定分类)',
    threshold             decimal(18,2) NOT NULL DEFAULT 0.00 comment '使用门槛(统一使用0表示无门槛)',
    discount_amount       decimal(18,2) DEFAULT NULL comment '满减/立减优惠金额',
    discount_rate         decimal(8,6)  DEFAULT NULL comment '折扣券减免比例(0.10表示减免10%)',
    total_count           bigint        DEFAULT NULL comment '发行总量(NULL=不限量 正数=发行上限)',
    claimed_count         bigint        NOT NULL DEFAULT 0 comment '累计成功发放数量(只增不减)',
    issue_channel         varchar(20)   DEFAULT NULL comment '发放渠道(PUBLIC_CLAIM=公开领取 REGISTER_AUTO=注册自动发放 NULL=未设置不可发布)',
    validity_mode         varchar(20)   NOT NULL comment '有效期模式(FIXED=固定有效期 RELATIVE=发放后相对有效期)',
    issue_start_time      datetime      DEFAULT NULL comment '公开领取/自动发放窗口开始时间',
    issue_end_time        datetime      DEFAULT NULL comment '公开领取/自动发放窗口结束时间',
    fixed_start_time      datetime      DEFAULT NULL comment 'FIXED模式用户券生效时间',
    fixed_expire_time     datetime      DEFAULT NULL comment 'FIXED模式用户券过期时间',
    valid_days            int           DEFAULT NULL comment 'RELATIVE模式发放后有效天数',
    status                varchar(20)   NOT NULL DEFAULT 'DRAFT' comment '模板状态(DRAFT=草稿 ACTIVE=已发布 PAUSED=已暂停 ENDED=已结束)',
    first_issued_time     datetime      DEFAULT NULL comment '首次发放时间',
    create_time           datetime      DEFAULT NULL comment '创建时间',
    update_time           datetime      DEFAULT NULL comment '更新时间',
    deleted               tinyint(1)    NOT NULL DEFAULT 0 comment '是否删除(0=否 1=是)',
    PRIMARY KEY (id),
    KEY idx_owner_shop (owner_type, shop_id),
    KEY idx_status (status),
    KEY idx_issue_window (issue_start_time, issue_end_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 comment '优惠券模板表';

-- 优惠券商品范围（scope_type=PRODUCT 时使用，按 SPU 配置，覆盖全部 SKU）
CREATE TABLE IF NOT EXISTS cm_coupon_product (
    coupon_id   bigint   NOT NULL comment '优惠券模板ID',
    product_id  bigint   NOT NULL comment '商品SPU ID',
    create_time datetime DEFAULT NULL comment '创建时间',
    PRIMARY KEY (coupon_id, product_id),
    KEY idx_product_id (product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 comment '优惠券商品范围表';

-- 优惠券分类范围（scope_type=CATEGORY 时使用，命中所选分类及全部后代分类）
CREATE TABLE IF NOT EXISTS cm_coupon_category (
    coupon_id   bigint   NOT NULL comment '优惠券模板ID',
    category_id bigint   NOT NULL comment '分类树根节点ID',
    create_time datetime DEFAULT NULL comment '创建时间',
    PRIMARY KEY (coupon_id, category_id),
    KEY idx_category_id (category_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 comment '优惠券分类范围表';

-- 用户券（用户实际持有的优惠权益，保存领取时核心快照）
CREATE TABLE IF NOT EXISTS cm_user_coupon (
    id                      bigint        NOT NULL AUTO_INCREMENT comment '用户券ID',
    user_id                 bigint        NOT NULL comment '用户ID',
    coupon_id               bigint        NOT NULL comment '优惠券模板ID',
    owner_type              varchar(20)   NOT NULL comment '领取时归属快照(PLATFORM/SHOP)',
    shop_id                 bigint        DEFAULT NULL comment '店铺券归属快照',
    name_snapshot           varchar(100)  NOT NULL comment '名称快照',
    type_snapshot           varchar(20)   NOT NULL comment '券类型快照',
    scope_type_snapshot     varchar(20)   NOT NULL comment '适用范围快照',
    threshold_snapshot      decimal(18,2) NOT NULL DEFAULT 0.00 comment '门槛快照',
    discount_amount_snapshot decimal(18,2) DEFAULT NULL comment '金额优惠快照',
    discount_rate_snapshot  decimal(8,6)  DEFAULT NULL comment '比例优惠快照',
    effective_time          datetime      NOT NULL comment '本张用户券实际生效时间',
    expire_time             datetime      NOT NULL comment '本张用户券实际过期时间',
    status                  varchar(20)   NOT NULL DEFAULT 'AVAILABLE' comment '用户券状态(AVAILABLE=可用 LOCKED=锁定 USED=已使用 EXPIRED=已过期 REVOKED=已撤销)',
    locked_order_no         varchar(64)   DEFAULT NULL comment '当前锁定订单号(非永久历史)',
    lock_expire_time        datetime      DEFAULT NULL comment '当前锁定过期时间',
    state_time              datetime      DEFAULT NULL comment '当前状态变更时间',
    issued_source           varchar(30)   NOT NULL comment '发放来源(PUBLIC_CLAIM=公开领取 REGISTER_AUTO=注册自动发放)',
    issued_event_id         varchar(64)   DEFAULT NULL comment '自动发放事件ID(公开领取为空)',
    create_time             datetime      DEFAULT NULL comment '发放时间',
    update_time             datetime      DEFAULT NULL comment '更新时间',
    deleted                 tinyint(1)    NOT NULL DEFAULT 0 comment '是否删除(0=否 1=是)',
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_coupon (user_id, coupon_id),
    KEY idx_user_status (user_id, status),
    KEY idx_coupon_id (coupon_id),
    KEY idx_locked_order (locked_order_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 comment '用户优惠券表';

-- 用户券状态流水（追加写入，不允许修改和删除）
CREATE TABLE IF NOT EXISTS cm_user_coupon_log (
    id              bigint        NOT NULL AUTO_INCREMENT comment '流水ID',
    user_coupon_id  bigint        NOT NULL comment '用户券ID',
    coupon_id       bigint        NOT NULL comment '优惠券模板ID',
    user_id         bigint        NOT NULL comment '用户ID',
    order_no        varchar(64)   DEFAULT NULL comment '相关订单号',
    event_id        varchar(64)   DEFAULT NULL comment '事件/操作幂等ID',
    operation_type  varchar(30)   NOT NULL comment '操作类型(ISSUE=发放 LOCK=锁定 COMMIT=提交 RELEASE=释放 REVERT=回退 EXPIRE=过期 REVOKE=撤销)',
    from_status     varchar(20)   DEFAULT NULL comment '原状态',
    to_status       varchar(20)   DEFAULT NULL comment '新状态',
    source          varchar(20)   NOT NULL comment '来源(USER=用户 ORDER=订单 PAYMENT=支付 SYSTEM=系统 OPERATOR=运营)',
    operator_id     bigint        DEFAULT NULL comment '操作人ID(系统事件为空)',
    reason          varchar(500)  DEFAULT NULL comment '原因',
    create_time     datetime      DEFAULT NULL comment '发生时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_event_operation (event_id, operation_type),
    KEY idx_user_coupon_id (user_coupon_id),
    KEY idx_coupon_id (coupon_id),
    KEY idx_user_id (user_id),
    KEY idx_order_no (order_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 comment '用户券状态流水表';

-- 模板管理流水（追加写入，商家只能查询自身店铺，运营可查询全量）
CREATE TABLE IF NOT EXISTS cm_coupon_operation_log (
    id              bigint        NOT NULL AUTO_INCREMENT comment '流水ID',
    coupon_id       bigint        NOT NULL comment '优惠券模板ID',
    operator_id     bigint        NOT NULL comment '操作人ID',
    operator_type   varchar(20)   NOT NULL comment '操作人类型(OPERATOR=运营 MERCHANT=商家)',
    shop_id         bigint        DEFAULT NULL comment '店铺ID(平台券为NULL)',
    from_status     varchar(20)   DEFAULT NULL comment '操作前状态',
    to_status       varchar(20)   DEFAULT NULL comment '操作后状态',
    action_type     varchar(30)   NOT NULL comment '操作类型(CREATE=创建 UPDATE=编辑 PUBLISH=发布 PAUSE=暂停 RESUME=恢复 END=结束 DELETE=删除 STOCK_ADJUST=库存调整 REVOKE=紧急撤销)',
    change_summary  varchar(500)  DEFAULT NULL comment '变更摘要',
    reason          varchar(500)  DEFAULT NULL comment '原因',
    create_time     datetime      DEFAULT NULL comment '创建时间',
    PRIMARY KEY (id),
    KEY idx_coupon_id (coupon_id),
    KEY idx_operator_id (operator_id),
    KEY idx_shop_id (shop_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 comment '优惠券模板操作流水表';

-- ============================================================
-- 订单模块 (order)
-- ============================================================

-- 父订单（负责支付聚合和用户视角的完整订单）
CREATE TABLE IF NOT EXISTS cm_order (
    id                         bigint        NOT NULL AUTO_INCREMENT comment '父订单ID',
    order_no                   varchar(64)   NOT NULL comment '订单编号',
    user_id                    bigint        NOT NULL comment '下单用户ID',
    total_original_amount      decimal(18,2) NOT NULL DEFAULT 0.00 comment '商品原价总额',
    total_shop_discount_amount decimal(18,2) NOT NULL DEFAULT 0.00 comment '店铺券优惠总额',
    total_platform_discount_amount decimal(18,2) NOT NULL DEFAULT 0.00 comment '平台券优惠总额',
    goods_payable_amount       decimal(18,2) NOT NULL DEFAULT 0.00 comment '商品应付金额(原价减全部优惠)',
    freight_amount             decimal(18,2) NOT NULL DEFAULT 0.00 comment '运费金额',
    payable_amount             decimal(18,2) NOT NULL DEFAULT 0.00 comment '最终应付金额(商品应付加运费)',
    status                     varchar(20)   NOT NULL DEFAULT 'CREATING' comment '订单状态(CREATING=创建中 PENDING_PAYMENT=待支付 PAID=已支付 TIMEOUT=超时关闭 CANCELLED=已取消 PARTIAL_REFUNDED=部分退款 REFUNDED=已全额退款)',
    order_type                 varchar(20)   NOT NULL DEFAULT 'CART' comment '订单类型(CART=购物车结算 DIRECT=直接购买 SECKILL=秒杀结算)',
    address_snapshot           text          DEFAULT NULL comment '收货地址快照(JSON)',
    remark                     varchar(500)  DEFAULT NULL comment '订单备注',
    create_time                datetime      DEFAULT NULL comment '创建时间',
    update_time                datetime      DEFAULT NULL comment '更新时间',
    deleted                    tinyint(1)    NOT NULL DEFAULT 0 comment '是否删除(0=否 1=是)',
    PRIMARY KEY (id),
    UNIQUE KEY uk_order_no (order_no),
    KEY idx_user_id (user_id),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 comment '父订单表';

-- 店铺子订单（负责履约，每个父订单每个店铺最多一条）
CREATE TABLE IF NOT EXISTS cm_order_sub (
    id                         bigint        NOT NULL AUTO_INCREMENT comment '子订单ID',
    sub_order_no               varchar(64)   NOT NULL comment '子订单编号',
    parent_order_id            bigint        NOT NULL comment '父订单ID',
    shop_id                    bigint        NOT NULL comment '店铺ID',
    original_amount            decimal(18,2) NOT NULL DEFAULT 0.00 comment '商品原价金额',
    shop_discount_amount       decimal(18,2) NOT NULL DEFAULT 0.00 comment '店铺券优惠金额',
    platform_discount_amount   decimal(18,2) NOT NULL DEFAULT 0.00 comment '平台券优惠金额',
    payable_amount             decimal(18,2) NOT NULL DEFAULT 0.00 comment '商品应付金额',
    merchant_settlement_amount decimal(18,2) NOT NULL DEFAULT 0.00 comment '商家结算金额',
    remark                     varchar(500)  DEFAULT NULL comment '店铺订单备注(跨店铺聚合订单按店铺单独设置)',
    status                     varchar(20)   NOT NULL DEFAULT 'PENDING_SHIPMENT' comment '子订单状态(PENDING_SHIPMENT=待发货 SHIPPED=已发货 DELIVERED=已收货 COMPLETED=已完成 CANCELLED=已取消 REFUNDED=已退款)',
    create_time                datetime      DEFAULT NULL comment '创建时间',
    update_time                datetime      DEFAULT NULL comment '更新时间',
    deleted                    tinyint(1)    NOT NULL DEFAULT 0 comment '是否删除(0=否 1=是)',
    PRIMARY KEY (id),
    UNIQUE KEY uk_sub_order_no (sub_order_no),
    UNIQUE KEY uk_parent_shop (parent_order_id, shop_id),
    KEY idx_parent_order_id (parent_order_id),
    KEY idx_shop_id (shop_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 comment '店铺子订单表';

-- 订单明细（归属于子订单，保存商品展示与金额快照）
CREATE TABLE IF NOT EXISTS cm_order_item (
    id                        bigint        NOT NULL AUTO_INCREMENT comment '订单明细ID',
    sub_order_id              bigint        NOT NULL comment '所属子订单ID',
    product_id                bigint        NOT NULL comment '商品SPU ID',
    sku_id                    bigint        NOT NULL comment 'SKU ID',
    shop_id                   bigint        NOT NULL comment '店铺ID',
    category_id               bigint        DEFAULT NULL comment '商品分类ID',
    product_name              varchar(200)  DEFAULT NULL comment '商品名称(快照)',
    product_image             varchar(500)  DEFAULT NULL comment '商品图片(快照)',
    spec_snapshot             varchar(500)  DEFAULT NULL comment '规格快照',
    unit_price                decimal(18,2) NOT NULL comment '成交单价(快照)',
    quantity                  int           NOT NULL comment '购买数量',
    original_amount           decimal(18,2) NOT NULL DEFAULT 0.00 comment '原价金额(单价乘数量)',
    shop_discount_amount      decimal(18,2) NOT NULL DEFAULT 0.00 comment '店铺券优惠金额',
    platform_discount_amount  decimal(18,2) NOT NULL DEFAULT 0.00 comment '平台券优惠金额',
    payable_amount            decimal(18,2) NOT NULL DEFAULT 0.00 comment '应付金额',
    create_time               datetime      DEFAULT NULL comment '创建时间',
    update_time               datetime      DEFAULT NULL comment '更新时间',
    deleted                   tinyint(1)    NOT NULL DEFAULT 0 comment '是否删除(0=否 1=是)',
    PRIMARY KEY (id),
    KEY idx_sub_order_id (sub_order_id),
    KEY idx_sku_id (sku_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 comment '订单明细表';

-- 订单优惠关联（记录本次订单实际使用的用户券，不重新读取当前模板计算历史金额）
CREATE TABLE IF NOT EXISTS cm_order_coupon (
    id                        bigint        NOT NULL AUTO_INCREMENT comment '订单优惠关联ID',
    parent_order_id           bigint        NOT NULL comment '父订单ID',
    sub_order_id              bigint        DEFAULT NULL comment '子订单ID(平台券为NULL)',
    user_coupon_id            bigint        NOT NULL comment '用户券ID',
    coupon_id                 bigint        NOT NULL comment '优惠券模板ID',
    owner_type                varchar(20)   NOT NULL comment '券归属(PLATFORM/SHOP)',
    shop_id                   bigint        DEFAULT NULL comment '店铺ID',
    type_snapshot             varchar(20)   NOT NULL comment '券类型快照',
    threshold_snapshot        decimal(18,2) NOT NULL DEFAULT 0.00 comment '门槛快照',
    discount_amount_snapshot  decimal(18,2) DEFAULT NULL comment '金额优惠快照',
    discount_rate_snapshot    decimal(8,6)  DEFAULT NULL comment '比例优惠快照',
    applicable_amount         decimal(18,2) NOT NULL DEFAULT 0.00 comment '适用金额',
    discount_amount           decimal(18,2) NOT NULL DEFAULT 0.00 comment '实际优惠金额',
    create_time               datetime      DEFAULT NULL comment '创建时间',
    update_time               datetime      DEFAULT NULL comment '更新时间',
    deleted                   tinyint(1)    NOT NULL DEFAULT 0 comment '是否删除(0=否 1=是)',
    PRIMARY KEY (id),
    KEY idx_parent_order_id (parent_order_id),
    KEY idx_sub_order_id (sub_order_id),
    KEY idx_user_coupon_id (user_coupon_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 comment '订单优惠关联表';

CREATE TABLE IF NOT EXISTS cm_cart (
    id           bigint        NOT NULL AUTO_INCREMENT comment '购物车项ID',
    user_id      bigint        NOT NULL comment '用户ID',
    shop_id      bigint        DEFAULT NULL comment '所属店铺ID',
    product_id     bigint        NOT NULL comment '商品ID',
    sku_id       bigint        DEFAULT NULL comment 'SKU ID',
    product_name   varchar(200)  DEFAULT NULL comment '商品名称',
    product_image  varchar(500)  DEFAULT NULL comment '商品图片URL',
    price        decimal(10,2) NOT NULL comment '加入时单价',
    quantity     int           NOT NULL DEFAULT '1' comment '数量',
    selected     tinyint       DEFAULT '1' comment '是否选中(0=未选中 1=已选中)',
    create_time  datetime      DEFAULT NULL comment '加入时间',
    update_time  datetime      DEFAULT NULL comment '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_sku (user_id, sku_id),
    KEY idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 comment '购物车表';

-- ============================================================
-- 审核模块 (通用审核单)
-- ============================================================

-- 通用审核单（biz_type 区分审核对象类型，当前用于秒杀商品审核）
CREATE TABLE IF NOT EXISTS cm_audit_record (
    id           bigint       NOT NULL AUTO_INCREMENT comment '审核单ID',
    biz_type     varchar(40)  NOT NULL comment '审核对象类型(SECKILL_PRODUCT=秒杀商品)',
    biz_id       bigint       NOT NULL comment '审核对象ID',
    submitter_id bigint       DEFAULT NULL comment '提交人用户ID',
    auditor_id   bigint       DEFAULT NULL comment '审核人用户ID',
    status       varchar(20)  NOT NULL DEFAULT 'PENDING' comment '审核状态(PENDING=待审核 APPROVED=已通过 REJECTED=已驳回 WITHDRAWN=已撤回)',
    audit_remark varchar(200) DEFAULT NULL comment '审核意见/驳回原因',
    extra_info   json         DEFAULT NULL comment '扩展信息(提交时快照)',
    create_time  datetime     DEFAULT NULL comment '创建时间',
    update_time  datetime     DEFAULT NULL comment '更新时间',
    deleted      tinyint(1)   NOT NULL DEFAULT 0 comment '是否删除(0=否 1=是)',
    PRIMARY KEY (id),
    KEY idx_biz (biz_type, biz_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 comment '通用审核单表';

-- ============================================================
-- 秒杀模块 (seckill)
-- ============================================================

-- 秒杀活动（运营排期，发布=开放商家报名，报名截止=start_time）
CREATE TABLE IF NOT EXISTS cm_seckill_activity (
    id             bigint       NOT NULL AUTO_INCREMENT comment '秒杀活动ID',
    name           varchar(200) NOT NULL comment '活动名称',
    description    varchar(500) DEFAULT NULL comment '活动描述',
    start_time     datetime     NOT NULL comment '活动开始时间(创建时须晚于当前24小时)',
    end_time       datetime     NOT NULL comment '活动结束时间(固定为开始时间加1小时)',
    max_products   int          NOT NULL DEFAULT 50 comment '每活动最大商品数(创建时可配1~500,默认50)',
    status         varchar(20)  NOT NULL DEFAULT 'DRAFT' comment '活动状态(DRAFT=草稿 PUBLISHED=报名中 RUNNING=进行中 FINISHED=已结束 CANCELLED=已取消)',
    create_user_id bigint       DEFAULT NULL comment '创建人用户ID',
    publish_user_id bigint      DEFAULT NULL comment '发布人用户ID',
    publish_time   datetime     DEFAULT NULL comment '发布时间',
    create_time    datetime     DEFAULT NULL comment '创建时间',
    update_time    datetime     DEFAULT NULL comment '更新时间',
    deleted        tinyint(1)   NOT NULL DEFAULT 0 comment '是否删除(0=否 1=是)',
    PRIMARY KEY (id),
    KEY idx_status_start (status, start_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 comment '秒杀活动表';

-- 秒杀商品（商家按SKU报名，运营审核；秒杀库存为独立池，不接入 cm_stock_op_log）
CREATE TABLE IF NOT EXISTS cm_seckill_product (
    id            bigint        NOT NULL AUTO_INCREMENT comment '秒杀商品ID',
    activity_id   bigint        NOT NULL comment '所属活动ID',
    product_id    bigint        NOT NULL comment '商品SPU ID(冗余)',
    sku_id        bigint        NOT NULL comment 'SKU ID(下单粒度)',
    shop_id       bigint        NOT NULL comment '店铺ID(冗余)',
    seckill_price decimal(10,2) NOT NULL comment '秒杀价格',
    stock         int           NOT NULL DEFAULT 0 comment '秒杀库存(运行时权威在Redis，支付确认时落库扣减)',
    sold_count    int           NOT NULL DEFAULT 0 comment '已确认售出数量',
    audit_status  varchar(20)   NOT NULL DEFAULT 'PENDING' comment '审核状态(PENDING=待审核 APPROVED=已通过 REJECTED=已驳回)',
    create_time   datetime      DEFAULT NULL comment '创建时间',
    update_time   datetime      DEFAULT NULL comment '更新时间',
    deleted       tinyint(1)    NOT NULL DEFAULT 0 comment '是否删除(0=否 1=是)',
    PRIMARY KEY (id),
    UNIQUE KEY uk_activity_sku (activity_id, sku_id),
    KEY idx_activity (activity_id),
    KEY idx_shop (shop_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 comment '秒杀商品表';

-- 秒杀购买记录（唯一键权威保证一人一单，异步建真实订单）
CREATE TABLE IF NOT EXISTS cm_seckill_order (
    id            bigint        NOT NULL comment '秒杀单ID(雪花)',
    activity_id   bigint        NOT NULL comment '活动ID',
    product_id    bigint        NOT NULL comment '商品SPU ID(冗余)',
    sku_id        bigint        NOT NULL comment 'SKU ID',
    user_id       bigint        NOT NULL comment '购买用户ID',
    quantity      int           NOT NULL DEFAULT 1 comment '购买数量(固定1)',
    seckill_price decimal(10,2) NOT NULL comment '成交价快照',
    order_no      varchar(64)   DEFAULT NULL comment '真实订单号(抢购时预生成)',
    status        varchar(20)   NOT NULL DEFAULT 'PENDING_PAYMENT' comment '秒杀单状态(PENDING_PAYMENT=待支付 PAID=已支付 CANCELLED=已取消 REFUNDED=已退款)',
    create_time   datetime      DEFAULT NULL comment '创建时间',
    update_time   datetime      DEFAULT NULL comment '更新时间',
    deleted       tinyint(1)    NOT NULL DEFAULT 0 comment '是否删除(0=否 1=是)',
    PRIMARY KEY (id),
    UNIQUE KEY uk_activity_sku_user (activity_id, sku_id, user_id),
    KEY idx_user (user_id),
    KEY idx_order_no (order_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 comment '秒杀购买记录表';

-- ============================================================
-- 库存模块 (product) — 普通商品库存预占/释放流水
-- ============================================================

-- 普通商品库存操作流水（秒杀库存不接入，订单号/SKU/操作幂等）
CREATE TABLE IF NOT EXISTS cm_stock_op_log (
    id           bigint       NOT NULL AUTO_INCREMENT comment '库存操作记录ID',
    order_no     varchar(64)  NOT NULL comment '订单号',
    sku_id       bigint       NOT NULL comment 'SKU ID',
    product_id   bigint       NOT NULL comment '商品SPU ID',
    shop_id      bigint       DEFAULT NULL comment '店铺ID',
    op_type      varchar(20)  NOT NULL comment '操作类型(RESERVE=预占 CONFIRM=确认 RELEASE=释放 COMPENSATE=补偿)',
    quantity     int          NOT NULL comment '操作数量(正数，方向由op_type表达)',
    before_stock int          DEFAULT NULL comment '操作前库存',
    after_stock  int          DEFAULT NULL comment '操作后库存',
    source       varchar(20)  NOT NULL DEFAULT 'ORDER' comment '来源(ORDER=订单 COMPENSATE=补偿任务)',
    operator_id  bigint       DEFAULT NULL comment '操作人ID(系统操作为NULL)',
    reason       varchar(500) DEFAULT NULL comment '原因',
    create_time  datetime     DEFAULT NULL comment '创建时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_order_sku_op (order_no, sku_id, op_type),
    KEY idx_sku_id (sku_id),
    KEY idx_order_no (order_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 comment '普通商品库存操作流水表';

-- ============================================================
-- 消息可靠性模块 (outbox / 幂等消费)
-- ============================================================

-- 通用本地消息 Outbox（所有服务共用，app_name 区分来源）
CREATE TABLE IF NOT EXISTS cm_message_outbox (
    id              bigint       NOT NULL AUTO_INCREMENT comment 'Outbox记录ID',
    app_name        varchar(50)  NOT NULL comment '来源服务(spring.application.name)',
    event_type      varchar(50)  NOT NULL comment '事件类型(建议带域前缀，如 FILE_BIND)',
    biz_id          varchar(64)  DEFAULT NULL comment '业务ID',
    payload         text         comment '事件载荷(字符串原样写入，对象序列化为 JSON)',
    status          varchar(20)  NOT NULL DEFAULT 'PENDING' comment '状态(PENDING/PROCESSING/DONE/FAILED)',
    retry_count     int          NOT NULL DEFAULT 0 comment '已重试次数',
    next_retry_time datetime     DEFAULT NULL comment '下次重试时间',
    create_time     datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP comment '创建时间',
    PRIMARY KEY (id),
    KEY idx_status (status, next_retry_time),
    KEY idx_type_status (event_type, status),
    KEY idx_app_status (app_name, status),
    KEY idx_biz (biz_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 comment '通用本地消息 Outbox 表';

-- ============================================================
-- RBAC 权限模块 — 种子数据
-- ============================================================

-- 内置角色：admin（拥有所有权限）
INSERT INTO cm_role (id, name, built_in, enabled, description) VALUES (1, 'admin', 1, 1, '系统管理员');

-- API 资源权限
INSERT INTO cm_resource (id, code, name, type) VALUES
    (1,  '*',                               '超级管理员（所有权限）',              'api'),
    (10, 'product:view',                      '商品查询',                          'api'),
    (11, 'product:create',                    '商品创建',                          'api'),
    (12, 'product:update',                    '商品更新',                          'api'),
    (13, 'product:delete',                    '商品删除',                          'api'),
    (20, 'order:view',                      '订单查询',                          'api'),
    (21, 'order:create',                    '订单创建',                          'api'),
    (22, 'order:update',                    '订单更新',                          'api'),
    (23, 'order:delete',                    '订单删除',                          'api'),
    (30, 'user:view',                       '用户查询',                          'api'),
    (31, 'user:create',                     '用户创建',                          'api'),
    (32, 'user:update',                     '用户更新',                          'api'),
    (33, 'user:delete',                     '用户删除',                          'api'),
    (40, 'shop:manage',                    '店铺管理',                          'api'),
    (50, 'seckill:manage',                  '秒杀管理',                          'api'),
    (51, 'seckill:activity:create',         '秒杀活动创建',                      'api'),
    (52, 'seckill:activity:publish',        '秒杀活动发布',                      'api'),
    (53, 'seckill:activity:cancel',         '秒杀活动取消',                      'api'),
    (54, 'seckill:activity:view',           '秒杀活动查询',                      'api'),
    (55, 'seckill:product:submit',          '秒杀商品提交',                      'api'),
    (56, 'seckill:product:audit',           '秒杀商品审核',                      'api'),
    (57, 'seckill:product:view',            '秒杀商品查询',                      'api'),
    (58, 'seckill:order:view',              '秒杀订单查询',                      'api'),
    (60, 'coupon:manage',                   '优惠券管理',                        'api'),
    (61, 'coupon:template:create',          '优惠券模板创建',                    'api'),
    (62, 'coupon:template:view',            '优惠券模板查询',                    'api'),
    (63, 'coupon:template:publish',         '优惠券模板发布',                    'api'),
    (64, 'coupon:template:pause',           '优惠券模板暂停',                    'api'),
    (65, 'coupon:template:end',             '优惠券模板结束',                    'api'),
    (66, 'coupon:template:delete',          '优惠券模板删除',                    'api'),
    (67, 'coupon:user:view',                '用户券查询',                        'api'),
    (68, 'coupon:user:revoke',              '用户券撤销',                        'api'),
    (69, 'coupon:stats:view',               '优惠券统计',                        'api');

-- admin 角色拥有所有资源权限
INSERT INTO cm_role_resource (role_id, resource_id) VALUES
    (1, 1),
    (1, 10), (1, 11), (1, 12), (1, 13),
    (1, 20), (1, 21), (1, 22), (1, 23),
    (1, 30), (1, 31), (1, 32), (1, 33),
    (1, 40),
    (1, 50),
    (1, 51), (1, 52), (1, 53), (1, 54),
    (1, 55), (1, 56), (1, 57), (1, 58),
    (1, 60),
    (1, 61), (1, 62), (1, 63), (1, 64), (1, 65), (1, 66),
    (1, 67), (1, 68), (1, 69);

-- ============================================================
-- AI Agent 模块 (agent)
-- ============================================================

CREATE TABLE IF NOT EXISTS cm_chat_session (
    id          bigint       NOT NULL comment '会话ID',
    session_id  varchar(64)  NOT NULL comment '会话唯一标识',
    user_id     bigint       NOT NULL comment '用户ID',
    title       varchar(200) DEFAULT NULL comment '会话标题',
    create_time datetime     DEFAULT NULL comment '创建时间',
    update_time datetime     DEFAULT NULL comment '更新时间',
    deleted     tinyint(1)   DEFAULT '0' comment '是否删除(0=否 1=是)',
    PRIMARY KEY (id),
    UNIQUE KEY uk_session_id (session_id),
    KEY idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 comment 'AI聊天会话表';

-- ============================================================
-- IM 模块 (im) —— 买家与商家实时聊天
-- ============================================================

CREATE TABLE IF NOT EXISTS cm_im_session (
    id              bigint       NOT NULL AUTO_INCREMENT comment '会话ID',
    buyer_id        bigint       NOT NULL comment '买家用户ID',
    merchant_id     bigint       NOT NULL comment '商家用户ID',
    buyer_pinned    tinyint(1)   NOT NULL DEFAULT '0' comment '买家是否置顶(0=否 1=是)',
    merchant_pinned tinyint(1)   NOT NULL DEFAULT '0' comment '商家是否置顶(0=否 1=是)',
    buyer_hidden    tinyint(1)   NOT NULL DEFAULT '0' comment '买家是否隐藏会话(0=否 1=是)',
    merchant_hidden tinyint(1)   NOT NULL DEFAULT '0' comment '商家是否隐藏会话(0=否 1=是)',
    create_time     datetime     DEFAULT NULL comment '创建时间',
    update_time     datetime     DEFAULT NULL comment '更新时间',
    deleted         tinyint(1)   DEFAULT '0' comment '是否删除(0=否 1=是)',
    PRIMARY KEY (id),
    UNIQUE KEY uk_buyer_merchant (buyer_id, merchant_id),
    KEY idx_merchant_id (merchant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 comment '买家商家聊天会话表';

CREATE TABLE IF NOT EXISTS cm_im_message (
    id          bigint       NOT NULL AUTO_INCREMENT comment '消息ID',
    session_id  bigint       NOT NULL comment '会话ID',
    sender_id   bigint       NOT NULL comment '发送者用户ID',
    content     varchar(2000) NOT NULL comment '消息内容（文本、图片/视频URL或商品卡片JSON）',
    type        varchar(20)  NOT NULL DEFAULT 'text' comment '消息类型(text/image/video/product)',
    is_read     tinyint(1)   DEFAULT '0' comment '是否已读(0=否 1=是)',
    create_time datetime     DEFAULT NULL comment '创建时间',
    deleted     tinyint(1)   DEFAULT '0' comment '是否删除(0=否 1=是)',
    PRIMARY KEY (id),
    KEY idx_session_id (session_id),
    KEY idx_sender_id (sender_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 comment '买家商家聊天消息表';

-- ============================================================
-- 模拟支付模块 (mock-payment) —— 微信支付 API v3 模拟渠道
-- ============================================================

CREATE TABLE IF NOT EXISTS cm_mock_payment_order (
    id                bigint       NOT NULL AUTO_INCREMENT comment '支付单ID',
    out_trade_no      varchar(32)  NOT NULL comment '商户订单号',
    transaction_id    varchar(64)  DEFAULT NULL comment '模拟微信支付订单号',
    appid             varchar(32)  NOT NULL comment '公众号APPID',
    mchid             varchar(32)  NOT NULL comment '商户号',
    description       varchar(127) NOT NULL comment '商品描述',
    notify_url        varchar(500) NOT NULL comment '异步回调地址',
    attach            varchar(127) DEFAULT NULL comment '附加数据',
    amount_total      int          NOT NULL comment '订单总金额(分)',
    currency          varchar(16)  NOT NULL DEFAULT 'CNY' comment '币种',
    payer_openid      varchar(128) DEFAULT NULL comment '支付者标识',
    trade_type        varchar(16)  NOT NULL DEFAULT 'NATIVE' comment '交易类型(JSAPI/NATIVE)',
    trade_state       varchar(32)  NOT NULL DEFAULT 'NOTPAY' comment '交易状态(NOTPAY/SUCCESS/CLOSED)',
    trade_state_desc  varchar(256) DEFAULT NULL comment '交易状态描述',
    prepay_id         varchar(64)  DEFAULT NULL comment 'JSAPI预支付会话标识',
    code_url          varchar(128) DEFAULT NULL comment 'Native二维码链接',
    success_time      datetime     DEFAULT NULL comment '支付完成时间',
    expire_time       datetime     DEFAULT NULL comment '支付截止时间',
    create_time       datetime     DEFAULT NULL comment '创建时间',
    update_time       datetime     DEFAULT NULL comment '更新时间',
    deleted           tinyint(1)   NOT NULL DEFAULT 0 comment '是否删除(0=否 1=是)',
    PRIMARY KEY (id),
    UNIQUE KEY uk_out_trade_no (out_trade_no),
    UNIQUE KEY uk_transaction_id (transaction_id),
    KEY idx_trade_state (trade_state),
    KEY idx_expire_time (expire_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 comment '模拟支付单表';

CREATE TABLE IF NOT EXISTS cm_mock_payment_notify (
    id                bigint        NOT NULL AUTO_INCREMENT comment '回调记录ID',
    out_trade_no      varchar(32)   NOT NULL comment '商户订单号',
    event_type        varchar(32)   NOT NULL comment '回调事件类型',
    notify_url        varchar(500)  NOT NULL comment '回调地址',
    request_body      mediumtext    NOT NULL comment '加密后的回调报文',
    signature         varchar(1024) DEFAULT NULL comment '最近一次签名值',
    response_body     varchar(2000) DEFAULT NULL comment '最近一次应答报文',
    status            varchar(20)   NOT NULL DEFAULT 'PENDING' comment '投递状态(PENDING/SUCCESS/FAILED)',
    retry_count       int           NOT NULL DEFAULT 0 comment '已重试次数',
    next_retry_time   datetime      DEFAULT NULL comment '下次重试时间',
    last_error        varchar(500)  DEFAULT NULL comment '最近一次失败原因',
    create_time       datetime      DEFAULT NULL comment '创建时间',
    update_time       datetime      DEFAULT NULL comment '更新时间',
    deleted           tinyint(1)    NOT NULL DEFAULT 0 comment '是否删除(0=否 1=是)',
    PRIMARY KEY (id),
    KEY idx_out_trade_no (out_trade_no),
    KEY idx_status_retry (status, retry_count, next_retry_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 comment '模拟支付回调记录表';

-- ============================================================
-- 个人中心扩展表
-- 覆盖账户设置、积分权益、浏览记录等页面所需数据。
-- 可重复执行（CREATE TABLE IF NOT EXISTS）。
-- ============================================================

USE cm_mall;

CREATE TABLE IF NOT EXISTS cm_user_profile
(
    user_id             bigint                                not null comment '用户ID'
        primary key,
    gender              varchar(10) default '保密'            null comment '性别：男/女/保密',
    birthday            date                                  null comment '生日',
    profile_visibility  varchar(20) default 'PUBLIC'          not null comment '个人主页可见范围：PUBLIC/PRIVATE',
    purchase_visibility varchar(20) default 'PRIVATE'         not null comment '购买记录可见范围：PUBLIC/PRIVATE',
    notify_order        tinyint(1)  default 1                 not null comment '订单通知开关',
    notify_promotion    tinyint(1)  default 1                 not null comment '促销通知开关',
    notify_logistics    tinyint(1)  default 1                 not null comment '物流通知开关',
    notify_community    tinyint(1)  default 0                 not null comment '社区通知开关',
    recommend_enabled   tinyint(1)  default 1                 not null comment '个性化推荐开关',
    create_time         datetime    default CURRENT_TIMESTAMP null comment '创建时间',
    update_time         datetime    default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间'
) comment '用户扩展资料与设置' engine = InnoDB
                          collate = utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS cm_user_point
(
    user_id      bigint   not null comment '用户ID'
        primary key,
    balance      int      default 0 not null comment '当前可用积分',
    total_earned int      default 0 not null comment '累计获得积分',
    total_spent  int      default 0 not null comment '累计消耗积分',
    update_time  datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间'
) comment '用户积分账户' engine = InnoDB
                        collate = utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS cm_user_point_log
(
    id            bigint auto_increment comment '流水ID'
        primary key,
    user_id       bigint       not null comment '用户ID',
    change_amount int          not null comment '积分变动值（获得为正，消耗为负）',
    balance_after int          not null comment '变动后余额',
    type          varchar(32)  not null comment '流水类型',
    description   varchar(255) null comment '流水描述',
    create_time   datetime     default CURRENT_TIMESTAMP null comment '创建时间',
    idempotency_key varchar(64) null comment '幂等键',
    key idx_user_point_log_user_time (user_id, create_time),
    constraint uk_user_point_log_idempotency unique (user_id, idempotency_key)
) comment '用户积分流水' engine = InnoDB
                          collate = utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS cm_user_browse_history
(
    id            bigint auto_increment comment '浏览记录ID'
        primary key,
    user_id       bigint         not null comment '用户ID',
    product_id    bigint         not null comment '商品ID',
    product_name  varchar(200)   null comment '商品名称快照',
    product_image varchar(500)   null comment '商品图片快照',
    price         decimal(10, 2) null comment '商品价格快照',
    browse_time   datetime       not null comment '浏览时间',
    update_time   datetime       default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    constraint uk_user_browse_product
        unique (user_id, product_id),
    key idx_user_browse_time (user_id, browse_time)
) comment '用户商品浏览记录' engine = InnoDB
                              collate = utf8mb4_unicode_ci;

-- ============================================================
-- 个人中心补齐能力
-- 包含账户安全、积分签到/任务/兑换、地址区域数据。
-- 建表语句可重复执行（CREATE TABLE IF NOT EXISTS）；
-- 末尾 ALTER TABLE 用于旧库升级，字段/索引已存在时需人工跳过。
-- ============================================================

USE cm_mall;

CREATE TABLE IF NOT EXISTS cm_user_security
(
    user_id                  bigint       not null comment '用户ID'
        primary key,
    pay_password_hash        varchar(255) null comment '支付密码BCrypt哈希',
    pay_password_set_time    datetime     null comment '支付密码设置/修改时间',
    real_name                varchar(64)  null comment '真实姓名密文（AES-GCM）',
    real_name_mask           varchar(32)  null comment '真实姓名脱敏展示值',
    id_card_no               varchar(255) null comment '身份证号密文（AES-GCM）',
    id_card_mask             varchar(32)  null comment '身份证号脱敏展示值',
    real_name_status         varchar(20)  not null default 'UNVERIFIED' comment '实名状态：UNVERIFIED/PENDING/VERIFIED/REJECTED',
    real_name_submit_time    datetime     null comment '实名提交时间',
    real_name_verify_time    datetime     null comment '实名核验完成时间',
    real_name_reject_reason  varchar(255) null comment '实名驳回原因',
    create_time              datetime     not null default CURRENT_TIMESTAMP comment '创建时间',
    update_time              datetime     not null default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP comment '更新时间',
    key idx_real_name_status (real_name_status)
) comment '用户账户安全信息' engine = InnoDB
                          collate = utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS cm_user_sign_in
(
    id            bigint auto_increment comment '签到ID'
        primary key,
    user_id       bigint   not null comment '用户ID',
    sign_date     date     not null comment '签到日期',
    streak_days   int      not null comment '连续签到天数',
    reward_points int      not null comment '本次奖励积分',
    create_time   datetime not null default CURRENT_TIMESTAMP comment '创建时间',
    constraint uk_user_sign_date unique (user_id, sign_date)
) comment '用户签到记录' engine = InnoDB
                        collate = utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS cm_point_task
(
    id            bigint auto_increment comment '任务ID'
        primary key,
    code          varchar(50)  not null comment '任务编码',
    name          varchar(100) not null comment '任务名称',
    description   varchar(255) null comment '任务说明',
    reward_points int          not null comment '奖励积分',
    frequency     varchar(20)  not null comment '频率：DAILY/ONCE',
    enabled       tinyint(1)   not null default 1 comment '是否启用',
    sort          int          not null default 0 comment '排序',
    create_time   datetime     not null default CURRENT_TIMESTAMP comment '创建时间',
    update_time   datetime     not null default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP comment '更新时间',
    constraint uk_point_task_code unique (code)
) comment '积分任务定义' engine = InnoDB
                        collate = utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS cm_point_task_record
(
    id            bigint auto_increment comment '任务记录ID'
        primary key,
    user_id       bigint      not null comment '用户ID',
    task_code     varchar(50) not null comment '任务编码',
    task_date     date        not null comment '任务日期',
    status        varchar(20) not null comment '状态：COMPLETED/CLAIMED',
    reward_points int         not null comment '实际发放积分',
    create_time   datetime    not null default CURRENT_TIMESTAMP comment '完成时间',
    claim_time    datetime    null comment '领取时间',
    constraint uk_user_task_date unique (user_id, task_code, task_date)
) comment '用户积分任务记录' engine = InnoDB
                            collate = utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS cm_point_exchange_item
(
    id          bigint auto_increment comment '兑换商品ID'
        primary key,
    name        varchar(100) not null comment '商品名称',
    type        varchar(20)  not null comment '类型：COUPON/PHYSICAL',
    points_cost int          not null comment '所需积分',
    coupon_id   bigint       null comment '优惠券模板ID',
    stock       int          not null default 0 comment '剩余库存',
    enabled     tinyint(1)   not null default 1 comment '是否上架',
    sort        int          not null default 0 comment '排序',
    create_time datetime     not null default CURRENT_TIMESTAMP comment '创建时间',
    update_time datetime     not null default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP comment '更新时间'
) comment '积分兑换商品' engine = InnoDB
                        collate = utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS cm_point_exchange_order
(
    id              bigint auto_increment comment '兑换记录ID'
        primary key,
    user_id         bigint      not null comment '用户ID',
    item_id         bigint      not null comment '兑换商品ID',
    points_cost     int         not null comment '消耗积分快照',
    status          varchar(20) not null comment '状态：SUCCESS/FAILED',
    user_coupon_id  bigint      null comment '发放的用户券ID',
    idempotency_key varchar(64) null comment '幂等键',
    create_time     datetime    not null default CURRENT_TIMESTAMP comment '创建时间',
    constraint uk_user_exchange_key unique (user_id, idempotency_key)
) comment '积分兑换记录' engine = InnoDB
                        collate = utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS cm_region
(
    code        varchar(12) not null comment '行政区代码'
        primary key,
    parent_code varchar(12) null comment '父级代码',
    name        varchar(64) not null comment '名称',
    level       tinyint     not null comment '层级：1省/2市/3区县',
    sort        int         not null default 0 comment '排序',
    enabled     tinyint(1)  not null default 1 comment '是否启用',
    key idx_parent_level (parent_code, level, sort)
) comment '行政区划' engine = InnoDB
                    collate = utf8mb4_unicode_ci;

-- 文件台账：记录上传文件的归属与生命周期，用于支撑临时文件回收
CREATE TABLE IF NOT EXISTS cm_file
(
    id           bigint       NOT NULL AUTO_INCREMENT COMMENT '文件ID',
    object_key   varchar(255) NOT NULL COMMENT '对象存储 key',
    url          varchar(500) NOT NULL COMMENT '访问地址',
    origin_name  varchar(255)          DEFAULT NULL COMMENT '原始文件名',
    content_type varchar(100)          DEFAULT NULL COMMENT 'MIME 类型',
    size         bigint                DEFAULT NULL COMMENT '文件大小（字节）',
    owner_id     bigint                DEFAULT NULL COMMENT '上传用户ID',
    biz_type     varchar(30)           DEFAULT NULL COMMENT '业务类型：GENERAL/AVATAR/SHOP_ONBOARDING/CHAT',
    status       varchar(20)  NOT NULL DEFAULT 'PENDING' COMMENT '状态：PENDING/BOUND/ORPHAN',
    create_time  datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time  datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted      tinyint(1)   NOT NULL DEFAULT 0 COMMENT '是否删除(0=否 1=是)',
    PRIMARY KEY (id),
    UNIQUE KEY uk_object_key (object_key),
    KEY idx_status_create (status, create_time),
    KEY idx_owner (owner_id),
    KEY idx_url (url)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT '文件台账';
