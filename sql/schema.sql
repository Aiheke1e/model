-- ============================================================
-- Gemify Auth Module - MySQL DDL
-- Database: gemify_test
-- Charset: utf8mb4
-- ============================================================

CREATE DATABASE IF NOT EXISTS `gemify_test`
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE `gemify_test`;

-- ------------------------------------------------------------
-- 用户表
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `users` (
    `id`              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `phone`           VARCHAR(20)  NULL     COMMENT '手机号，E.164 格式，如 +8613812345678',
    `email`           VARCHAR(128) NULL     COMMENT '邮箱，小写存储',
    `password_hash`   VARCHAR(255) NULL     COMMENT 'BCrypt 密码哈希，未设密码为 NULL',
    `password_algo`   VARCHAR(32)  NULL     DEFAULT 'bcrypt' COMMENT '密码算法',
    `status`          TINYINT      NOT NULL DEFAULT 1 COMMENT '状态：1=正常 2=禁用 3=冻结',
    `last_login_at`   DATETIME     NULL     COMMENT '最近登录时间',
    `created_at`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_phone` (`phone`),
    UNIQUE KEY `uk_email` (`email`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='用户表';

-- ------------------------------------------------------------
-- 验证码表（手机 / 邮箱共用）
-- purpose:
--   login          验证码登录（不存在则自动注册）
--   reset_password 找回密码（仅已设密账号）
--   bind           绑定手机或邮箱
-- identity_type: phone | email
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `verification_codes` (
    `id`              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `identity_type`   VARCHAR(16)  NOT NULL COMMENT '标识类型：phone | email',
    `identifier`      VARCHAR(128) NOT NULL COMMENT '规范化后的手机号或邮箱',
    `purpose`         VARCHAR(32)  NOT NULL COMMENT '用途：login | reset_password | bind',
    `code_hash`       VARCHAR(128) NOT NULL COMMENT '验证码哈希，不存明文',
    `attempt_count`   INT          NOT NULL DEFAULT 0 COMMENT '已尝试验证次数',
    `max_attempts`    INT          NOT NULL DEFAULT 5 COMMENT '最大尝试次数',
    `expires_at`      DATETIME     NOT NULL COMMENT '过期时间',
    `used_at`         DATETIME     NULL     COMMENT '使用时间，非空表示已使用',
    `ip_address`      VARCHAR(45)  NULL     COMMENT '请求IP',
    `created_at`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_lookup` (`identity_type`, `identifier`, `purpose`, `expires_at`),
    KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='验证码表';

-- ------------------------------------------------------------
-- 用户会话表（Refresh Token，支持多设备）
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `user_sessions` (
    `id`                  BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id`             BIGINT       NOT NULL COMMENT '用户ID',
    `refresh_token_hash`  VARCHAR(128) NOT NULL COMMENT 'Refresh Token 哈希',
    `device_id`           VARCHAR(64)  NULL     COMMENT '设备标识',
    `device_name`         VARCHAR(64)  NULL     COMMENT '设备名称',
    `user_agent`          VARCHAR(512) NULL     COMMENT 'User-Agent',
    `ip_address`          VARCHAR(45)  NULL     COMMENT '登录IP',
    `expires_at`          DATETIME     NOT NULL COMMENT 'Refresh Token 过期时间',
    `last_active_at`      DATETIME     NULL     COMMENT '最近活跃时间',
    `created_at`          DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_refresh_token_hash` (`refresh_token_hash`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_expires_at` (`expires_at`),
    CONSTRAINT `fk_user_sessions_user_id`
        FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
        ON DELETE CASCADE
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='用户会话表';
