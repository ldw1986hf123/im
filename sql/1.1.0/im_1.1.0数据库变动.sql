/*
 Navicat Premium Data Transfer

 Source Server         : kuailu_dev
 Source Server Type    : MySQL
 Source Server Version : 80022
 Source Host           : 192.168.101.67:3306
 Source Schema         : imserver

 Target Server Type    : MySQL
 Target Server Version : 80022
 File Encoding         : 65001

 Date: 24/03/2023 15:55:46
*/

-- ----------------------------
-- Table structure for im_file
-- ----------------------------
DROP TABLE IF EXISTS `im_file`;
CREATE TABLE `im_file`  (
  `id` bigint(0) NOT NULL,
  `content` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '访问连接',
  `pdg_thumb_viewer` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '预览图连接',
  `file_size` bigint(0) NULL DEFAULT NULL COMMENT '文件大小，字节为单位',
  `suffix` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '后缀名',
  `duration` bigint(0) NULL DEFAULT NULL COMMENT '持续时长，毫秒为单位',
  `full_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '文件全名',
  `receiver` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '文件接收人',
  `status` tinyint(0) NULL DEFAULT NULL COMMENT '1:上传成功；0 上传失败',
  `created_by` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '上传人id',
  `created_time` datetime(0) NOT NULL COMMENT '上传时间',
  `updated_by` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
  `updated_time` datetime(0) NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin ROW_FORMAT = Dynamic;

ALTER TABLE im_chat_msg ADD COLUMN is_read TINYINT DEFAULT NULL COMMENT '是否已读。1：已读；0未读';

DROP INDEX chat_unread_msg_msg_id_user_name_inx on im_chat_unread_msg;

DROP INDEX member_group_user_id_inx on im_chat_group_member;
