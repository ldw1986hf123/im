package com.kuailu.im.core.message;

import com.kuailu.im.core.listener.ImStoreBindListener;
import com.kuailu.im.core.packets.*;

import java.util.List;
/**
 */
public interface MessageHelper {
	/**
	 * 获取IM开启持久化时绑定/解绑群组、用户监听器;
	 * @return
	 */
	 ImStoreBindListener getBindListener();
	/**
	 * 判断用户是否在线
	 * @param userId 用户ID
	 * @return
	 */
	 boolean isOnline(String userId);

	/**
	 * 消息统计消息持久化写入，暂时存放最后一条已读消息
	 * @param userId
	 * @param groupId
	 * @param msgId
	 */
//	 void putChatGroupMsgConfirm(String userId, String groupId, ChatMsgReadConfirmBody lastReadMsgId);

//	ChatMsgReadConfirmBody getChatGroupMsgConfirm(String userId,String  groupId);

}
