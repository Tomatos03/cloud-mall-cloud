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
