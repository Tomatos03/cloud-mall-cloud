-- ============================================================
-- Outbox 表结构标准化（已有库增量脚本）
-- 将旧的"发布型"结构迁移为固定九列：
-- app_name / event_type / biz_id / payload / status / retry_count /
-- next_retry_time / create_time
-- 脚本可重复执行；全新库执行 01-schema.sql 后已是新结构，本脚本不产生变化。
-- MySQL 不支持 DROP/RENAME COLUMN IF EXISTS，故通过 information_schema 动态执行。
-- ============================================================

USE cm_mall;

-- 补全空创建时间，便于后续改为 NOT NULL
UPDATE cm_message_outbox SET create_time = NOW() WHERE create_time IS NULL;

-- 旧状态值映射：PUBLISHED → DONE，DEAD → FAILED
SET @ddl = (
    SELECT IF(COUNT(*) > 0,
              'UPDATE cm_message_outbox SET publish_status = ''DONE'' WHERE publish_status = ''PUBLISHED''',
              'SELECT 1')
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'cm_message_outbox'
      AND COLUMN_NAME = 'publish_status'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl = (
    SELECT IF(COUNT(*) > 0,
              'UPDATE cm_message_outbox SET publish_status = ''FAILED'' WHERE publish_status = ''DEAD''',
              'SELECT 1')
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'cm_message_outbox'
      AND COLUMN_NAME = 'publish_status'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 重命名状态列并收敛字段类型
SET @ddl = (
    SELECT IF(COUNT(*) > 0,
              'ALTER TABLE cm_message_outbox CHANGE COLUMN publish_status status varchar(20) NOT NULL DEFAULT ''PENDING'' COMMENT ''状态(PENDING/PROCESSING/DONE/FAILED)'', MODIFY COLUMN payload text COMMENT ''事件载荷(字符串原样写入，对象序列化为 JSON)'', MODIFY COLUMN biz_id varchar(64) DEFAULT NULL COMMENT ''业务ID'', MODIFY COLUMN create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT ''创建时间''',
              'SELECT 1')
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'cm_message_outbox'
      AND COLUMN_NAME = 'publish_status'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 删除发布型结构遗留的索引与列
SET @ddl = (
    SELECT IF(COUNT(*) > 0,
              'ALTER TABLE cm_message_outbox DROP INDEX uk_event_id, DROP INDEX idx_app_status, DROP INDEX idx_next_retry_time, DROP COLUMN event_id, DROP COLUMN topic, DROP COLUMN biz_type, DROP COLUMN publish_time, DROP COLUMN last_error, DROP COLUMN update_time',
              'SELECT 1')
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'cm_message_outbox'
      AND COLUMN_NAME = 'event_id'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 补齐标准索引
SET @ddl = (
    SELECT IF(COUNT(*) = 0,
              'ALTER TABLE cm_message_outbox ADD KEY idx_status (status, next_retry_time), ADD KEY idx_type_status (event_type, status), ADD KEY idx_app_status (app_name, status), ADD KEY idx_biz (biz_id)',
              'SELECT 1')
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'cm_message_outbox'
      AND INDEX_NAME = 'idx_type_status'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
