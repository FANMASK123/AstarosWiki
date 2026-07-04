-- ============================================================
-- 阿斯塔洛斯Wiki数据库初始化脚本
-- 统一版本 v1.0 - 兼容所有环境
-- 创建时间: 2026-06-30
-- 作者: AstarosWiki开发团队
-- ============================================================

-- ============================================================
-- 第一阶段：创建数据库并设置基础配置
-- ============================================================

-- 创建数据库（如果不存在），指定字符集为utf8mb4
-- utf8mb4支持完整的Unicode字符，包括emoji表情和特殊字符
CREATE DATABASE IF NOT EXISTS astaros_wiki 
DEFAULT CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

-- 使用创建的数据库
USE astaros_wiki;

-- 设置会话字符集，确保后续操作使用正确的字符编码
SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;
SET COLLATION_CONNECTION = 'utf8mb4_unicode_ci';

-- 禁用外键约束检查，避免创建表和插入数据时的外键依赖问题
-- 在所有表创建完成后会重新启用
SET FOREIGN_KEY_CHECKS = 0;

-- ============================================================
-- 第二阶段：创建数据表
-- 按照依赖顺序创建：先创建无外键依赖的表，再创建有外键依赖的表
-- ============================================================

-- ------------------------------------------------------------
-- 1. 用户表 (users)
-- 存储系统用户信息，包括管理员和创作者
-- ------------------------------------------------------------
DROP TABLE IF EXISTS users;
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '用户ID，自增主键',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名，唯一标识，最大50字符',
    email VARCHAR(100) NOT NULL UNIQUE COMMENT '邮箱地址，唯一，最大100字符',
    password_hash VARCHAR(255) NOT NULL COMMENT '密码哈希值，BCrypt加密，最大255字符',
    role VARCHAR(20) NOT NULL DEFAULT 'CREATOR' COMMENT '角色: ADMIN(管理员)/CREATOR(创作者)',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '状态: PENDING(待审核)/ACTIVE(活跃)/DISABLED(禁用)',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间，自动设置为当前时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间，自动更新',
    extra_json JSON COMMENT '扩展字段，存储额外的用户信息，JSON格式',
    -- 索引：加速查询
    INDEX idx_username (username),
    INDEX idx_email (email),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- ------------------------------------------------------------
