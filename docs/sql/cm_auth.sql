-- ============================================================
-- cm_auth — 用户认证与授权数据库
-- 来源于 cloud-mall 原始项目的 user / roles / user_roles 表
-- ============================================================

CREATE DATABASE IF NOT EXISTS cm_auth DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE cm_auth;

-- -----------------------------------------------------------
-- cm_auth_user — 用户认证表
-- 对应原表: user
-- -----------------------------------------------------------
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
    constraint uk_username
        unique (username)
) comment '用户认证表' engine = InnoDB
                       collate = utf8mb4_unicode_ci;

-- -----------------------------------------------------------
-- cm_auth_role — 角色表
-- 对应原表: roles
-- -----------------------------------------------------------
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

-- -----------------------------------------------------------
-- cm_auth_user_role — 用户角色关联表
-- 对应原表: user_roles
-- -----------------------------------------------------------
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

create index idx_auth_user_role_role_id
    on cm_auth_user_role (role_id);
