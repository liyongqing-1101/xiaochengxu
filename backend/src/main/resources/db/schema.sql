-- ============================================================
-- 高校教资刷题系统 完整数据库建表脚本
-- Database: exam_db
-- ============================================================

CREATE DATABASE IF NOT EXISTS exam_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE exam_db;

-- ============================================================
-- 1. 考试大类
-- [EXTENSION-POINT] 新增考试分类只需 INSERT 即可, 无需改表
-- ============================================================
CREATE TABLE IF NOT EXISTS exam_category (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    name        VARCHAR(50)  NOT NULL COMMENT '分类名称',
    icon        VARCHAR(100) COMMENT '图标',
    description VARCHAR(200) COMMENT '描述',
    sort_order  INT DEFAULT 0 COMMENT '排序',
    status      TINYINT DEFAULT 1 COMMENT '状态 0禁用 1启用',
    created_at  DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='考试大类';

-- ============================================================
-- 2. 科目
-- ============================================================
CREATE TABLE IF NOT EXISTS exam_subject (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    category_id     BIGINT NOT NULL COMMENT '所属大类',
    name            VARCHAR(100) NOT NULL COMMENT '科目名称',
    icon            VARCHAR(100) COMMENT '图标',
    total_questions INT DEFAULT 0 COMMENT '题目总数(冗余)',
    sort_order      INT DEFAULT 0,
    status          TINYINT DEFAULT 1,
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_category (category_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='科目';

-- ============================================================
-- 3. 章节
-- ============================================================
CREATE TABLE IF NOT EXISTS exam_chapter (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    subject_id      BIGINT NOT NULL COMMENT '所属科目',
    name            VARCHAR(200) NOT NULL COMMENT '章节名称',
    question_count  INT DEFAULT 0,
    sort_order      INT DEFAULT 0,
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_subject (subject_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='章节';

-- ============================================================
-- 4. 知识点标签
-- ============================================================
CREATE TABLE IF NOT EXISTS exam_tag (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    chapter_id      BIGINT NOT NULL COMMENT '所属章节',
    name            VARCHAR(200) NOT NULL COMMENT '知识点名称',
    question_count  INT DEFAULT 0,
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_chapter (chapter_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='知识点标签';

-- ============================================================
-- 5. 题目 (核心表)
-- ============================================================
CREATE TABLE IF NOT EXISTS exam_question (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    category_id     BIGINT NOT NULL COMMENT '考试大类ID',
    subject_id      BIGINT NOT NULL COMMENT '科目ID',
    chapter_id      BIGINT COMMENT '章节ID',
    tag_id          BIGINT COMMENT '知识点标签ID',
    type            TINYINT NOT NULL COMMENT '题型: 1单选 2多选 3判断',
    stem            TEXT NOT NULL COMMENT '题干(HTML)',
    option_a        VARCHAR(500) COMMENT '选项A',
    option_b        VARCHAR(500) COMMENT '选项B',
    option_c        VARCHAR(500) COMMENT '选项C',
    option_d        VARCHAR(500) COMMENT '选项D',
    answer          VARCHAR(50) NOT NULL COMMENT '正确答案 JSON数组: ["A"] 或 ["A","C"] 或 ["T"]/["F"]',
    explanation     TEXT COMMENT '解析(HTML)',
    difficulty      TINYINT DEFAULT 2 COMMENT '难度: 1简单 2中等 3困难',
    status          TINYINT DEFAULT 1 COMMENT '状态: 0下架 1上架',
    stem_images     VARCHAR(1000) COMMENT '题干图片URL JSON数组',
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_category_type (category_id, type),
    INDEX idx_subject (subject_id),
    INDEX idx_chapter (chapter_id),
    INDEX idx_tag (tag_id),
    INDEX idx_status (status),
    FULLTEXT INDEX ft_stem (stem)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='题目';

-- ============================================================
-- 6. 用户（支持微信登录 + 用户名密码登录）
-- ============================================================
CREATE TABLE IF NOT EXISTS user (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    openid      VARCHAR(64) NULL UNIQUE COMMENT '微信OpenID',
    username    VARCHAR(50) NULL UNIQUE COMMENT '登录用户名',
    password    VARCHAR(200) NULL COMMENT 'BCrypt加密密码',
    nickname    VARCHAR(100) COMMENT '用户昵称',
    avatar      VARCHAR(500) COMMENT '头像URL',
    gender      TINYINT DEFAULT 0 COMMENT '性别: 0未知 1男 2女',
    phone       VARCHAR(20) COMMENT '手机号',
    membership  VARCHAR(20) DEFAULT 'free' COMMENT '会员: free/vip',
    status      TINYINT DEFAULT 1 COMMENT '状态: 0禁用 1正常',
    created_at  DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户';

-- ============================================================
-- 7. 答题记录
-- ============================================================
CREATE TABLE IF NOT EXISTS user_answer_record (
    id               BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id          BIGINT NOT NULL COMMENT '用户ID',
    question_id      BIGINT NOT NULL COMMENT '题目ID',
    session_id       VARCHAR(64) NOT NULL COMMENT '答题会话ID',
    selected_options VARCHAR(50) NOT NULL COMMENT '选择的答案 JSON数组',
    is_correct       TINYINT NOT NULL COMMENT '0错误 1正确',
    duration         INT DEFAULT 0 COMMENT '答题耗时(秒)',
    category_id      BIGINT COMMENT '分类ID',
    subject_id       BIGINT COMMENT '科目ID',
    created_at       DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user (user_id),
    INDEX idx_session (session_id),
    INDEX idx_user_created (user_id, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='答题记录';

-- ============================================================
-- 8. 错题本
-- ============================================================
CREATE TABLE IF NOT EXISTS user_wrong_question (
    id                BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id           BIGINT NOT NULL COMMENT '用户ID',
    question_id       BIGINT NOT NULL COMMENT '题目ID',
    error_count       INT DEFAULT 1 COMMENT '错误次数',
    last_wrong_answer VARCHAR(50) COMMENT '最后一次错误答案',
    source            TINYINT DEFAULT 1 COMMENT '来源: 1练习 2考试 3每日一题',
    created_at        DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at        DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_question (user_id, question_id),
    INDEX idx_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='错题本';

-- ============================================================
-- 9. 打卡记录
-- ============================================================
CREATE TABLE IF NOT EXISTS user_check_in (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id     BIGINT NOT NULL COMMENT '用户ID',
    check_date  DATE NOT NULL COMMENT '打卡日期',
    created_at  DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_date (user_id, check_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='打卡记录';

-- ============================================================
-- 10. 收藏
-- ============================================================
CREATE TABLE IF NOT EXISTS user_collection (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id     BIGINT NOT NULL COMMENT '用户ID',
    question_id BIGINT NOT NULL COMMENT '题目ID',
    created_at  DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_question (user_id, question_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收藏';

-- ============================================================
-- 11. 用户反馈
-- ============================================================
CREATE TABLE IF NOT EXISTS user_feedback (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id     BIGINT NOT NULL COMMENT '用户ID',
    content     TEXT NOT NULL COMMENT '反馈内容',
    contact     VARCHAR(100) COMMENT '联系方式',
    status      TINYINT DEFAULT 0 COMMENT '0未处理 1已处理',
    created_at  DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户反馈';

-- ============================================================
-- 12. 管理员
-- ============================================================
CREATE TABLE IF NOT EXISTS admin_user (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    username    VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password    VARCHAR(200) NOT NULL COMMENT 'BCrypt加密密码',
    nickname    VARCHAR(50) COMMENT '昵称',
    role        VARCHAR(20) DEFAULT 'ADMIN' COMMENT '角色: ADMIN/SUPER_ADMIN',
    status      TINYINT DEFAULT 1 COMMENT '状态: 0禁用 1正常',
    created_at  DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='管理员';

-- ============================================================
-- 13. 导入任务
-- ============================================================
CREATE TABLE IF NOT EXISTS import_task (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    admin_id        BIGINT COMMENT '操作管理员ID',
    file_name       VARCHAR(200) NOT NULL COMMENT '原始文件名',
    file_size       BIGINT COMMENT '文件大小(字节)',
    category_id     BIGINT NOT NULL COMMENT '导入目标分类ID',
    total_rows      INT DEFAULT 0 COMMENT 'Excel总行数',
    success_count   INT DEFAULT 0 COMMENT '成功导入数',
    fail_count      INT DEFAULT 0 COMMENT '失败数',
    status          VARCHAR(20) DEFAULT 'PENDING' COMMENT 'PENDING/PROCESSING/COMPLETED/FAILED',
    error_file_url  VARCHAR(500) COMMENT '错误明细Excel OSS地址',
    error_detail    TEXT COMMENT '错误详情JSON',
    started_at      DATETIME COMMENT '开始时间',
    completed_at    DATETIME COMMENT '完成时间',
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='导入任务';

-- ============================================================
-- 种子数据
-- ============================================================

-- 初始化考试大类
INSERT INTO exam_category (id, name, icon, description, sort_order, status) VALUES
(1, '高校教资', 'certificate', '高校教师资格证考试题库', 1, 1);

-- 初始化示例科目
INSERT INTO exam_subject (id, category_id, name, icon, sort_order, status) VALUES
(1, 1, '综合素质', 'book', 1, 1),
(2, 1, '教育知识与能力', 'edit', 2, 1);

-- 初始化管理员 (密码: admin123, BCrypt加密)
INSERT INTO admin_user (id, username, password, nickname, role, status) VALUES
(1, 'admin', '$2b$10$1aie/GPaamRez1x//vBqZObE/6HHsQjQ0zOjpAA8Fg7kkhp4JuYS.', '超级管理员', 'SUPER_ADMIN', 1);
