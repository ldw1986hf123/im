/**
 * 
 */
package com.kuailu.im.core.packets;

import com.kuailu.im.core.ImStatus;
import com.kuailu.im.core.Status;

/**
 * 版本: [1.0]
 * 功能说明: 加入群组响应消息体
 * 作者: WChao 创建时间: 2017年7月26日 下午5:09:20
 */
public class JoinGroupRespBody extends RespBody {
	
	private static final long serialVersionUID = 6635620192752369689L;
	public JoinGroupResult result;
	public String group;

	private String conversationId;
	private int chatType;


	public int getChatType() {
		return chatType;
	}

	public void setChatType(int chatType) {
		this.chatType = chatType;
	}


	public String getConversationId() {
		return conversationId;
	}

	public void setConversationId(String conversationId) {
		this.conversationId = conversationId;
	}


	public JoinGroupRespBody(){
		this(Command.COMMAND_JOIN_GROUP_RESP, null);
	}

	public JoinGroupRespBody(Integer code, String msg){
		super(code, msg);
		this.cmd = Command.COMMAND_JOIN_GROUP_RESP;
	}

	public JoinGroupRespBody(Status status){
		this(Command.COMMAND_JOIN_GROUP_RESP, status);
	}

	public JoinGroupRespBody(Command command , Status status){
		super(command, status);
	}

	public JoinGroupResult getResult() {
		return result;
	}

	public JoinGroupRespBody setResult(JoinGroupResult result) {
		this.result = result;
		return this;
	}

	public String getGroup() {
		return group;
	}

	public JoinGroupRespBody setGroup(String group) {
		this.group = group;
		return this;
	}

	public JoinGroupRespBody setData(Object data){
		super.setData(data);
		return this;
	}

	public static JoinGroupRespBody success(){
		JoinGroupRespBody joinGroupRespBody = new JoinGroupRespBody(ImStatus.C10011);
		joinGroupRespBody.setResult(JoinGroupResult.JOIN_GROUP_RESULT_OK);
		return joinGroupRespBody;
	}

	public static JoinGroupRespBody failed(){
		JoinGroupRespBody joinGroupRespBody = new JoinGroupRespBody(ImStatus.C10012);
		joinGroupRespBody.setResult(JoinGroupResult.JOIN_GROUP_RESULT_UNKNOWN);
		return joinGroupRespBody;
	}

}
