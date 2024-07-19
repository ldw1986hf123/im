/**
 * 
 */
package com.kuailu.im.server.response;

import com.kuailu.im.core.packets.Message;
import com.kuailu.im.server.req.MessageBody;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


/**
 * 压入消息队列的就是这个类message
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatReqPushResponse extends Message {
	
	private static final long serialVersionUID = 5731474214655476286L;
	/**
	 * todo 发送用户id  不需要前端传;
	 */
	private String sender;

	//todo 不需要前端传
	private String senderName;
	/**
	 * 目标用户id;
	 */
	private String receiver;
	/**
	 * 消息类型;(如：0:文本、1:图片、2:语音、3:视频、4:音乐、5:图文、6：合并转发消息)
	 */
	private Integer msgType;
	/**
	 * 聊天类型;(如公聊、私聊)
	 */
	private Integer chatType;
	/**
	 * 消息内容;
	 */
	private MessageBody messageBody;
	/**
	 * 消息发到哪个群组;
	 */
	private String groupId;

	private Long timestamp;

	private Integer status ;

	/**
	 * 合并消息的时候，才用到这个属性
	 */
	private List<String> mergedMessageIdList;

}
