CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    account_id VARCHAR(64) NOT NULL COMMENT '随机账号ID',
    
    -- 添加唯一索引
    UNIQUE KEY uk_username (username),
    UNIQUE KEY uk_account_id (account_id)
);