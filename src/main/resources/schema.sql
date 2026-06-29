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