USE astaros_wiki;

-- 更新管理员密码为 admin123
UPDATE users SET password_hash = '$2a$10$N9qo8uLOickgx2ZMRZoMye.IjzqAKL9xL5jvMFVdNJHvGCgTq/VEq' WHERE username = 'admin';

-- 确保管理员状态为 ACTIVE
UPDATE users SET status = 'ACTIVE' WHERE username = 'admin';

-- 验证更新结果
SELECT username, password_hash, status FROM users WHERE username = 'admin';