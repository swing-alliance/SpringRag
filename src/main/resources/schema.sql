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
CREATE TABLE IF NOT EXISTS `user_config` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    `user_id` BIGINT NOT NULL COMMENT '关联的用户ID',
    `platform_source` VARCHAR(32) NOT NULL COMMENT 'API来源，固定为 DEEPSEEK 或 OPENAI',
    `api_key` VARCHAR(512) NOT NULL COMMENT '加密存储或明文存储的用户密钥',
    `base_url` VARCHAR(255) DEFAULT NULL COMMENT '可选：允许用户自定义中转地址，不填则用系统默认',
    `is_active` TINYINT(1) DEFAULT 1 COMMENT '是否启用：1启用，0禁用',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    -- 建立索引提升根据用户和平台查询的效率
    KEY `idx_user_platform` (`user_id`, `platform_source`),
    
    -- 外键约束：当用户被删除时，其对应的配置自动级联删除
    CONSTRAINT `fk_config_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户API密钥配置表';
CREATE TABLE IF NOT EXISTS `chat_message` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '聊天记录主键ID',
    `user_id` BIGINT NOT NULL COMMENT '关联的用户ID，表示是谁在聊天',
    `repo_name` VARCHAR(100) NOT NULL COMMENT '关联的知识库/仓库名，对应 knowledge_chunk 的 repo_name',
    `room_name` VARCHAR(100) NOT NULL COMMENT '聊天室房间名/会话名称',
    `user_message` TEXT NOT NULL COMMENT '用户说的话（提问内容）',
    `ai_message` TEXT DEFAULT NULL COMMENT 'AI 回复的话',
    `system_message` TEXT DEFAULT NULL COMMENT '可选：当时使用的系统提示词',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '对话时间',

    -- 【核心索引优化】
    -- 1. 最常用的查询：查询某个用户在某个知识库的某个房间内的所有聊天记录（按时间正序排列）
    -- 满足最左匹配原则：(user_id) -> (user_id, repo_name) -> (user_id, repo_name, room_name) 全部走索引
    KEY `idx_user_repo_room` (`user_id`, `repo_name`, `room_name`),
    
    -- 2. 备用索引（可选）：如果你的业务需要单独查询“某个房间下的所有消息”，而不限制知识库，可以放开下面这个索引
    -- KEY `idx_room_name` (`room_name`),

    -- 外键约束
    CONSTRAINT `fk_chat_message_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='人机聊天室对话记录表';