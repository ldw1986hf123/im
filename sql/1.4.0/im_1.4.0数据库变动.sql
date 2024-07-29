DROP INDEX conversation_app_key_inx on im_conversation;
DROP INDEX conversation_conversation_id_inx on im_conversation;
DROP INDEX conversation_chatgroup_id_inx on im_conversation;
DROP INDEX group_unq_idx on im_chat_group;


ALTER TABLE im_conversation ADD COLUMN role_type varchar(32) DEFAULT NULL COMMENT '用户在这个会话中的角色';
ALTER TABLE im_conversation ADD COLUMN self_chat tinyint DEFAULT NULL COMMENT '是否是自己和自己的会话。1 ：是；0:否';
ALTER TABLE im_conversation ADD COLUMN unread_count int DEFAULT 0 COMMENT '未读消息数';
ALTER TABLE im_conversation ADD COLUMN conversation_name varchar(268) DEFAULT NULL COMMENT '会话名称';
ALTER TABLE im_conversation ADD COLUMN group_owner varchar(128) DEFAULT NULL COMMENT '群主';
ALTER TABLE im_conversation ADD COLUMN receiver varchar(128) DEFAULT NULL COMMENT '消息接收人';
ALTER TABLE im_conversation ADD COLUMN user_id varchar(128) DEFAULT NULL COMMENT '会话所属用户';
ALTER TABLE im_conversation ADD COLUMN avatar varchar(2048) DEFAULT NULL COMMENT '消息接收方的头像';
ALTER TABLE im_conversation ADD COLUMN no_disturb tinyint DEFAULT NULL COMMENT '是否开启免打扰';

CREATE UNIQUE INDEX user_group_roleType_unqdex ON im_chat_group_member (user_id,group_id,role_type);
CREATE UNIQUE INDEX user_id_conversation_id_unq_idx ON im_conversation (user_id, conversation_id);


ALTER TABLE im_chat_unread_msg ADD COLUMN created_by varchar(128) DEFAULT NULL COMMENT '';
ALTER TABLE im_chat_unread_msg ADD COLUMN created_time datetime DEFAULT NULL COMMENT '';
ALTER TABLE im_chat_unread_msg ADD COLUMN updated_by varchar(128) DEFAULT NULL COMMENT '';
ALTER TABLE im_chat_unread_msg ADD COLUMN updated_time  datetime DEFAULT NULL COMMENT '';


ALTER TABLE im_user_account ADD COLUMN seid varchar(128) DEFAULT NULL COMMENT '';

CREATE TABLE `im_at_msg`  (
  `id` bigint(0) NOT NULL,
  `msg_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '对应msg表的message_id',
  `at_user` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '被@的用户ID',
  `is_read` tinyint(0) NOT NULL COMMENT '被@的用户是否已读',
  `group_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '群id',
  `conversation_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '会话Id',
  `created_by` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `created_time` datetime(0) NULL DEFAULT NULL,
  `updated_by` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `updated_time` datetime(0) NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

DROP TABLE IF EXISTS im_search_record;
create table im_search_record
(
    id                          bigint(20)      not null primary key,
    user_id                     varchar(128)    not null comment '用户ID',
    search_key                  varchar(128)    not null comment '搜索关键字',
    created_by                  varchar(128)    default null,
    created_time                datetime        NULL DEFAULT CURRENT_TIMESTAMP,
    index user_id_inx (user_id)
)
    ENGINE=InnoDB
COMMENT='用户搜索记录表';

ALTER TABLE im_chat_msg ADD COLUMN msg_content text(16384) DEFAULT NULL COMMENT '';
