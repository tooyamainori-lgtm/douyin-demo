-- ============================================================
-- Douyin Web Demo — 数据库初始化脚本
-- 所有表结构变更必须同步更新此文件
-- 数据库：MySQL 8.0
-- 字符集：utf8mb4
-- ============================================================

CREATE DATABASE IF NOT EXISTS `douyin` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `douyin`;

-- ============================================================
-- 用户模块
-- ============================================================

-- 用户表
CREATE TABLE `user` (
    `id`          BIGINT       NOT NULL COMMENT '主键ID（雪花算法）',
    `username`    VARCHAR(30)  NOT NULL COMMENT '用户名，3-30位字母数字下划线',
    `password`    VARCHAR(255) NOT NULL COMMENT '密码（BCrypt 加密）',
    `nickname`    VARCHAR(30)  NOT NULL DEFAULT '' COMMENT '昵称',
    `avatar_url`  VARCHAR(500)          DEFAULT NULL COMMENT '头像URL（MinIO）',
    `bio`         VARCHAR(200)          DEFAULT '' COMMENT '个人简介',
    `gender`      TINYINT(1)            DEFAULT 0 COMMENT '性别：0-未知 1-男 2-女',
    `birthday`    DATE                  DEFAULT NULL COMMENT '生日',
    `phone`       VARCHAR(11)           DEFAULT NULL COMMENT '手机号',
    `email`       VARCHAR(100)          DEFAULT NULL COMMENT '邮箱',
    `status`      TINYINT(1)   NOT NULL DEFAULT 1 COMMENT '状态：0-禁用 1-正常',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted`  TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_username` (`username`),
    UNIQUE KEY `uk_user_phone` (`phone`),
    UNIQUE KEY `uk_user_email` (`email`),
    KEY `idx_user_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 关注关系表
CREATE TABLE `follow` (
    `id`          BIGINT     NOT NULL COMMENT '主键ID',
    `follower_id`  BIGINT     NOT NULL COMMENT '关注者用户ID',
    `followee_id`  BIGINT     NOT NULL COMMENT '被关注者用户ID',
    `create_time` DATETIME   NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '关注时间',
    `is_deleted`  TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_follow_relation` (`follower_id`, `followee_id`),
    KEY `idx_follow_follower` (`follower_id`),
    KEY `idx_follow_followee` (`followee_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='关注关系表';

-- ============================================================
-- 视频模块
-- ============================================================

-- 视频表
CREATE TABLE `video` (
    `id`            BIGINT        NOT NULL COMMENT '主键ID（雪花算法）',
    `user_id`       BIGINT        NOT NULL COMMENT '发布者用户ID',
    `title`         VARCHAR(100)  NOT NULL COMMENT '视频标题，1-100字',
    `description`   VARCHAR(500)           DEFAULT '' COMMENT '视频描述',
    `video_url`     VARCHAR(500)  NOT NULL COMMENT '视频文件URL（MinIO）',
    `cover_url`     VARCHAR(500)           DEFAULT NULL COMMENT '封面图片URL（MinIO）',
    `duration`      DECIMAL(10,2)          DEFAULT 0 COMMENT '视频时长（秒）',
    `width`         INT                    DEFAULT 0 COMMENT '视频宽度（像素）',
    `height`        INT                    DEFAULT 0 COMMENT '视频高度（像素）',
    `size`          BIGINT                 DEFAULT 0 COMMENT '文件大小（字节）',
    `tags`          VARCHAR(500)           DEFAULT '' COMMENT '标签，逗号分隔',
    `status`        TINYINT(1)    NOT NULL DEFAULT 1 COMMENT '状态：0-审核不通过 1-正常 2-转码中',
    `view_count`    BIGINT        NOT NULL DEFAULT 0 COMMENT '播放量',
    `like_count`    BIGINT        NOT NULL DEFAULT 0 COMMENT '点赞数（冗余字段，提升查询性能）',
    `comment_count` BIGINT        NOT NULL DEFAULT 0 COMMENT '评论数（冗余字段）',
    `share_count`   BIGINT        NOT NULL DEFAULT 0 COMMENT '分享数（冗余字段）',
    `create_time`   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间',
    `update_time`   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted`    TINYINT(1)    NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
    PRIMARY KEY (`id`),
    KEY `idx_video_user_id` (`user_id`),
    KEY `idx_video_create_time` (`create_time`),
    KEY `idx_video_title` (`title`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='视频表';

-- ============================================================
-- 互动模块
-- ============================================================

-- 点赞表
CREATE TABLE `like_record` (
    `id`          BIGINT     NOT NULL COMMENT '主键ID',
    `user_id`     BIGINT     NOT NULL COMMENT '点赞用户ID',
    `video_id`    BIGINT     NOT NULL COMMENT '被点赞视频ID',
    `create_time` DATETIME   NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '点赞时间',
    `is_deleted`  TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除（取消点赞即为删除）',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_like_user_video` (`user_id`, `video_id`),
    KEY `idx_like_video_id` (`video_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='点赞记录表';

-- 评论表
CREATE TABLE `comment` (
    `id`          BIGINT       NOT NULL COMMENT '主键ID',
    `user_id`     BIGINT       NOT NULL COMMENT '评论用户ID',
    `video_id`    BIGINT       NOT NULL COMMENT '所属视频ID',
    `parent_id`   BIGINT                DEFAULT NULL COMMENT '父评论ID（NULL表示一级评论）',
    `content`     VARCHAR(500) NOT NULL COMMENT '评论内容，1-500字',
    `like_count`  BIGINT       NOT NULL DEFAULT 0 COMMENT '点赞数',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '评论时间',
    `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted`  TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
    PRIMARY KEY (`id`),
    KEY `idx_comment_video_id` (`video_id`),
    KEY `idx_comment_user_id` (`user_id`),
    KEY `idx_comment_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评论表';
