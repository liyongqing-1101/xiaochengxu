-- ============================================================
-- 完整修复脚本
-- 一键修复科目数据和题目关联
-- ============================================================

-- 1. 先清空旧科目数据（谨慎操作！如果有重要数据先备份）
DELETE FROM exam_subject WHERE category_id = 1;

-- 2. 插入正确的4个科目（与前端FIXED_SUBJECTS完全一致）
INSERT INTO exam_subject (id, category_id, name, icon, sort_order, status) VALUES
(1, 1, '高等教育学', '📖', 1, 1),
(2, 1, '高等教育法规和政策', '⚖️', 2, 1),
(3, 1, '教师伦理学', '🎓', 3, 1),
(4, 1, '大学心理学', '🧠', 4, 1);

-- 3. 如果您exam_question表里已经有题目，确保它们的status=1
-- （假设原来的题目是有效的，全部启用）
UPDATE exam_question SET status = 1 WHERE status IS NULL OR status != 0;

-- 4. 验证修复结果
SELECT '修复后的科目列表' AS '信息';
SELECT id, name,
       (SELECT COUNT(*) FROM exam_question q WHERE q.subject_id = s.id AND q.status = 1) AS 题目总数
FROM exam_subject s WHERE s.category_id = 1 ORDER BY id;

SELECT '当前题目分布' AS '信息';
SELECT subject_id, COUNT(*) AS 题目数
FROM exam_question
WHERE status = 1
GROUP BY subject_id
ORDER BY subject_id;
