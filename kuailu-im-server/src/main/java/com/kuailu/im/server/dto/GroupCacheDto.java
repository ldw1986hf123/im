/**
 * 
 */
package com.kuailu.im.server.dto;

import cn.hutool.core.bean.BeanUtil;
import com.kuailu.im.core.packets.ChatBody;
import com.kuailu.im.core.packets.Message;
import com.kuailu.im.core.packets.UserDto;
import com.kuailu.im.server.model.entity.ChatGroupMember;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
public class GroupCacheDto  implements Serializable {
	
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

	private Integer chatType;

	private String groupOwner;

	//私聊未读数
	private Integer privateChatUnReadCount;

	//群聊未读数
	private Integer publicChatUnReadCount;

	//私聊的对话人
	private ChatGroupMember privateChatMember;
	private Date createdTime;

}
