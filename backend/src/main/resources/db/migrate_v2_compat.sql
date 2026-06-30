-- ============================================================
-- exam_question 题库表重构脚本 v2.0 (MySQL 8.0 兼容版)
-- ============================================================

USE mini_wx_db;

-- ============================================================
-- 1. 创建新表（v2.0 结构）
-- ============================================================
CREATE TABLE IF NOT EXISTS exam_question_new (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    subject_id      BIGINT NOT NULL COMMENT '科目ID：1=高等教育学 2=高等教育法规和政策 3=教师伦理学 4=大学心理学',
    type            TINYINT NOT NULL COMMENT '题型：1=单选题，2=多选题，3=判断题',
    stem            TEXT NOT NULL COMMENT '题干内容（支持HTML格式）',
    option_list     JSON NULL COMMENT '题目选项数组，存储格式：[{"key":"A","value":"选项内容"},{"key":"E","value":"选项内容"}]，兼容A~G多选项；判断题此字段为NULL',
    answer          VARCHAR(50) NOT NULL COMMENT '正确答案：单选存单个字母如"A"；多选存逗号分隔如"A,C,E,G"；判断存"true"或"false"',
    explanation     TEXT NULL COMMENT '答案解析（支持HTML格式）',
    status          TINYINT DEFAULT 1 COMMENT '状态：0=禁用，1=正常（抽卷、统计时仅过滤status=0的数据）',
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at      DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_subject_type (subject_id, type),
    FULLTEXT INDEX ft_stem (stem)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='题目表（v2.0 重构版）';

-- ============================================================
-- 2. 迁移数据（如果有）
-- ============================================================
-- 空表跳过数据迁移

-- ============================================================
-- 3. 替换旧表
-- ============================================================
DROP TABLE IF EXISTS exam_question;
RENAME TABLE exam_question_new TO exam_question;

SELECT 'exam_question 表重构完成!' AS status;
