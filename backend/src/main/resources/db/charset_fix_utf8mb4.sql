-- ============================================================
-- 题库表字符集修复与乱码处理完整脚本
-- 执行环境: MySQL 8.0+
-- ============================================================

-- ============================================================
-- 1. 数据库、表、字段字符集统一设置为 utf8mb4
-- ============================================================

USE mini_wx_db;

-- 1.1 修改数据库字符集
ALTER DATABASE mini_wx_db
CHARACTER SET = utf8mb4
COLLATE = utf8mb4_unicode_ci;

-- 1.2 修改表字符集
ALTER TABLE exam_question
CONVERT TO CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

-- 1.3 逐个字段重新设置字符集并修复中文注释
ALTER TABLE exam_question MODIFY COLUMN
    subject_id BIGINT NOT NULL COMMENT '科目ID：1=高等教育学 2=高等教育法规和政策 3=教师伦理学 4=大学心理学';

ALTER TABLE exam_question MODIFY COLUMN
    type TINYINT NOT NULL COMMENT '题型：1=单选题，2=多选题，3=判断题';

ALTER TABLE exam_question MODIFY COLUMN
    stem TEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '题干内容（支持HTML格式）';

ALTER TABLE exam_question MODIFY COLUMN
    option_list JSON NULL COMMENT '题目选项数组，存储格式：[{"key":"A","value":"选项内容"},{"key":"E","value":"选项内容"}]，兼容A~G多选项；判断题此字段为NULL';

