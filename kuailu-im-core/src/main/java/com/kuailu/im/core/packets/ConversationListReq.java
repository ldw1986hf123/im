
package com.kuailu.im.core.packets;

import lombok.Data;

/**
会话列表入参
 */
@Data
public class ConversationListReq extends Message{
	
	private static final long serialVersionUID = 1861307516710578262L;

	/**
	 * 群组id;
	 * 现在前端并没有传这个参数
	 */
	private String groupId;

	/**
	 * 用户seid
	 */
	private String seid;
}
