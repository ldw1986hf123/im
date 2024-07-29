/**
 * 
 */
package com.kuailu.im.core.packets;

/**
 * 版本: [1.0]
 * 功能说明: 退出群组通知消息体
 * 作者: WChao 创建时间: 2017年7月26日 下午5:15:18
 */
public class ExitGroupNotifyRespBody extends Message{
	
	private static final long serialVersionUID = 3680734574052114902L;
	private UserDto user;
	private String group;
	
	public UserDto getUser() {
		return user;
	}
	public ExitGroupNotifyRespBody setUser(UserDto user) {
		this.user = user;
		return this;
	}
	public String getGroup() {
		return group;
	}
	public ExitGroupNotifyRespBody setGroup(String group) {
		this.group = group;
		return this;
	}
}
