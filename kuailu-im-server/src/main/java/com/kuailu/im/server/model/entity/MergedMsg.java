package com.kuailu.im.server.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;


@Data
@Accessors(chain = true)
@TableName("im_merged_msg")
public class MergedMsg    {
    private String id;

    /**
     *  `id` bigint NOT NULL DEFAULT '1',
     *   `message_id` varchar(64) COLLATE utf8mb4_bin NOT NULL COMMENT '对应chat_msg表的message_id',
     *   `merged_message_id` text COLLATE utf8mb4_bin NOT NULL COMMENT '所有被合并的消息的id,逗号隔开',
     *   `level` int NOT NULL DEFAULT '1' COMMENT '被合并的层次',
     *   `created_time` datetime DEFAULT NULL,
     *   `created_by` varchar(255) COLLATE utf8mb4_bin DEFAULT NULL,
     *   `updated_by` varchar(128) COLLATE utf8mb4_bin DEFAULT NULL,
     *   `updated_time` datetime DEFAULT NULL,
     */
    /**
     * 消息ID
     */
    private String messageId;

    private String entityId;

    private String mergedMessageId;

    private Integer level;
    private String title;

    private Integer msgType;
    private Integer chatType;

    private String createdBy="";

    private Date createdTime;

    private String updatedBy="";
}
