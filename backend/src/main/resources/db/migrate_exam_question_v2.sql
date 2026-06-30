-- ============================================================
-- exam_question 题库表重构脚本 v2.0
-- 执行环境: MySQL 8.0+
-- 执行前请备份数据!
-- ============================================================

USE mini_wx_db;

-- ============================================================
-- 1. 删除冗余无用字段
-- ============================================================
ALTER TABLE exam_question
    DROP COLUMN IF EXISTS category_id,
    DROP COLUMN IF EXISTS chapter_id,
    DROP COLUMN IF EXISTS tag_id,
    DROP COLUMN IF EXISTS difficulty,
    DROP COLUMN IF EXISTS stem_images,
    DROP COLUMN IF EXISTS option_a,
    DROP COLUMN IF EXISTS option_b,
    DROP COLUMN IF EXISTS option_c,
    DROP COLUMN IF EXISTS option_d;

-- ============================================================
-- 2. 新增动态选项 JSON 字段
-- ============================================================
ALTER TABLE exam_question
    ADD COLUMN option_list JSON NULL COMMENT '题目选项数组，存储格式：[{"key":"A","value":"选项内容"},{"key":"E","value":"选项内容"}]，兼容A~G多选项；判断题此字段为NULL'
    AFTER stem;

-- ============================================================
-- 3. 修正 updated_at 字段（确保自动更新）
-- ============================================================
ALTER TABLE exam_question
    MODIFY COLUMN updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间';

-- ============================================================
-- 4. 重写 answer 字段注释（明确存储规则）
-- ============================================================
ALTER TABLE exam_question
    MODIFY COLUMN answer VARCHAR(50) NOT NULL COMMENT '正确答案：单选存单个字母如"A"；多选存逗号分隔如"A,C,E,G"；判断存"true"或"false"';

-- ============================================================
-- 5. 重写 type 字段注释
-- ============================================================
ALTER TABLE exam_question
    MODIFY COLUMN type TINYINT NOT NULL COMMENT '题型：1=单选题，2=多选题，3=判断题';

-- ============================================================
-- 6. 重写 subject_id 字段注释
-- ============================================================
ALTER TABLE exam_question
    MODIFY COLUMN subject_id BIGINT NOT NULL COMMENT '科目ID：1=高等教育学 2=高等教育法规和政策 3=教师伦理学 4=大学心理学';

-- ============================================================
-- 7. 重写 status 字段注释
-- ============================================================
ALTER TABLE exam_question
    MODIFY COLUMN status TINYINT DEFAULT 1 COMMENT '状态：0=禁用，1=正常（抽卷、统计时仅过滤status=0的数据）';

-- ============================================================
-- 8. 重写 stem 字段注释
-- ============================================================
ALTER TABLE exam_question
    MODIFY COLUMN stem TEXT NOT NULL COMMENT '题干内容（支持HTML格式）';

-- ============================================================
-- 9. 重写 explanation 字段注释
-- ============================================================
ALTER TABLE exam_question
    MODIFY COLUMN explanation TEXT NULL COMMENT '答案解析（支持HTML格式）';

-- ============================================================
-- 10. 删除旧索引
-- ============================================================
ALTER TABLE exam_question
    DROP INDEX IF EXISTS idx_category_type,
    DROP INDEX IF EXISTS idx_chapter,
    DROP INDEX IF EXISTS idx_tag;

-- ============================================================
-- 11. 新增业务查询联合索引（用于随机按科目+题型抽题组卷）
-- ============================================================
ALTER TABLE exam_question
    ADD INDEX idx_subject_type (subject_id, type);

-- ============================================================
-- 12. 确保表级别的编码为 utf8mb4
-- ============================================================
ALTER TABLE exam_question CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- ============================================================
-- 13. 更新表注释
-- ============================================================
ALTER TABLE exam_question COMMENT '题目表（v2.0 重构版）';

-- ============================================================
-- 执行完成！
-- ============================================================
SELECT 'exam_question 表重构完成!' AS status;
