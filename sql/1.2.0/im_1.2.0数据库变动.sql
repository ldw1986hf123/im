 -- ----------------------------
 -- Table im_user_account  新增员工状态
 -- ----------------------------
ALTER TABLE im_user_account ADD COLUMN staff_status TINYINT DEFAULT NULL COMMENT '员工状况 1-在线,2-离线,3-请假,4-外出,5-出差';
ALTER TABLE im_user_account ADD COLUMN last_login_time DATETIME DEFAULT NULL COMMENT '最后登录时间';
ALTER TABLE im_user_account ADD COLUMN last_logout_time DATETIME DEFAULT NULL COMMENT '最后登出时间';

ALTER TABLE im_conversation ADD COLUMN sender varchar(128) DEFAULT NULL COMMENT '发送者';
ALTER TABLE im_conversation ADD COLUMN receiver varchar(128) DEFAULT NULL COMMENT '接收者';

ALTER TABLE im_file ADD COLUMN parent_file varchar(128) DEFAULT NULL COMMENT '主文件';
ALTER TABLE im_file ADD COLUMN sender varchar(128) DEFAULT NULL COMMENT '发送者';
ALTER TABLE im_file ADD COLUMN orientation TINYINT DEFAULT NULL COMMENT '0竖图 1横图';


ALTER TABLE im_chat_msg ADD COLUMN opera_type varchar(128) DEFAULT NULL COMMENT '发消息的操作类型。转发合并还是合并转发';
ALTER TABLE im_chat_msg ADD COLUMN show_side varchar(64) DEFAULT NULL COMMENT '消息显示再左边还说右边';

-- ----------------------------
-- Table structure for im_merged_msg
-- ----------------------------
DROP TABLE IF EXISTS `im_merged_msg`;
CREATE TABLE `im_merged_msg`  (
  `id` varchar(124) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL DEFAULT '1',
  `message_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '对应chat_msg表的message_id',
  `entity_id` varchar(124) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT 'msgType为6时才有值，对应已经存在的实体id',
  `merged_message_id` text CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL COMMENT '所有被合并的消息的id,逗号隔开',
  `level` int(0) NULL DEFAULT 1 COMMENT '被合并的层次',
  `created_time` datetime(0) NULL DEFAULT NULL,
  `msg_type` tinyint(0) NULL DEFAULT NULL,
  `chat_type` tinyint(0) NULL DEFAULT NULL,
  `title` varchar(10240) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
  `created_by` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
  `updated_by` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
  `updated_time` datetime(0) NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin ROW_FORMAT = Dynamic;

