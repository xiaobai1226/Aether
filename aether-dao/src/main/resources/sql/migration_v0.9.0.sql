-- ================================================================
-- Aether v0.9.0 架构重构数据库变更
-- 
-- 变更内容：
-- 1. 添加 migration_pending 字段支持异步存储源迁移
-- 2. 添加索引优化查询性能
--
-- 执行方式：
-- mysql -u root -p aether < migration_v0.9.0.sql
-- ================================================================

USE aether;

-- 1. 添加迁移标记字段
ALTER TABLE user_file 
ADD COLUMN migration_pending TINYINT DEFAULT 0 
COMMENT '待迁移标记 0=否 1=是'
AFTER storage_source_type;

-- 2. 添加索引提升查询性能
CREATE INDEX idx_migration_pending ON user_file(migration_pending);

-- 3. 显示变更结果
SELECT 
    'migration_pending字段已添加' AS status,
    COUNT(*) AS total_records,
    SUM(CASE WHEN migration_pending = 1 THEN 1 ELSE 0 END) AS pending_count
FROM user_file;

-- 说明：
-- - migration_pending=1 表示该文件需要迁移到新的存储源
-- - 定时任务 StorageMigrationScheduler 会每分钟扫描并处理这些文件
-- - 迁移过程用户无感知，文件可以正常访问