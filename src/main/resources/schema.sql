CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    account_id VARCHAR(64) NOT NULL COMMENT '随机账号ID',
    
    -- 添加唯一索引
    UNIQUE KEY uk_username (username),
    UNIQUE KEY uk_account_id (account_id)
);
CREATE TABLE `knowledge_chunk` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `repo_name` varchar(100) NOT NULL COMMENT '仓库名',
  `user_id` bigint NOT NULL COMMENT '关联的用户ID',
  `file_name` varchar(100) NOT NULL COMMENT '源文件名，如 核心人员画像——张三.md',
  `content` text NOT NULL COMMENT '文本块的具体内容',
  `vector_data` JSON NOT NULL COMMENT 'file_name结果Embedding 后的向量数据，暂时以 JSON 字符串或 Text 存储',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  -- 添加普通索引提升查询效率
  KEY `idx_user_id` (`user_id`),
  -- 添加外键约束（可选，推荐在物理表或逻辑层面建立关联）
  CONSTRAINT `fk_knowledge_chunk_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
-- 创建 user_config 表
CREATE TABLE `user_config` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    `user_id` BIGINT NOT NULL COMMENT '关联的用户ID',
    `platform_source` VARCHAR(32) NOT NULL COMMENT 'API来源只能为DeepSeek或者OpenAI',
    `api_key` VARCHAR(512) NOT NULL COMMENT '用户密钥',
    `base_url` VARCHAR(255) DEFAULT NULL COMMENT '自定义中转地址',
    `is_active` TINYINT(1) DEFAULT 1 COMMENT '是否启用',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_user_platform` (`user_id`, `platform_source`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户API密钥配置表';


-- 创建 chat_message 表
CREATE TABLE `chat_message` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '聊天记录主键ID',
    `user_id` BIGINT NOT NULL COMMENT '关联的用户ID',
    `repo_name` VARCHAR(100) NOT NULL COMMENT '知识库名称',
    `room_name` VARCHAR(100) NOT NULL COMMENT '聊天室名称',
    `user_message` TEXT NOT NULL COMMENT '用户提问',
    `ai_message` TEXT DEFAULT NULL COMMENT 'AI回复',
    `system_message` TEXT DEFAULT NULL COMMENT '系统提示词',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '对话时间',
    INDEX `idx_user_repo_room` (`user_id`, `repo_name`, `room_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='人机聊天记录表';