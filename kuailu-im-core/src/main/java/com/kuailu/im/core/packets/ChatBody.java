/**
 * 
 */
package com.kuailu.im.core.packets;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * 压入消息队列的就是这个类message
 */
@Builder
@Data
@Deprecated
@NoArgsConstructor
@AllArgsConstructor
public class ChatBody extends Message {
	
	private static final long serialVersionUID = 5731474214655476286L;
	/**
	 * 发送用户id;
	 */
	private String sender;

	private String senderName;
	/**
	 * 目标用户id;
	 */
	private String receiver;
	/**
	 * 消息类型;(如：0:文本、1:图片、2:语音、3:视频、4:音乐、5:图文)
	 */
	private Integer msgType;
	/**
	 * 聊天类型;(如公聊、私聊)
	 */
	private Integer chatType;
	/**
	 * 消息内容;
	 */
	private String content;
	/**
	 * 消息发到哪个群组;
	 */
	private String groupId;

	private Long timestamp;
	//	pending: 0, // 发送未开始
	//	delivering: 1, // 正在发送
	//	succeed: 2, // 发送成功
	//	failed: 3, // 发送失败
	private Integer status = ChatMessageSendStatus.SUCCEED.getValue();

}
