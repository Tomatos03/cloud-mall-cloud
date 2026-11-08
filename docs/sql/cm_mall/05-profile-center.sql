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

-- ------------------------------------------------------------
-- 积分任务与兑换商品种子
-- ------------------------------------------------------------
INSERT INTO cm_point_task (code, name, description, reward_points, frequency, enabled, sort)
VALUES ('DAILY_SIGN_IN', '每日签到', '每天签到即可获得积分', 10, 'DAILY', 1, 10),
       ('DAILY_BROWSE', '浏览商品', '每天浏览任意商品即可获得积分', 5, 'DAILY', 1, 20),
       ('DAILY_COMMENT', '发表评价', '每天发表一条商品评价即可获得积分', 20, 'DAILY', 1, 30)
ON DUPLICATE KEY UPDATE name = VALUES(name),
                        description = VALUES(description),
                        reward_points = VALUES(reward_points),
                        frequency = VALUES(frequency),
                        enabled = VALUES(enabled),
                        sort = VALUES(sort);

INSERT INTO cm_point_exchange_item (id, name, type, points_cost, coupon_id, stock, enabled, sort)
VALUES (1, '平台满500减50券', 'COUPON', 500, 2, 100, 1, 10),
       (2, '数码分类9折券', 'COUPON', 300, 3, 100, 1, 20),
       (3, '平台无门槛立减5元券', 'COUPON', 100, 9, 100, 1, 30)
ON DUPLICATE KEY UPDATE name = VALUES(name),
                        points_cost = VALUES(points_cost),
                        coupon_id = VALUES(coupon_id),
                        stock = VALUES(stock),
                        enabled = VALUES(enabled),
                        sort = VALUES(sort);

-- 地址区域代码（可选，历史数据保留名称为准）
ALTER TABLE cm_address
    ADD COLUMN province_code varchar(12) NULL COMMENT '省级代码',
    ADD COLUMN city_code     varchar(12) NULL COMMENT '市级代码',
    ADD COLUMN district_code varchar(12) NULL COMMENT '区县代码';

-- 积分流水幂等键（订单支付等事件防止重复发分）
ALTER TABLE cm_user_point_log
    ADD COLUMN idempotency_key varchar(64) NULL COMMENT '幂等键';
CREATE UNIQUE INDEX uk_user_point_log_idempotency
    ON cm_user_point_log (user_id, idempotency_key);
