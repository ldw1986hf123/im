/**
 * 
 */
package com.kuailu.im.core.packets;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
已废弃

 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GroupDto extends Message{
	
	private static final long serialVersionUID = -3817755433171220952L;
	/**
	 * 群组ID
	 */
	private String groupId;
	/**
	 * 群组名称
	 */
	private String groupName;
	/**
	 * 群组头像
	 */
	private String avatar;
	/**
	 * 在线人数
	 */
	private Integer online;
	/**
	 * 用户创建人
	 */
	private  String groupOwner;
	/**
	 * 组用户
	 */
	private List<UserDto> users;

	/** 群聊类型 */
	private int  chatType;
	/**
	 * 未读消息数
	 */
	private Integer unReaderMsgCount;
	/**
	 * 最后一次时间
	 */
	private Long readMsgTime;
	/**
	 * 最后一条消息
	 */
	private ChatBody lastMsg;
	/**
	 * 最新一条已读消息;
	 */
	private String lastReadMsgId;

	private Long createTime;

	/*public GroupDto clone(){
		GroupDto group = GroupDto.builder().build();
		BeanUtil.copyProperties(this, group,"users");
		return group;
	}
*/
}