ALTER TABLE exam_question MODIFY COLUMN
    answer VARCHAR(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '正确答案：单选存单个字母如"A"；多选存逗号分隔如"A,C,E,G"；判断存"true"或"false"';

ALTER TABLE exam_question MODIFY COLUMN
    explanation TEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '答案解析（支持HTML格式）';

ALTER TABLE exam_question MODIFY COLUMN
    status TINYINT DEFAULT 1 COMMENT '状态：0=禁用，1=正常（抽卷、统计时仅过滤status=0的数据）';

ALTER TABLE exam_question MODIFY COLUMN
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间';

ALTER TABLE exam_question MODIFY COLUMN
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间';

-- 1.4 验证字符集设置结果
SELECT '=== 数据库字符集 ===' AS info;
SELECT default_character_set_name, default_collation_name
FROM information_schema.SCHEMATA WHERE schema_name = 'mini_wx_db';

SELECT '=== 表字符集 ===' AS info;
SELECT table_name, table_collation
FROM information_schema.TABLES WHERE table_schema = 'mini_wx_db' AND table_name = 'exam_question';

SELECT '=== 字段字符集 & 注释 ===' AS info;
SELECT column_name, character_set_name, collation_name, column_comment
FROM information_schema.COLUMNS WHERE table_schema = 'mini_wx_db' AND table_name = 'exam_question'
ORDER BY ordinal_position;

-- ============================================================
-- 2. stem字段乱码根源分析与数据库连接URL配置
-- ============================================================

-- 乱码根源说明：
-- 【问题】 stem 字段出现中文乱码根本原因：
--     Java 应用程序连接 MySQL 时，JDBC URL 未正确配置字符编码参数，
--     导致 Java 端 UTF-8 编码的中文在传输到 MySQL 时被转换为其他编码。
--
-- 【典型场景】：
--     ① 旧版 MySQL JDBC 驱动（5.x）未配置 useUnicode=true&characterEncoding=UTF-8
--     ② 新版 MySQL JDBC 驱动（8.x）与 MySQL 服务器编码协商不一致
--     ③ Excel 导入工具读取文件时编码错误（如 GBK → UTF-8 转换失败）
--     ④ 命令行导入时终端编码与 SQL 文件编码不一致
--
-- 【无法还原原因】：
--     UTF-8 多字节字符被错误解析后产生的 "?","??","å­¦ç" 等字符，
--     是信息丢失后的结果，MySQL 层面无法逆向还原原始中文。

-- ============================================================
-- 【完整数据库连接URL配置】
-- ============================================================
--
-- MySQL 8.x + Spring Boot 标准配置：
-- spring.datasource.url=jdbc:mysql://47.97.193.230:3306/mini_wx_db?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true
--
-- 参数说明：
--   useUnicode=true              → 启用Unicode传输
--   characterEncoding=utf8       → 传输字符集为 UTF-8（必须，即使MySQL 8.x也需要）
--   serverTimezone=Asia/Shanghai → 时区设置为中国标准时间（避免时间字段乱码）
--   useSSL=false                 → 非生产环境可关闭SSL（避免证书问题）
--   allowPublicKeyRetrieval=true → 允许客户端从服务器获取公钥
--
-- ============================================================
-- 【附加配置】application.yml：
-- spring:
--   datasource:
--     url: jdbc:mysql://47.97.193.230:3306/mini_wx_db?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true
--     driver-class-name: com.mysql.cj.jdbc.Driver
--     hikari:
--       connection-init-sql: SET NAMES utf8mb4
--
-- ============================================================

-- ============================================================
-- 3. 字段存储规范重申
-- ============================================================

-- subject_id 科目编码（必须使用枚举校验）：
--   1 = 高等教育学
--   2 = 高等教育法规和政策
--   3 = 教师伦理学
--   4 = 大学心理学
--
-- type 题型编码：
--   1 = 单选题
--   2 = 多选题
--   3 = 判断题
--
-- stem 题干：
--   类型: TEXT
--   编码: utf8mb4
--   要求: 纯中文或带HTML标签，禁止乱码字符
--   示例: '高等教育的本质属性是？'
--
-- option_list 选项：
--   类型: JSON
--   要求:
--     √ 单选题/多选题: 数组格式 [{"key":"A","value":"选项内容"},{"key":"B","value":"..."}]
--     √ 判断题: NULL（选项由前端生成：正确/错误）
--   支持键值: A ~ Z 任意字母
--   禁止: 固定ABCD字段、选项在answer字段存储
--
-- answer 正确答案：
--   类型: VARCHAR(50)
--   √ 单选题: 单个大写字母，如 'D'
--   √ 多选题: 多个字母英文逗号分隔，按字母顺序如 'A,C,E'
--   √ 判断题: 固定小写字符串 'true' 或 'false'
--   禁止: '对'/'错'/'T'/'F'/'1'/'0'/中文逗号等
--
-- explanation 解析：
--   类型: TEXT
--   编码: utf8mb4
--   要求: 详细解析，支持HTML标签，可为NULL
--
-- status 状态：
--   1 = 正常有效题目（抽卷、统计时包含）
--   0 = 已禁用（所有查询过滤掉）
--   默认值: 1

-- ============================================================
-- 4. 清空乱码数据SQL语句
-- ============================================================
-- 【重要说明】：
--   已产生乱码的数据无法通过MySQL层面命令还原（信息已丢失），
--   必须清空后使用 UTF-8 编码的原始题库文件重新导入。

-- 4.1 清空全表数据（推荐，自增ID重置）
TRUNCATE TABLE exam_question;

-- 4.2 或 选择性删除包含乱码特征的行（可选，不推荐，可能漏删）
-- DELETE FROM exam_question WHERE
--     stem LIKE '%?%'
--     OR stem LIKE '%Ã%'
--     OR stem LIKE '%Â%'
--     OR stem LIKE '%å%'
--     OR stem LIKE '%ç%'
--     OR HEX(stem) REGEXP '^(..)*(C3|C2|E5|E6|E7)';

-- 4.3 验证清空结果
SELECT '=== 清空后数据统计 ===' AS info;
SELECT COUNT(*) AS total_rows FROM exam_question;

-- ============================================================
-- 【重新导入数据注意事项】：
-- 1. Excel/CSV 源文件必须以 UTF-8 编码保存（不要用 GBK/ANSI）
-- 2. Java 导入程序读取文件时指定编码: Files.newBufferedReader(path, StandardCharsets.UTF_8)
-- 3. JDBC URL 必须包含 useUnicode=true&characterEncoding=utf8 参数
-- 4. 导入前执行 SET NAMES utf8mb4; 确保连接会话编码正确
-- 5. 导入后抽样验证: SELECT stem FROM exam_question LIMIT 10;
-- ============================================================

SELECT '字符集修复脚本执行完成！' AS status;
