-- 阿斯塔洛斯Wiki数据库初始化脚本
-- 适用于IntelliJ IDEA数据库工具直接执行

-- 创建数据库
CREATE DATABASE IF NOT EXISTS astaros_wiki DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE astaros_wiki;

-- 1. 用户表
DROP TABLE IF EXISTS users;
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '用户ID',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    email VARCHAR(100) NOT NULL UNIQUE COMMENT '邮箱',
    password_hash VARCHAR(255) NOT NULL COMMENT '密码哈希',
    role VARCHAR(20) NOT NULL DEFAULT 'CREATOR' COMMENT '角色: ADMIN/CREATOR',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '状态: PENDING/ACTIVE/DISABLED',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    extra_json JSON COMMENT '扩展字段',
    INDEX idx_username (username),
    INDEX idx_email (email),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 2. 分类表
DROP TABLE IF EXISTS categories;
CREATE TABLE categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '分类ID',
    name VARCHAR(100) NOT NULL COMMENT '分类名称',
    description TEXT COMMENT '分类描述',
    parent_id BIGINT DEFAULT NULL COMMENT '父分类ID',
    sort_order INT DEFAULT 0 COMMENT '排序',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    extra_json JSON COMMENT '扩展字段',
    INDEX idx_parent_id (parent_id),
    INDEX idx_sort_order (sort_order),
    CONSTRAINT fk_category_parent FOREIGN KEY (parent_id) REFERENCES categories(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='分类表';

-- 3. 标签表
DROP TABLE IF EXISTS tags;
CREATE TABLE tags (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '标签ID',
    name VARCHAR(50) NOT NULL UNIQUE COMMENT '标签名称',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='标签表';

-- 4. 页面表
DROP TABLE IF EXISTS pages;
CREATE TABLE pages (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '页面ID',
    title VARCHAR(200) NOT NULL COMMENT '标题',
    content LONGTEXT COMMENT '内容',
    slug VARCHAR(200) NOT NULL UNIQUE COMMENT 'URL别名',
    category_id BIGINT COMMENT '分类ID',
    author_id BIGINT NOT NULL COMMENT '作者ID',
    status VARCHAR(20) NOT NULL DEFAULT 'PUBLISHED' COMMENT '状态: PUBLISHED/DELETED/LOCKED',
    version INT DEFAULT 1 COMMENT '版本号',
    view_count INT DEFAULT 0 COMMENT '浏览次数',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    extra_json JSON COMMENT '扩展字段',
    INDEX idx_slug (slug),
    INDEX idx_category_id (category_id),
    INDEX idx_author_id (author_id),
    INDEX idx_status (status),
    FULLTEXT INDEX ft_title_content (title, content),
    CONSTRAINT fk_page_category FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE SET NULL,
    CONSTRAINT fk_page_author FOREIGN KEY (author_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='页面表';

-- 5. 页面版本表
DROP TABLE IF EXISTS page_versions;
CREATE TABLE page_versions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '版本ID',
    page_id BIGINT NOT NULL COMMENT '页面ID',
    content LONGTEXT COMMENT '内容快照',
    editor_id BIGINT NOT NULL COMMENT '编辑者ID',
    version_number INT NOT NULL COMMENT '版本号',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_page_id (page_id),
    INDEX idx_version_number (version_number),
    CONSTRAINT fk_version_page FOREIGN KEY (page_id) REFERENCES pages(id) ON DELETE CASCADE,
    CONSTRAINT fk_version_editor FOREIGN KEY (editor_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='页面版本表';

-- 6. 页面标签关联表
DROP TABLE IF EXISTS page_tags;
CREATE TABLE page_tags (
    page_id BIGINT NOT NULL COMMENT '页面ID',
    tag_id BIGINT NOT NULL COMMENT '标签ID',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (page_id, tag_id),
    INDEX idx_tag_id (tag_id),
    CONSTRAINT fk_page_tag_page FOREIGN KEY (page_id) REFERENCES pages(id) ON DELETE CASCADE,
    CONSTRAINT fk_page_tag_tag FOREIGN KEY (tag_id) REFERENCES tags(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='页面标签关联表';

-- 7. 用户申请表
DROP TABLE IF EXISTS user_requests;
CREATE TABLE user_requests (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '申请ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    request_type VARCHAR(50) NOT NULL DEFAULT 'CREATOR_REGISTRATION' COMMENT '申请类型',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '状态: PENDING/APPROVED/REJECTED',
    reviewer_id BIGINT COMMENT '审核人ID',
    review_comment TEXT COMMENT '审核意见',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_user_id (user_id),
    INDEX idx_status (status),
    CONSTRAINT fk_request_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_request_reviewer FOREIGN KEY (reviewer_id) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户申请表';

-- 8. 操作日志表
DROP TABLE IF EXISTS operation_logs;
CREATE TABLE operation_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '日志ID',
    user_id BIGINT COMMENT '用户ID',
    action VARCHAR(50) NOT NULL COMMENT '操作类型',
    target_type VARCHAR(50) COMMENT '目标类型',
    target_id BIGINT COMMENT '目标ID',
    detail TEXT COMMENT '详细信息',
    ip_address VARCHAR(50) COMMENT 'IP地址',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_user_id (user_id),
    INDEX idx_action (action),
    INDEX idx_target (target_type, target_id),
    INDEX idx_created_at (created_at),
    CONSTRAINT fk_log_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='操作日志表';

-- 插入默认管理员账户（用户名：admin，密码：admin123）
-- 正确的 BCrypt 哈希格式（共60字符）
INSERT INTO users (username, email, password_hash, role, status)
VALUES ('admin', 'admin@astaros.wiki', '$2a$10$slYQmyNdGzTn7ZLBXBChFOC9f6kFjAqPhccnP6DxlWXx2lPk1C3G6', 'ADMIN', 'ACTIVE');

-- 插入默认分类
INSERT INTO categories (name, description, parent_id, sort_order) VALUES
('种族', '阿斯塔洛斯世界的种族设定', NULL, 1),
('地理', '阿斯塔洛斯世界的地理设定', NULL, 2),
('人物', '阿斯塔洛斯世界的人物设定', NULL, 3),
('力量体系', '阿斯塔洛斯世界的力量体系设定', NULL, 4),
('历史事件', '阿斯塔洛斯世界的历史事件', NULL, 5),
('物品', '阿斯塔洛斯世界的物品设定', NULL, 6);

-- 插入示例标签
INSERT INTO tags (name) VALUES
('夜族'),
('仙灵'),
('龙族'),
('恶魔'),
('人类'),
('神圣'),
('深渊'),
('黑暗'),
('魔力'),
('起源传说');

-- 插入示例页面
INSERT INTO pages (title, content, slug, category_id, author_id, status, version, view_count) VALUES
('夜族', '# 夜族\n\n夜族是阿斯塔洛斯世界中最神秘的种族之一，他们以夜晚为生，拥有独特的能力和命运。\n\n## 基本信息\n- **起源**: 创世之夜\n- **特征**: 拥有暗影力量，夜间活动\n- **主要居住地**: 暗影森林', 'night-race', 1, 1, 'PUBLISHED', 1, 0),
('神圣本源', '# 神圣本源\n\n神圣本源是四大本源力量之一，代表着光明、正义和治愈的力量。\n\n## 特性\n- **颜色**: 金色光芒\n- **象征**: 太阳、净化、治愈\n- **主要使用者**: 仙灵族、部分人类', 'sacred-origin', 4, 1, 'PUBLISHED', 1, 0),
('阿斯塔洛斯大陆', '# 阿斯塔洛斯大陆\n\n阿斯塔洛斯大陆是一个充满奇幻色彩的世界大陆，孕育着各种神奇的生物和文明。\n\n## 地理特征\n- **总面积**: 约500万平方公里\n- **主要地形**: 平原、山脉、森林、海洋\n- **气候**: 多样化，从热带到寒带', 'astaros-continent', 2, 1, 'PUBLISHED', 1, 0);

-- 为示例页面添加标签
INSERT INTO page_tags (page_id, tag_id) VALUES
(1, 1),
(2, 6),
(3, 7);

-- 显示初始化完成信息
SELECT '数据库初始化完成！' AS message;
SELECT COUNT(*) AS user_count FROM users;
SELECT COUNT(*) AS category_count FROM categories;
SELECT COUNT(*) AS tag_count FROM tags;
SELECT COUNT(*) AS page_count FROM pages;