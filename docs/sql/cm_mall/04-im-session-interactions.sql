-- ============================================================
-- IM 会话交互扩展（按用户维度置顶 / 删除）
-- 已有数据库执行本脚本一次；脚本可重复执行。
-- MySQL 不支持 ADD COLUMN IF NOT EXISTS，故先查询
-- information_schema，再通过预处理语句动态执行 ALTER。
-- ============================================================

USE cm_mall;

SET @ddl = (
    SELECT IF(COUNT(*) = 0,
              'ALTER TABLE cm_im_session ADD COLUMN buyer_pinned tinyint(1) NOT NULL DEFAULT 0 COMMENT ''买家是否置顶(0=否 1=是)'' AFTER update_time',
              'SELECT 1')
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'cm_im_session'
      AND COLUMN_NAME = 'buyer_pinned'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl = (
    SELECT IF(COUNT(*) = 0,
              'ALTER TABLE cm_im_session ADD COLUMN merchant_pinned tinyint(1) NOT NULL DEFAULT 0 COMMENT ''商家是否置顶(0=否 1=是)'' AFTER buyer_pinned',
              'SELECT 1')
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'cm_im_session'
      AND COLUMN_NAME = 'merchant_pinned'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl = (
    SELECT IF(COUNT(*) = 0,
              'ALTER TABLE cm_im_session ADD COLUMN buyer_hidden tinyint(1) NOT NULL DEFAULT 0 COMMENT ''买家是否隐藏会话(0=否 1=是)'' AFTER merchant_pinned',
              'SELECT 1')
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'cm_im_session'
      AND COLUMN_NAME = 'buyer_hidden'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl = (
    SELECT IF(COUNT(*) = 0,
              'ALTER TABLE cm_im_session ADD COLUMN merchant_hidden tinyint(1) NOT NULL DEFAULT 0 COMMENT ''商家是否隐藏会话(0=否 1=是)'' AFTER buyer_hidden',
              'SELECT 1')
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'cm_im_session'
      AND COLUMN_NAME = 'merchant_hidden'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
