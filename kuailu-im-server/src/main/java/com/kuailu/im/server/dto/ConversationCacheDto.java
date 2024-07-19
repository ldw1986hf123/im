/**
 * 
 */
package com.kuailu.im.server.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.kuailu.im.server.model.entity.ChatMsg;
import lombok.Data;


@Data
public class ConversationCacheDto {
	private String receiver;

	private String userNo;

	private String userId;

	private String conversationName;
	/**
	 * 群组ID  马上就要去掉
	 */
	private String groupId;
	private String groupOwner;

	private String conversationId;

	@JsonInclude
	private String avatar;

	/** 群聊类型 */
	private Integer  chatType;
	/**
	 * 未读消息数
	 */
	private Integer unReaderMsgCount;

	/**
	 * 最后一条消息
	 */
	private ChatMsg lastMsg ;
//	private String lastMsgId;

	private Long createdTime;

	/** 员工状况 1-在线,2-离线,3-请假,4-外出,5-出差 */
	private Integer staffStatus;
	private String staffStatusName;
	private String statusDesc;
	//0-默认  1-免打扰
	private Integer disturbType;

	private Boolean isSelfChat;


	private String lastAtAvatar;
}
