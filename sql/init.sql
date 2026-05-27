-- 企业级私有化大模型系统 - 数据库初始化脚本

CREATE DATABASE IF NOT EXISTS enterprise_llm DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE enterprise_llm;

-- 用户表
CREATE TABLE IF NOT EXISTS `user` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `username` VARCHAR(50) NOT NULL UNIQUE,
    `password` VARCHAR(255) NOT NULL,
    `role` VARCHAR(20) NOT NULL DEFAULT 'USER' COMMENT 'ADMIN / USER',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 知识库文档表
CREATE TABLE IF NOT EXISTS `knowledge_doc` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `title` VARCHAR(200) NOT NULL,
    `content` TEXT,
    `file_path` VARCHAR(500),
    `created_by` BIGINT NOT NULL,
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 提示词配置表
CREATE TABLE IF NOT EXISTS `prompt_config` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `name` VARCHAR(100) NOT NULL,
    `system_prompt` TEXT NOT NULL,
    `created_by` BIGINT NOT NULL,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 对话历史表
CREATE TABLE IF NOT EXISTS `chat_history` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL,
    `role` VARCHAR(20) NOT NULL COMMENT 'user / assistant',
    `content` TEXT NOT NULL,
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_time (`user_id`, `created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 预置用户（密码使用 BCrypt，明文分别为 admin123 和 user123）
INSERT INTO `user` (`username`, `password`, `role`) VALUES
    ('admin', '$2a$10$lVP8nYPZ0OiYKcnQGXJxN.kUBipq1S885/q7kTyK6h5VhOFlq5LXe', 'ADMIN'),
    ('user',  '$2a$10$cdl2SZ/8GhumBCQDLq4yhupSf2fTjbc1biK8WFFB5AyVz6pPqHuNq', 'USER')
ON DUPLICATE KEY UPDATE username = username;
