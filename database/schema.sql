-- MySQL dump 10.13  Distrib 8.0.36, for Win64 (x86_64)
--
-- Host: localhost    Database: photo_share
-- ------------------------------------------------------
-- Server version	8.0.36

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `album`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `album` (
  `id` varchar(36) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '相册ID（UUID）',
  `title` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '相册标题',
  `description` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '相册描述',
  `cover_url` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '封面照片URL',
  `visibility` varchar(10) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'PRIVATE' COMMENT '可见范围：PRIVATE=仅本人，PUBLIC=关注者可见',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除（0=正常，1=已删除）',
  `user_id` varchar(36) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '所属用户ID',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_user_visibility_time` (`user_id`,`visibility`,`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='相册表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `album_comment`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `album_comment` (
  `id` varchar(36) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '评论ID（UUID）',
  `album_id` varchar(36) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '被评论的相册ID',
  `parent_id` varchar(36) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '被回复的评论ID（NULL=一级评论）',
  `reply_to_user_id` varchar(36) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '被回复人ID，用于显示"回复 @某人"',
  `user_id` varchar(36) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '评论人ID',
  `content` varchar(500) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '评论内容',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除（0=正常，1=已删除）',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '评论时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_album_time` (`album_id`,`created_at`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_parent` (`parent_id`),
  KEY `fk_comment_reply_user` (`reply_to_user_id`),
  CONSTRAINT `fk_comment_album` FOREIGN KEY (`album_id`) REFERENCES `album` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_comment_parent` FOREIGN KEY (`parent_id`) REFERENCES `album_comment` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_comment_reply_user` FOREIGN KEY (`reply_to_user_id`) REFERENCES `user` (`id`) ON DELETE SET NULL,
  CONSTRAINT `fk_comment_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='相册评论表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `notification`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `notification` (
  `id` varchar(36) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '通知ID（UUID）',
  `user_id` varchar(36) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '接收人ID',
  `actor_id` varchar(36) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '触发人ID（NULL=系统或用户已注销）',
  `type` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'LIKE=点赞，COMMENT=评论，REPLY=回复，FOLLOW=关注，MENTION=@提及',
  `target_type` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '跳转目标类型：ALBUM=相册，PHOTO=照片，USER=用户（关注）',
  `target_id` varchar(36) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '点击通知后的跳转目标ID',
  `snippet` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '内容摘要（评论片段等），列表直接展示',
  `is_read` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否已读（0=未读，1=已读）',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '通知时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_read_time` (`user_id`,`is_read`,`created_at`),
  KEY `idx_event` (`actor_id`,`type`,`target_type`,`target_id`),
  CONSTRAINT `fk_notice_actor` FOREIGN KEY (`actor_id`) REFERENCES `user` (`id`) ON DELETE SET NULL,
  CONSTRAINT `fk_notice_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='站内通知表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `photo`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `photo` (
  `id` varchar(36) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '照片ID（UUID）',
  `album_id` varchar(36) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '所属相册ID',
  `file_name` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '原始文件名',
  `file_url` varchar(500) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '文件存储路径/URL',
  `file_size` bigint NOT NULL COMMENT '文件大小（字节）',
  `mime_type` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '文件类型（image/jpeg等）',
  `sort_order` int NOT NULL DEFAULT '0' COMMENT '排序序号',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除（0=正常，1=已删除）',
  `user_id` varchar(36) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '所属用户ID',
  `status` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT 'pending' COMMENT '审核状态：pending(待审核)、approved(已通过)、rejected(已拒绝)',
  `reviewed_by` varchar(36) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '审核人ID（管理员ID）',
  `reviewed_at` datetime DEFAULT NULL COMMENT '审核时间',
  `reject_reason` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '拒绝原因（可选）',
  `uploader_nickname` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'contributor nickname',
  PRIMARY KEY (`id`),
  KEY `idx_album_id` (`album_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`status`),
  KEY `idx_reviewed_by` (`reviewed_by`),
  KEY `idx_user_status_time` (`user_id`,`status`,`created_at`),
  CONSTRAINT `fk_photo_album` FOREIGN KEY (`album_id`) REFERENCES `album` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='照片表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `share_link`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `share_link` (
  `id` varchar(36) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '分享链接ID（UUID）',
  `album_id` varchar(36) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '关联相册ID',
  `share_code` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '分享码（8位短码）',
  `password` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '访问密码（加密存储）',
  `expire_at` datetime DEFAULT NULL COMMENT '过期时间（NULL表示永久）',
  `allow_download` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否允许下载（1=允许，0=禁止）',
  `visit_count` int NOT NULL DEFAULT '0' COMMENT '访问次数',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `user_id` varchar(36) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '所属用户ID',
  `allow_upload` tinyint(1) NOT NULL DEFAULT '0' COMMENT 'allow collaborators to upload: 1 yes, 0 read-only',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_share_code` (`share_code`),
  KEY `idx_album_id` (`album_id`),
  KEY `idx_user_id` (`user_id`),
  CONSTRAINT `fk_share_album` FOREIGN KEY (`album_id`) REFERENCES `album` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='分享链接表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tag`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tag` (
  `id` varchar(36) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '标签ID（UUID）',
  `name` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '标签名（首尾空格已去除，不区分大小写唯一）',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '首次被使用时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='标签字典表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tag_relation`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tag_relation` (
  `id` varchar(36) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '关联ID（UUID）',
  `tag_id` varchar(36) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '标签ID',
  `user_id` varchar(36) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '添加人ID',
  `target_type` varchar(10) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '目标类型：ALBUM=相册，PHOTO=照片',
  `target_id` varchar(36) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '目标ID（相册ID或照片ID）',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '添加时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tag_target` (`tag_id`,`target_type`,`target_id`,`user_id`),
  KEY `idx_target` (`target_type`,`target_id`),
  KEY `idx_user_id` (`user_id`),
  CONSTRAINT `fk_tagrel_tag` FOREIGN KEY (`tag_id`) REFERENCES `tag` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_tagrel_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='标签关联表（相册/照片共用）';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `user`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user` (
  `id` varchar(36) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户ID',
  `username` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户名',
  `password` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'BCrypt加密后的密码',
  `nickname` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '昵称',
  `avatar_url` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '头像URL',
  `role` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'USER' COMMENT '角色: USER, ADMIN',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `user_follow`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_follow` (
  `id` varchar(36) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '关注记录ID（UUID）',
  `follower_id` varchar(36) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '发起关注的人',
  `followee_id` varchar(36) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '被关注的人',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '关注时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_follow` (`follower_id`,`followee_id`),
  KEY `idx_followee_time` (`followee_id`,`created_at`),
  KEY `idx_follower_time` (`follower_id`,`created_at`),
  CONSTRAINT `fk_follow_followee` FOREIGN KEY (`followee_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_follow_follower` FOREIGN KEY (`follower_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户关注表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `user_like`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_like` (
  `id` varchar(36) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '点赞记录ID（UUID）',
  `user_id` varchar(36) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '点赞人ID',
  `target_type` varchar(10) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '目标类型：ALBUM=相册，PHOTO=照片',
  `target_id` varchar(36) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '目标ID（相册ID或照片ID）',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '点赞时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_target` (`user_id`,`target_type`,`target_id`),
  KEY `idx_target` (`target_type`,`target_id`),
  KEY `idx_user_type` (`user_id`,`target_type`,`created_at`),
  CONSTRAINT `fk_like_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='点赞表（相册/照片共用）';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping routines for database 'photo_share'
--
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-09-25 18:48:16
