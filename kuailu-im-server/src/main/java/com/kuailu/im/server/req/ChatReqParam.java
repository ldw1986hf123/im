/**
 * 
 */
package com.kuailu.im.server.req;

import cn.hutool.json.JSONUtil;
import com.kuailu.im.core.packets.ChatType;
import com.kuailu.im.core.packets.Command;
import com.kuailu.im.core.packets.Message;
import com.kuailu.im.server.constant.IM_SERVER;
import com.kuailu.im.server.enums.MessageTypeEnum;
import com.kuailu.im.server.enums.YesOrNoEnum;
import com.kuailu.im.server.model.entity.ChatMsg;
import com.kuailu.im.server.util.UUIDUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;



@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatReqParam extends Message {
	
	private static final long serialVersionUID = 5731474214655476286L;
	/**
	 * todo 发送用户id  不需要前端传;
	 */
	private String sender;
	private String seid;

	//todo 不需要前端传
	private String senderName;

	private String mergedUserName;

	/**
	 * 目标用户id;
	 */
	private String receiver;
	/**
	 * msgType：0:文本、1:文件、2:语音、3:视频、 4：合并转发消息、5、图片
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

//	private String mergedEntityId;

	private String mergedMessageId;

	//合并转发，还是转发合并
	private String operaType;

	//消息是显示左边还说右边
	private String showSide;

	private String conversationId;


	public ChatReqParam(FileHelperBuilder builder) {
		this.id = builder.messageId;
		this.chatType = ChatType.FILE_HELPER.getNumber();
		this.sender = builder.sender;
		this.senderName = builder.senderName;
		this.messageBody = builder.messageBody;
		this.conversationId = builder.conversationId;
		this.receiver = builder.receiver;
		this.setCmd(Command.COMMAND_CHAT_REQ_2.getNumber());
		this.msgType = builder.msgType;
		this.groupId = builder.groupId;
		this.setCreatedTime(new Date().getTime());
	}


	public static class FileHelperBuilder {
		private String messageId;
		private String conversationId;
		private String sender;
		private String senderName;
		private String receiver;
		private MessageBody messageBody;
		private String groupId;
		private Integer msgType;
		public FileHelperBuilder(String messageId,String sender, String senderName,  String conversationId, String messageBody,  Integer msgType, String groupId ) {
			this.messageId=messageId;
			this.conversationId = conversationId;
			this.sender = sender;
			this.senderName = senderName;
			this.messageBody =JSONUtil.toBean(messageBody, MessageBody.class);
			this.groupId = groupId;
			this.msgType=msgType;
		}

		public ChatReqParam build() {
			return new ChatReqParam(this);
		}

	}



}
