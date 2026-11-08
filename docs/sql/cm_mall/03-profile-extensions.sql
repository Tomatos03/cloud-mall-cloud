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
    key idx_user_point_log_user_time (user_id, create_time)
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

-- ------------------------------------------------------------
-- 测试/演示种子：为已有用户初始化积分账户与少量流水
-- 可重复执行，不覆盖已有积分余额。
-- ------------------------------------------------------------
INSERT INTO cm_user_point (user_id, balance, total_earned, total_spent)
SELECT id, 6820, 6820, 0
FROM cm_user
ON DUPLICATE KEY UPDATE user_id = user_id;

INSERT INTO cm_user_point_log (user_id, change_amount, balance_after, type, description, create_time)
SELECT u.id, 730, 6820, 'ORDER', '购买商品 获得积分', DATE_SUB(NOW(), INTERVAL 2 DAY)
FROM cm_user u
WHERE NOT EXISTS (
    SELECT 1 FROM cm_user_point_log l
    WHERE l.user_id = u.id AND l.type = 'ORDER' AND l.description = '购买商品 获得积分'
);

INSERT INTO cm_user_point_log (user_id, change_amount, balance_after, type, description, create_time)
SELECT u.id, 50, 6870, 'COMMENT', '评价订单 奖励', DATE_SUB(NOW(), INTERVAL 1 DAY)
FROM cm_user u
WHERE NOT EXISTS (
    SELECT 1 FROM cm_user_point_log l
    WHERE l.user_id = u.id AND l.type = 'COMMENT' AND l.description = '评价订单 奖励'
);

INSERT INTO cm_user_point_log (user_id, change_amount, balance_after, type, description, create_time)
SELECT u.id, -500, 6370, 'EXCHANGE', '积分兑换 50元优惠券', DATE_SUB(NOW(), INTERVAL 1 DAY)
FROM cm_user u
WHERE NOT EXISTS (
    SELECT 1 FROM cm_user_point_log l
    WHERE l.user_id = u.id AND l.type = 'EXCHANGE' AND l.description = '积分兑换 50元优惠券'
);
