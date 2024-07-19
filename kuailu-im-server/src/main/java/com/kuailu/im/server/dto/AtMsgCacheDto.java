/**
 * 
 */
package com.kuailu.im.server.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.kuailu.im.server.enums.YesOrNoEnum;
import com.kuailu.im.server.model.entity.ChatMsg;
import lombok.Data;


@Data
public class AtMsgCacheDto extends  BaseDto{
	/**
	 * 消息ID
	 */
	private String msgId;

	private String atUser;


	private String conversationId;

	private String groupId;
	/**
	 * 0:未读，1:已读
	 */
	private Integer isRead;
}