-- 2. 分类表 (categories)
-- 存储页面分类信息，支持多级分类（通过parent_id实现）
-- ------------------------------------------------------------
DROP TABLE IF EXISTS categories;
CREATE TABLE categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '分类ID，自增主键',
    name VARCHAR(100) NOT NULL COMMENT '分类名称，最大100字符',
    description TEXT COMMENT '分类描述，支持长文本',
    parent_id BIGINT DEFAULT NULL COMMENT '父分类ID，NULL表示顶级分类',
    sort_order INT DEFAULT 0 COMMENT '排序序号，数字越小越靠前',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    extra_json JSON COMMENT '扩展字段，JSON格式',
    -- 索引：加速查询和外键关联
    INDEX idx_parent_id (parent_id),
    INDEX idx_sort_order (sort_order),
    -- 外键约束：父分类删除时，子分类的parent_id设置为NULL
    CONSTRAINT fk_category_parent FOREIGN KEY (parent_id) REFERENCES categories(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='分类表';

-- ------------------------------------------------------------
-- 3. 标签表 (tags)
-- 存储页面标签信息，用于内容分类和检索
-- ------------------------------------------------------------
DROP TABLE IF EXISTS tags;
CREATE TABLE tags (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '标签ID，自增主键',
    name VARCHAR(50) NOT NULL UNIQUE COMMENT '标签名称，唯一，最大50字符',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    -- 索引：加速标签名称查询
    INDEX idx_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='标签表';

-- ------------------------------------------------------------
-- 4. 页面表 (pages)
-- 存储Wiki页面的核心数据
-- ------------------------------------------------------------
DROP TABLE IF EXISTS pages;
CREATE TABLE pages (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '页面ID，自增主键',
    title VARCHAR(200) NOT NULL COMMENT '页面标题，最大200字符',
    content LONGTEXT COMMENT '已发布内容，用户看到的正式版本',
    draft_content LONGTEXT COMMENT '草稿内容，作者正在编辑的版本，不影响已发布内容',
    slug VARCHAR(200) NOT NULL UNIQUE COMMENT 'URL别名，唯一标识，用于页面访问路径',
    category_id BIGINT COMMENT '分类ID，关联到categories表',
    author_id BIGINT NOT NULL COMMENT '作者ID，关联到users表',
    status VARCHAR(20) NOT NULL DEFAULT 'SAVED' COMMENT '状态: PUBLISHED(已发布)/DRAFT(预备审核)/SAVED(保存态)/DELETED(已删除)/LOCKED(已锁定)',
    draft_status VARCHAR(20) COMMENT '草稿状态: NULL(无草稿)/SAVED(已保存)/DRAFT(待审核)',
    version INT DEFAULT 1 COMMENT '版本号，每次更新递增',
    view_count INT DEFAULT 0 COMMENT '浏览次数，初始为0',
    reject_reason VARCHAR(500) COMMENT '驳回原因，最大500字符',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间，仅在审核通过时更新',
    draft_updated_at TIMESTAMP COMMENT '草稿更新时间，作者保存/提交时更新',
    last_viewed_at TIMESTAMP COMMENT '最后浏览时间',
    extra_json JSON COMMENT '扩展字段，JSON格式',
    -- 索引：加速查询和全文搜索
    INDEX idx_slug (slug),
    INDEX idx_category_id (category_id),
    INDEX idx_author_id (author_id),
    INDEX idx_status (status),
    FULLTEXT INDEX ft_title_content (title, content),
    FULLTEXT INDEX ft_draft_content (title, draft_content),
    -- 外键约束：分类删除时设置为NULL，作者删除时级联删除页面
    CONSTRAINT fk_page_category FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE SET NULL,
    CONSTRAINT fk_page_author FOREIGN KEY (author_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='页面表';

-- ------------------------------------------------------------
-- 5. 页面版本表 (page_versions)
-- 存储页面的历史版本，支持版本回滚功能
-- ------------------------------------------------------------
DROP TABLE IF EXISTS page_versions;
CREATE TABLE page_versions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '版本ID，自增主键',
    page_id BIGINT NOT NULL COMMENT '关联的页面ID',
    content LONGTEXT COMMENT '版本内容快照，保存当时的页面内容',
    editor_id BIGINT NOT NULL COMMENT '编辑者ID，关联到users表',
    version_number INT NOT NULL COMMENT '版本号，与pages表的version字段对应',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    -- 索引：加速版本查询
    INDEX idx_page_id (page_id),
    INDEX idx_version_number (version_number),
    -- 外键约束：页面或编辑者删除时级联删除版本记录
    CONSTRAINT fk_version_page FOREIGN KEY (page_id) REFERENCES pages(id) ON DELETE CASCADE,
    CONSTRAINT fk_version_editor FOREIGN KEY (editor_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='页面版本表';

-- ------------------------------------------------------------
-- 6. 页面标签关联表 (page_tags)
-- 存储页面和标签的多对多关联关系
-- ------------------------------------------------------------
DROP TABLE IF EXISTS page_tags;
CREATE TABLE page_tags (
    page_id BIGINT NOT NULL COMMENT '页面ID',
    tag_id BIGINT NOT NULL COMMENT '标签ID',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '关联创建时间',
    -- 复合主键：确保同一页面不会重复关联同一标签
    PRIMARY KEY (page_id, tag_id),
    -- 索引：加速按标签查询页面
    INDEX idx_tag_id (tag_id),
    -- 外键约束：页面或标签删除时级联删除关联记录
    CONSTRAINT fk_page_tag_page FOREIGN KEY (page_id) REFERENCES pages(id) ON DELETE CASCADE,
    CONSTRAINT fk_page_tag_tag FOREIGN KEY (tag_id) REFERENCES tags(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='页面标签关联表';

-- ------------------------------------------------------------
-- 7. 消息表 (messages)
-- 存储系统消息，用于通知用户审核结果等
-- ------------------------------------------------------------
DROP TABLE IF EXISTS messages;
CREATE TABLE messages (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '消息ID，自增主键',
    user_id BIGINT NOT NULL COMMENT '接收消息的用户ID',
    title VARCHAR(200) NOT NULL COMMENT '消息标题，最大200字符',
    content TEXT NOT NULL COMMENT '消息内容，支持长文本',
    opinion TEXT COMMENT '审核意见/驳回原因',
    is_read BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否已读，默认未读(FALSE)',
    page_id BIGINT COMMENT '关联的页面ID，NULL表示无关联',
    page_slug VARCHAR(200) COMMENT '关联页面的URL别名，用于消息跳转',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    -- 索引：加速查询
    INDEX idx_user_id (user_id),
    INDEX idx_is_read (is_read),
    INDEX idx_page_id (page_id),
    INDEX idx_created_at (created_at),
    -- 外键约束：用户删除时级联删除消息，页面删除时设置为NULL
    CONSTRAINT fk_message_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_message_page FOREIGN KEY (page_id) REFERENCES pages(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='消息表';

-- ------------------------------------------------------------
-- 8. 用户申请表 (user_requests)
-- 存储用户申请信息，如创作者注册申请
-- ------------------------------------------------------------
DROP TABLE IF EXISTS user_requests;
CREATE TABLE user_requests (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '申请ID，自增主键',
    user_id BIGINT NOT NULL COMMENT '申请者ID',
    request_type VARCHAR(50) NOT NULL DEFAULT 'CREATOR_REGISTRATION' COMMENT '申请类型: CREATOR_REGISTRATION(创作者注册)',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '状态: PENDING(待审核)/APPROVED(已通过)/REJECTED(已拒绝)',
    reviewer_id BIGINT COMMENT '审核人ID，NULL表示未审核',
    review_comment TEXT COMMENT '审核意见',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    -- 索引：加速查询
    INDEX idx_user_id (user_id),
    INDEX idx_status (status),
    -- 外键约束：用户删除时级联删除申请，审核人删除时设置为NULL
    CONSTRAINT fk_request_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_request_reviewer FOREIGN KEY (reviewer_id) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户申请表';

-- ------------------------------------------------------------
-- 9. 操作日志表 (operation_logs)
-- 存储管理员操作记录，用于审计和追溯
-- ------------------------------------------------------------
DROP TABLE IF EXISTS operation_logs;
CREATE TABLE operation_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '日志ID，自增主键',
    user_id BIGINT COMMENT '操作用户ID，NULL表示系统操作',
    action VARCHAR(50) NOT NULL COMMENT '操作类型: CREATE/UPDATE/DELETE/APPROVE/REJECT等',
    target_type VARCHAR(50) COMMENT '操作目标类型: PAGE/CATEGORY/USER等',
    target_id BIGINT COMMENT '操作目标ID',
    detail TEXT COMMENT '操作详细信息，JSON格式存储',
    ip_address VARCHAR(50) COMMENT '操作IP地址',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    -- 索引：加速查询
    INDEX idx_user_id (user_id),
    INDEX idx_action (action),
    INDEX idx_target (target_type, target_id),
    INDEX idx_created_at (created_at),
    -- 外键约束：用户删除时设置为NULL
    CONSTRAINT fk_log_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='操作日志表';

-- ============================================================
-- 第三阶段：插入初始数据
-- 按照依赖顺序插入：先插入基础数据，再插入关联数据
-- ============================================================

-- ------------------------------------------------------------
-- 插入默认管理员账户
-- 用户名：admin，密码：admin123（BCrypt加密）
-- BCrypt哈希值特点：固定60字符，每次加密结果不同但验证一致
-- ------------------------------------------------------------
INSERT INTO users (username, email, password_hash, role, status)
VALUES ('admin', 'admin@astaros.wiki', '$2a$10$slYQmyNdGzTn7ZLBXBChFOC9f6kFjAqPhccnP6DxlWXx2lPk1C3G6', 'ADMIN', 'ACTIVE');

-- ------------------------------------------------------------
-- 插入测试用户账户（用户常用测试账号）
-- 用户名：Axcis，密码：1q2w3e4r（BCrypt加密）
-- 注意：如果首次登录失败，AuthService会自动重置密码
-- ------------------------------------------------------------
INSERT INTO users (username, email, password_hash, role, status)
VALUES ('Axcis', 'axcis@astaros.wiki', '$2b$10$WQ/PA2S3a61llNVI21siy.FWTWKayBgkhjAnTGpyytFFk5wBeMOGe', 'CREATOR', 'ACTIVE');

-- ------------------------------------------------------------
-- 插入默认分类（顶级分类）
-- ID自动分配：1-6
-- ------------------------------------------------------------
INSERT INTO categories (name, description, parent_id, sort_order) VALUES
('种族', '阿斯塔洛斯世界的种族设定，包括各个种族的起源、特征和文化', NULL, 1),
('地理', '阿斯塔洛斯世界的地理设定，包括大陆、城市、地形等', NULL, 2),
('人物', '阿斯塔洛斯世界的重要人物设定', NULL, 3),
('力量体系', '阿斯塔洛斯世界的力量体系设定，包括魔法、斗气等', NULL, 4),
('历史事件', '阿斯塔洛斯世界的重要历史事件', NULL, 5),
('物品', '阿斯塔洛斯世界的重要物品设定', NULL, 6);

-- ------------------------------------------------------------
-- 插入子分类
-- ID自动分配：7-11
-- parent_id指向对应的父分类ID
-- ------------------------------------------------------------
INSERT INTO categories (name, description, parent_id, sort_order) VALUES
('夜族', '阿斯塔洛斯世界中最神秘的种族之一，以夜晚为生', 1, 1),
('仙灵', '优雅而神秘的种族，与自然和神圣力量紧密相连', 1, 2),
('龙族', '拥有强大力量和悠久历史的传奇种族', 1, 3),
('国家', '阿斯塔洛斯世界中的各个国家和势力', 2, 1),
('神圣本源', '四大本源力量之一，代表光明、正义和治愈', 4, 1);

-- ------------------------------------------------------------
-- 插入示例标签
-- ID自动分配：1-10
-- ------------------------------------------------------------
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

-- ------------------------------------------------------------
-- 插入示例页面
-- ID自动分配：1-3
-- category_id指向对应的子分类ID（不是父分类）
-- status设置为PUBLISHED（已发布状态）
-- ------------------------------------------------------------
INSERT INTO pages (title, content, draft_content, slug, category_id, author_id, status, version, view_count, created_at, updated_at) VALUES
('夜族', '# 夜族\n\n夜族是阿斯塔洛斯世界中最神秘的种族之一，他们以夜晚为生，拥有独特的能力和命运。\n\n## 基本信息\n- **起源**: 创世之夜\n- **特征**: 拥有暗影力量，夜间活动\n- **主要居住地**: 暗影森林\n\n## 能力\n夜族拥有操控暗影的能力，可以在黑暗中自由穿梭，并且擅长暗杀和情报收集。', '# 夜族\n\n夜族是阿斯塔洛斯世界中最神秘的种族之一，他们以夜晚为生，拥有独特的能力和命运。\n\n## 基本信息\n- **起源**: 创世之夜\n- **特征**: 拥有暗影力量，夜间活动\n- **主要居住地**: 暗影森林\n\n## 能力\n夜族拥有操控暗影的能力，可以在黑暗中自由穿梭，并且擅长暗杀和情报收集。', 'night-race', 7, 1, 'PUBLISHED', 1, 15, '2026-06-20 10:00:00', '2026-06-20 10:00:00'),
('神圣本源', '# 神圣本源\n\n神圣本源是四大本源力量之一，代表着光明、正义和治愈的力量。\n\n## 特性\n- **颜色**: 金色光芒\n- **象征**: 太阳、净化、治愈\n- **主要使用者**: 仙灵族、部分人类\n\n## 应用\n神圣本源主要用于治愈、净化和防御，是对抗黑暗力量的重要手段。', '# 神圣本源\n\n神圣本源是四大本源力量之一，代表着光明、正义和治愈的力量。\n\n## 特性\n- **颜色**: 金色光芒\n- **象征**: 太阳、净化、治愈\n- **主要使用者**: 仙灵族、部分人类\n\n## 应用\n神圣本源主要用于治愈、净化和防御，是对抗黑暗力量的重要手段。', 'sacred-origin', 11, 1, 'PUBLISHED', 1, 8, '2026-06-25 14:30:00', '2026-06-25 14:30:00'),
('阿斯塔洛斯大陆', '# 阿斯塔洛斯大陆\n\n阿斯塔洛斯大陆是一个充满奇幻色彩的世界大陆，孕育着各种神奇的生物和文明。\n\n## 地理特征\n- **总面积**: 约500万平方公里\n- **主要地形**: 平原、山脉、森林、海洋\n- **气候**: 多样化，从热带到寒带\n\n## 主要区域\n大陆分为东部、西部、南部、北部和中部五个主要区域，每个区域都有独特的地理特征和文明。', '# 阿斯塔洛斯大陆\n\n阿斯塔洛斯大陆是一个充满奇幻色彩的世界大陆，孕育着各种神奇的生物和文明。\n\n## 地理特征\n- **总面积**: 约500万平方公里\n- **主要地形**: 平原、山脉、森林、海洋\n- **气候**: 多样化，从热带到寒带\n\n## 主要区域\n大陆分为东部、西部、南部、北部和中部五个主要区域，每个区域都有独特的地理特征和文明。', 'astaros-continent', 2, 1, 'PUBLISHED', 1, 23, '2026-06-15 09:15:00', '2026-06-15 09:15:00');

-- ------------------------------------------------------------
-- 插入更多测试页面（用于测试最近更新功能）
-- 不同的创建时间和更新时间，验证排序逻辑
-- ------------------------------------------------------------
-- 夜族子分类页面
INSERT INTO pages (title, content, draft_content, slug, category_id, author_id, status, version, view_count, created_at, updated_at) VALUES
('仙灵族', '# 仙灵族\n\n仙灵族是阿斯塔洛斯世界中优雅而神秘的种族，与自然和神圣力量紧密相连。\n\n## 特征\n- **外貌**: 拥有透明翅膀，身高较高\n- **寿命**: 可活数千年\n- **文化**: 崇尚自然与和谐\n\n## 与其他种族的关系\n仙灵族与夜族保持着微妙的平衡，既不亲近也不敌对。', '# 仙灵族\n\n仙灵族是阿斯塔洛斯世界中优雅而神秘的种族，与自然和神圣力量紧密相连。\n\n## 特征\n- **外貌**: 拥有透明翅膀，身高较高\n- **寿命**: 可活数千年\n- **文化**: 崇尚自然与和谐\n\n## 与其他种族的关系\n仙灵族与夜族保持着微妙的平衡，既不亲近也不敌对。', 'fairy-race', 8, 2, 'PUBLISHED', 2, 45, '2026-06-10 11:00:00', '2026-06-28 16:45:00'),
('龙族', '# 龙族\n\n龙族是拥有强大力量和悠久历史的传奇种族，他们是世界上最古老的智慧生物之一。\n\n## 分类\n- **金龙**: 最尊贵的龙族，拥有神圣力量\n- **银龙**: 智慧之龙，擅长魔法\n- **黑龙**: 力量之龙，战斗能力最强\n\n## 历史\n龙族曾经统治着整片大陆，但在大灾变后数量锐减。', '# 龙族\n\n龙族是拥有强大力量和悠久历史的传奇种族，他们是世界上最古老的智慧生物之一。\n\n## 分类\n- **金龙**: 最尊贵的龙族，拥有神圣力量\n- **银龙**: 智慧之龙，擅长魔法\n- **黑龙**: 力量之龙，战斗能力最强\n\n## 历史\n龙族曾经统治着整片大陆，但在大灾变后数量锐减。', 'dragon-race', 9, 2, 'PUBLISHED', 1, 32, '2026-06-05 08:30:00', '2026-06-05 08:30:00');

-- 地理子分类页面（国家）
INSERT INTO pages (title, content, draft_content, slug, category_id, author_id, status, version, view_count, created_at, updated_at) VALUES
('圣光帝国', '# 圣光帝国\n\n圣光帝国是阿斯塔洛斯大陆上最强大的人类帝国，以神圣本源为信仰核心。\n\n## 首都\n- **圣城**: 帝国的政治、宗教和文化中心\n- **人口**: 超过五百万\n\n## 军事\n帝国拥有大陆上最庞大的军队，以圣骑士团闻名于世。', '# 圣光帝国\n\n圣光帝国是阿斯塔洛斯大陆上最强大的人类帝国，以神圣本源为信仰核心。\n\n## 首都\n- **圣城**: 帝国的政治、宗教和文化中心\n- **人口**: 超过五百万\n\n## 军事\n帝国拥有大陆上最庞大的军队，以圣骑士团闻名于世。', 'holy-light-empire', 10, 1, 'PUBLISHED', 3, 67, '2026-06-18 13:20:00', '2026-06-29 20:10:00'),
('暗影王国', '# 暗影王国\n\n暗影王国是夜族建立的神秘国度，位于大陆的最北端。\n\n## 地理位置\n- **气候**: 常年黑暗，极夜现象频繁\n- **地形**: 崎岖的山脉和幽深的森林\n\n## 文化\n暗影王国的文化以隐秘和沉默著称，外人很难了解其内部事务。', '# 暗影王国\n\n暗影王国是夜族建立的神秘国度，位于大陆的最北端。\n\n## 地理位置\n- **气候**: 常年黑暗，极夜现象频繁\n- **地形**: 崎岖的山脉和幽深的森林\n\n## 文化\n暗影王国的文化以隐秘和沉默著称，外人很难了解其内部事务。', 'shadow-kingdom', 10, 2, 'PUBLISHED', 2, 28, '2026-06-12 15:45:00', '2026-06-22 09:30:00');

-- 人物分类页面
INSERT INTO pages (title, content, draft_content, slug, category_id, author_id, status, version, view_count, created_at, updated_at) VALUES
('光明使者艾琳', '# 光明使者艾琳\n\n艾琳是圣光帝国最伟大的圣骑士，被称为光明的化身。\n\n## 生平\n- **出生**: 圣光帝国的一个小村庄\n- **成就**: 多次拯救帝国于危难之中\n- **称号**: 光明使者\n\n## 能力\n艾琳拥有强大的神圣本源力量，可以治愈任何创伤。', '# 光明使者艾琳\n\n艾琳是圣光帝国最伟大的圣骑士，被称为光明的化身。\n\n## 生平\n- **出生**: 圣光帝国的一个小村庄\n- **成就**: 多次拯救帝国于危难之中\n- **称号**: 光明使者\n\n## 能力\n艾琳拥有强大的神圣本源力量，可以治愈任何创伤。', 'irene-the-holy', 3, 1, 'PUBLISHED', 1, 89, '2026-06-22 10:00:00', '2026-06-22 10:00:00'),
('暗影女王露娜', '# 暗影女王露娜\n\n露娜是暗影王国的统治者，夜族历史上最强大的女王。\n\n## 统治\n- **统治时间**: 超过两百年\n- **政策**: 平衡与中立\n- **影响力**: 整个大陆都敬畏她的力量\n\n## 传说\n据说露娜在大灾变中失去了挚爱，从此变得冷酷无情。', '# 暗影女王露娜\n\n露娜是暗影王国的统治者，夜族历史上最强大的女王。\n\n## 统治\n- **统治时间**: 超过两百年\n- **政策**: 平衡与中立\n- **影响力**: 整个大陆都敬畏她的力量\n\n## 传说\n据说露娜在大灾变中失去了挚爱，从此变得冷酷无情。', 'luna-shadow-queen', 3, 2, 'PUBLISHED', 2, 54, '2026-06-08 14:00:00', '2026-06-27 18:20:00');

-- 历史事件分类页面
INSERT INTO pages (title, content, draft_content, slug, category_id, author_id, status, version, view_count, created_at, updated_at) VALUES
('第一次神圣战争', '# 第一次神圣战争\n\n第一次神圣战争是阿斯塔洛斯历史上最惨烈的战争，持续了整整一百年。\n\n## 起因\n- **资源争夺**: 神圣本源的控制\n- **信仰冲突**: 光明与黑暗的对立\n\n## 结果\n战争以圣光帝国的胜利告终，但双方都付出了巨大的代价。', '# 第一次神圣战争\n\n第一次神圣战争是阿斯塔洛斯历史上最惨烈的战争，持续了整整一百年。\n\n## 起因\n- **资源争夺**: 神圣本源的控制\n- **信仰冲突**: 光明与黑暗的对立\n\n## 结果\n战争以圣光帝国的胜利告终，但双方都付出了巨大的代价。', 'first-holy-war', 5, 1, 'PUBLISHED', 1, 41, '2026-06-01 00:00:00', '2026-06-01 00:00:00'),
('大灾变', '# 大灾变\n\n大灾变是阿斯塔洛斯世界历史上最黑暗的时期，几乎毁灭了整个文明。\n\n## 灾难\n- **天空破裂**: 异界生物入侵\n- **大地崩塌**: 大陆分裂\n- **人口锐减**: 九成人口死亡\n\n## 影响\n大灾变后，世界重新洗牌，新的势力崛起。', '# 大灾变\n\n大灾变是阿斯塔洛斯世界历史上最黑暗的时期，几乎毁灭了整个文明。\n\n## 灾难\n- **天空破裂**: 异界生物入侵\n- **大地崩塌**: 大陆分裂\n- **人口锐减**: 九成人口死亡\n\n## 影响\n大灾变后，世界重新洗牌，新的势力崛起。', 'great-cataclysm', 5, 2, 'PUBLISHED', 3, 76, '2026-06-03 06:00:00', '2026-06-30 12:00:00');

-- 物品分类页面
INSERT INTO pages (title, content, draft_content, slug, category_id, author_id, status, version, view_count, created_at, updated_at) VALUES
('光明圣剑', '# 光明圣剑\n\n光明圣剑是圣光帝国的国宝，传说由神圣本源凝聚而成。\n\n## 属性\n- **材质**: 神圣水晶\n- **能力**: 释放神圣光芒，净化邪恶\n- **持有者**: 历代圣骑士团长\n\n## 传说\n光明圣剑在第一次神圣战争中发挥了决定性作用。', '# 光明圣剑\n\n光明圣剑是圣光帝国的国宝，传说由神圣本源凝聚而成。\n\n## 属性\n- **材质**: 神圣水晶\n- **能力**: 释放神圣光芒，净化邪恶\n- **持有者**: 历代圣骑士团长\n\n## 传说\n光明圣剑在第一次神圣战争中发挥了决定性作用。', 'holy-sword', 6, 1, 'PUBLISHED', 1, 35, '2026-06-16 09:00:00', '2026-06-16 09:00:00'),
('暗影宝珠', '# 暗影宝珠\n\n暗影宝珠是暗影王国的秘宝，蕴含着强大的暗影力量。\n\n## 属性\n- **外观**: 黑色球体，内部流动着暗影能量\n- **能力**: 操控暗影，制造幻象\n- **持有者**: 暗影女王\n\n## 传说\n暗影宝珠是大灾变后从异界获得的神秘物品。', '# 暗影宝珠\n\n暗影宝珠是暗影王国的秘宝，蕴含着强大的暗影力量。\n\n## 属性\n- **外观**: 黑色球体，内部流动着暗影能量\n- **能力**: 操控暗影，制造幻象\n- **持有者**: 暗影女王\n\n## 传说\n暗影宝珠是大灾变后从异界获得的神秘物品。', 'shadow-orb', 6, 2, 'PUBLISHED', 2, 22, '2026-06-14 11:30:00', '2026-06-24 15:50:00');

-- ------------------------------------------------------------
-- 添加一个已发布但有草稿的页面（测试草稿功能）
-- ------------------------------------------------------------
INSERT INTO pages (title, content, draft_content, slug, category_id, author_id, status, draft_status, version, view_count, created_at, updated_at) VALUES
('深渊恶魔', '# 深渊恶魔\n\n深渊恶魔是来自异界的邪恶生物，以破坏和毁灭为乐。\n\n## 特征\n- **外貌**: 狰狞恐怖，拥有巨大的翅膀\n- **力量**: 黑暗魔法\n- **弱点**: 神圣力量', '# 深渊恶魔\n\n深渊恶魔是来自异界的邪恶生物，以破坏和毁灭为乐。\n\n## 特征\n- **外貌**: 狰狞恐怖，拥有巨大的翅膀\n- **力量**: 黑暗魔法\n- **弱点**: 神圣力量\n\n## 最新研究\n最近发现深渊恶魔的数量正在增加，可能预示着新的危机。', 'abyss-demon', 7, 1, 'PUBLISHED', 'SAVED', 3, 19, '2026-06-17 07:00:00', '2026-06-17 07:00:00');

-- ------------------------------------------------------------
-- 为示例页面添加标签
-- 使用子查询关联，避免硬编码ID导致的偏移问题
-- ------------------------------------------------------------
-- 夜族页面添加"夜族"标签
INSERT INTO page_tags (page_id, tag_id)
SELECT p.id, t.id FROM pages p, tags t WHERE p.slug = 'night-race' AND t.name = '夜族';

-- 神圣本源页面添加"神圣"标签
INSERT INTO page_tags (page_id, tag_id)
SELECT p.id, t.id FROM pages p, tags t WHERE p.slug = 'sacred-origin' AND t.name = '神圣';

-- 阿斯塔洛斯大陆页面添加"起源传说"标签
INSERT INTO page_tags (page_id, tag_id)
SELECT p.id, t.id FROM pages p, tags t WHERE p.slug = 'astaros-continent' AND t.name = '起源传说';

-- 新页面添加标签
INSERT INTO page_tags (page_id, tag_id) SELECT p.id, t.id FROM pages p, tags t WHERE p.slug = 'fairy-race' AND t.name = '仙灵';
INSERT INTO page_tags (page_id, tag_id) SELECT p.id, t.id FROM pages p, tags t WHERE p.slug = 'dragon-race' AND t.name = '龙族';
INSERT INTO page_tags (page_id, tag_id) SELECT p.id, t.id FROM pages p, tags t WHERE p.slug = 'holy-light-empire' AND t.name = '神圣';
INSERT INTO page_tags (page_id, tag_id) SELECT p.id, t.id FROM pages p, tags t WHERE p.slug = 'shadow-kingdom' AND t.name = '夜族';
INSERT INTO page_tags (page_id, tag_id) SELECT p.id, t.id FROM pages p, tags t WHERE p.slug = 'shadow-kingdom' AND t.name = '黑暗';
INSERT INTO page_tags (page_id, tag_id) SELECT p.id, t.id FROM pages p, tags t WHERE p.slug = 'irene-the-holy' AND t.name = '神圣';
INSERT INTO page_tags (page_id, tag_id) SELECT p.id, t.id FROM pages p, tags t WHERE p.slug = 'luna-shadow-queen' AND t.name = '夜族';
INSERT INTO page_tags (page_id, tag_id) SELECT p.id, t.id FROM pages p, tags t WHERE p.slug = 'luna-shadow-queen' AND t.name = '黑暗';
INSERT INTO page_tags (page_id, tag_id) SELECT p.id, t.id FROM pages p, tags t WHERE p.slug = 'first-holy-war' AND t.name = '起源传说';
INSERT INTO page_tags (page_id, tag_id) SELECT p.id, t.id FROM pages p, tags t WHERE p.slug = 'first-holy-war' AND t.name = '神圣';
INSERT INTO page_tags (page_id, tag_id) SELECT p.id, t.id FROM pages p, tags t WHERE p.slug = 'great-cataclysm' AND t.name = '起源传说';
INSERT INTO page_tags (page_id, tag_id) SELECT p.id, t.id FROM pages p, tags t WHERE p.slug = 'holy-sword' AND t.name = '神圣';
INSERT INTO page_tags (page_id, tag_id) SELECT p.id, t.id FROM pages p, tags t WHERE p.slug = 'shadow-orb' AND t.name = '黑暗';
INSERT INTO page_tags (page_id, tag_id) SELECT p.id, t.id FROM pages p, tags t WHERE p.slug = 'abyss-demon' AND t.name = '恶魔';

-- ------------------------------------------------------------
-- 插入版本历史数据（用于测试历史版本查看和回滚功能）
-- ------------------------------------------------------------
-- 大灾变页面版本历史
INSERT INTO page_versions (page_id, content, editor_id, version_number, created_at)
SELECT p.id, '# 大灾变\n\n大灾变是阿斯塔洛斯世界历史上最黑暗的时期。', 1, 1, '2026-06-03 06:00:00' FROM pages p WHERE p.slug = 'great-cataclysm';
INSERT INTO page_versions (page_id, content, editor_id, version_number, created_at)
SELECT p.id, '# 大灾变\n\n大灾变是阿斯塔洛斯世界历史上最黑暗的时期，几乎毁灭了整个文明。\n\n## 灾难\n- **天空破裂**: 异界生物入侵\n- **大地崩塌**: 大陆分裂', 2, 2, '2026-06-15 08:00:00' FROM pages p WHERE p.slug = 'great-cataclysm';
INSERT INTO page_versions (page_id, content, editor_id, version_number, created_at)
SELECT p.id, '# 大灾变\n\n大灾变是阿斯塔洛斯世界历史上最黑暗的时期，几乎毁灭了整个文明。\n\n## 灾难\n- **天空破裂**: 异界生物入侵\n- **大地崩塌**: 大陆分裂\n- **人口锐减**: 九成人口死亡\n\n## 影响\n大灾变后，世界重新洗牌，新的势力崛起。', 1, 3, '2026-06-30 12:00:00' FROM pages p WHERE p.slug = 'great-cataclysm';

-- 圣光帝国页面版本历史
INSERT INTO page_versions (page_id, content, editor_id, version_number, created_at)
SELECT p.id, '# 圣光帝国\n\n圣光帝国是阿斯塔洛斯大陆上最强大的人类帝国。', 1, 1, '2026-06-18 13:20:00' FROM pages p WHERE p.slug = 'holy-light-empire';
INSERT INTO page_versions (page_id, content, editor_id, version_number, created_at)
SELECT p.id, '# 圣光帝国\n\n圣光帝国是阿斯塔洛斯大陆上最强大的人类帝国，以神圣本源为信仰核心。\n\n## 首都\n- **圣城**: 帝国的政治、宗教和文化中心', 2, 2, '2026-06-25 10:00:00' FROM pages p WHERE p.slug = 'holy-light-empire';
INSERT INTO page_versions (page_id, content, editor_id, version_number, created_at)
SELECT p.id, '# 圣光帝国\n\n圣光帝国是阿斯塔洛斯大陆上最强大的人类帝国，以神圣本源为信仰核心。\n\n## 首都\n- **圣城**: 帝国的政治、宗教和文化中心\n- **人口**: 超过五百万\n\n## 军事\n帝国拥有大陆上最庞大的军队，以圣骑士团闻名于世。', 1, 3, '2026-06-29 20:10:00' FROM pages p WHERE p.slug = 'holy-light-empire';

-- 深渊恶魔页面版本历史（有草稿的页面）
INSERT INTO page_versions (page_id, content, editor_id, version_number, created_at)
SELECT p.id, '# 深渊恶魔\n\n深渊恶魔是来自异界的邪恶生物。', 1, 1, '2026-06-17 07:00:00' FROM pages p WHERE p.slug = 'abyss-demon';
INSERT INTO page_versions (page_id, content, editor_id, version_number, created_at)
SELECT p.id, '# 深渊恶魔\n\n深渊恶魔是来自异界的邪恶生物，以破坏和毁灭为乐。\n\n## 特征\n- **外貌**: 狰狞恐怖，拥有巨大的翅膀\n- **力量**: 黑暗魔法', 2, 2, '2026-06-20 11:00:00' FROM pages p WHERE p.slug = 'abyss-demon';
INSERT INTO page_versions (page_id, content, editor_id, version_number, created_at)
SELECT p.id, '# 深渊恶魔\n\n深渊恶魔是来自异界的邪恶生物，以破坏和毁灭为乐。\n\n## 特征\n- **外貌**: 狰狞恐怖，拥有巨大的翅膀\n- **力量**: 黑暗魔法\n- **弱点**: 神圣力量\n\n## 最新研究\n最近发现深渊恶魔的数量正在增加，可能预示着新的危机。', 1, 3, '2026-06-28 14:00:00' FROM pages p WHERE p.slug = 'abyss-demon';

-- ============================================================
-- 第四阶段：恢复约束和验证
-- ============================================================

-- 重新启用外键约束检查
SET FOREIGN_KEY_CHECKS = 1;

-- ============================================================
-- 验证初始化结果
-- 查询各表的数据量，确认初始化成功
-- ============================================================
SELECT '数据库初始化完成！' AS message;
SELECT COUNT(*) AS user_count FROM users;
SELECT COUNT(*) AS category_count FROM categories;
SELECT COUNT(*) AS tag_count FROM tags;
SELECT COUNT(*) AS page_count FROM pages;
SELECT COUNT(*) AS message_count FROM messages;
SELECT COUNT(*) AS page_tags_count FROM page_tags;