ALTER TABLE im_conversation ADD COLUMN last_msg_id varchar(128) DEFAULT NULL COMMENT '最新一条消息的id';


DROP TABLE IF EXISTS im_no_disturb;
create table im_no_disturb
(
    id                          bigint(20)      not null primary key,
    user_id                     varchar(128)    not null comment '用户ID',
    conversation_id             varchar(128)    not null comment '对话ID',
    created_by                  varchar(128)    default null,
    created_time                datetime        NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by                  varchar(128)    default null,
    updated_time                datetime        NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    unique index conversation_id_inx (user_id, conversation_id)
)
    ENGINE=InnoDB
COMMENT='免打扰表';

DROP TABLE IF EXISTS im_white_list;
create table im_white_list
(
    id                          bigint(20)      not null primary key,
    user_id                     varchar(128)    not null comment '用户ID',
    created_by                  varchar(128)    default null,
    created_time                datetime        NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by                  varchar(128)    default null,
    updated_time                datetime        NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    unique index conversation_id_inx (user_id)
)
    ENGINE=InnoDB
COMMENT='白名单表';


DROP TABLE IF EXISTS im_white_list_member;
create table im_white_list_member
(
    id                          bigint(20)      not null primary key,
    m_user_id                     varchar(128)    not null comment '白名单ID',
    user_id                     varchar(128)    not null comment '用户ID',
    created_by                  varchar(128)    default null,
    created_time                datetime        NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by                  varchar(128)    default null,
    updated_time                datetime        NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    unique index conversation_id_inx (m_user_id, user_id)
)
    ENGINE=InnoDB
COMMENT='白名单成员表';
