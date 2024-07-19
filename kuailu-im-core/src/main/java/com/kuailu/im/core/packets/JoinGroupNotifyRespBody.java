/**
 * 
 */
package com.kuailu.im.core.packets;

import com.kuailu.im.core.ImStatus;
import com.kuailu.im.core.Status;

/**
 * 版本: [1.0]
 * 功能说明: 进入群组通知消息体
 * 作者: WChao 创建时间: 2017年7月26日 下午5:14:04
 */
public class JoinGroupNotifyRespBody extends RespBody{
	
	private static final long serialVersionUID = 3828976681110713803L;
	private UserDto user;
	private String group;

	private String groupName;
	/** 群聊类型 */
	private int  chatType;

	private Long createTime;

	public Long getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Long createTime) {
		this.createTime = createTime;
	}


	public int getChatType() {
		return chatType;
	}

	public void setChatType(int chatType) {
		this.chatType = chatType;
	}

	public String getGroupOwner() {
		return groupOwner;
	}

	public void setGroupOwner(String groupOwner) {
		this.groupOwner = groupOwner;
	}

	private  String groupOwner;
	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}


	public JoinGroupNotifyRespBody(){
		super(Command.COMMAND_JOIN_GROUP_NOTIFY_RESP, null);
	}

	public JoinGroupNotifyRespBody(Integer code, String msg){
		super(Command.COMMAND_JOIN_GROUP_NOTIFY_RESP, null);
		this.code = code;
		this.msg = msg;
	}

	public JoinGroupNotifyRespBody(Status status){
		super(Command.COMMAND_JOIN_GROUP_NOTIFY_RESP,status);
	}

	public JoinGroupNotifyRespBody(Command command, Status status){
		super(command,status);
	}
	public UserDto getUser() {
		return user;
	}

	public JoinGroupNotifyRespBody setUser(UserDto user) {
		this.user = user;
		return this;
	}
	public String getGroup() {
		return group;
	}

	public JoinGroupNotifyRespBody setGroup(String group) {
		this.group = group;
		return this;
	}

	public static JoinGroupNotifyRespBody success(){
		return new JoinGroupNotifyRespBody(ImStatus.C10011);
	}

	public static JoinGroupNotifyRespBody failed(){
		return new JoinGroupNotifyRespBody(ImStatus.C10012);
	}

}
