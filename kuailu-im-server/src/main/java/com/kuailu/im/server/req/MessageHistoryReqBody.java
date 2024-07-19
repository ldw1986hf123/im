package com.kuailu.im.server.req;

import com.kuailu.im.core.packets.Message;
import lombok.Data;

/**
 */
@Data
public class MessageHistoryReqBody extends Message {

	private static final long serialVersionUID = -4748178964168947701L;
	/**
	 * 接收用户id;
	 */
	private String receiver;
	/**
	 * 群组id;
	 */
	private String groupId;
	/**
	 * 消息结束时间
	 */
	private Long endTime;
	/**
	 * 数量
	 */
	private Integer count;
	/**
	 *  '0:单聊, 1:是群聊',
	 */
//	private Integer chatType;

}
