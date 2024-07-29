/**
 * 
 */
package com.kuailu.im.server.response;

import com.kuailu.im.core.packets.Message;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 功能说明: 
 */
@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class ConversationListDto extends Message implements Serializable{
	
	private static final long serialVersionUID = 1L;

	private String userId;

	private String userNo;

	private String userName;
	/**
	 * 群组头像
	 */
	private String avatar;

	/**
	 * 用户所属终端;(ws、tcp、http、android、ios等)
	 */
	private String terminal;

	/**
	 * 最后一条消息
	 */
	private List<ConversationDto> conversationDtos;
}
