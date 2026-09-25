-- ============================================
-- 管理员照片审核功能数据库迁移脚本
-- 执行前请备份现有数据
-- ============================================

-- 1. 为 photo 表添加审核相关字段
ALTER TABLE photo ADD COLUMN status VARCHAR(20) DEFAULT 'pending' COMMENT '审核状态：pending(待审核)、approved(已通过)、rejected(已拒绝)';
ALTER TABLE photo ADD COLUMN reviewed_by VARCHAR(36) COMMENT '审核人ID（管理员ID）';
ALTER TABLE photo ADD COLUMN reviewed_at DATETIME COMMENT '审核时间';
ALTER TABLE photo ADD COLUMN reject_reason VARCHAR(500) COMMENT '拒绝原因（可选）';

-- 2. 将现有照片设为已通过（兼容旧数据）
UPDATE photo SET status = 'approved' WHERE status IS NULL;

-- 3. 添加索引以优化查询
ALTER TABLE photo ADD INDEX idx_status (status);
ALTER TABLE photo ADD INDEX idx_reviewed_by (reviewed_by);

-- ============================================
-- 迁移完成
-- ============================================
