///**
// *
// */
//package com.kuailu.im.core.message;
//
//import com.kuailu.im.core.ImConst;
//import com.kuailu.im.core.config.ImConfig;
//import com.kuailu.im.core.packets.ChatMsgReadConfirmBody;
//
///**
// * @author HP
// *
// */
//public abstract class AbstractMessageHelper implements MessageHelper, ImConst {
//
//	protected ImConfig imConfig;
//
//	public ImConfig getImConfig() {
//		return imConfig;
//	}
//
//	public void setImConfig(ImConfig imConfig) {
//		this.imConfig = imConfig;
//	}
//
//	public abstract void putChatGroupMsgConfirm(String userId, String groupId, ChatMsgReadConfirmBody lastReadMsg);
//
//	public abstract ChatMsgReadConfirmBody getChatGroupMsgConfirm(String userId, String groupId);
//}
