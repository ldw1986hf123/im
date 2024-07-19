/**
 * 
 */
package com.kuailu.im.server.util;

import com.kuailu.im.core.ImChannelContext;
import com.kuailu.im.core.ImConst;
import com.kuailu.im.core.ImSessionContext;
import com.kuailu.im.core.config.ImConfig;
import com.kuailu.im.server.req.ChatReqParam;
import com.kuailu.im.core.packets.UserDto;
import com.kuailu.im.core.session.id.impl.UUIDSessionIdGenerator;
import com.kuailu.im.core.utils.JsonKit;
import com.kuailu.im.server.config.ImServerConfig;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

/**
 * IM聊天命令工具类
 * @date 2022-09-05 23:29:30
 * @author linjd
 *
 */
public class ChatKit {
	
	private static Logger log = Logger.getLogger(ChatKit.class);

	/**
	 * 转换为聊天消息结构;
	 * @param body
	 * @param imChannelContext
	 * @return
	 */
	public static ChatReqParam toChatBody(byte[] body, ImChannelContext imChannelContext){
		ChatReqParam chatReqBody = parseChatBody(body);
		/*if(chatReqBody != null){
			if(StringUtils.isEmpty(chatReqBody.getSender())){
				ImSessionContext imSessionContext = imChannelContext.getSessionContext();
				UserDto user = imSessionContext.getImClientNode().getUser();
				if(user != null){
					chatReqBody.setSender(user.getUserName());
				}else{
					chatReqBody.setSender(imChannelContext.getId());
				}
			}
		}*/
		return chatReqBody;
	}

	/**
	 * 判断是否属于指定格式聊天消息;
	 * @param body
	 * @return
	 */
	private static ChatReqParam parseChatBody(byte[] body){
		if(body == null) {
			return null;
		}
		ChatReqParam chatReqBody = null;
		try{
			String text = new String(body, ImConst.CHARSET);
		    chatReqBody = JsonKit.toBean(text, ChatReqParam.class);
			if(chatReqBody != null){
				if(chatReqBody.getCreatedTime() == null) {
					chatReqBody.setCreatedTime(System.currentTimeMillis());
				}
				if(StringUtils.isEmpty(chatReqBody.getId())){
					chatReqBody.setId(UUIDSessionIdGenerator.instance.sessionId(null));
				}
				return chatReqBody;
			}
		}catch(Exception e){
			log.error(e.toString());
		}
		return chatReqBody;
	}

	/**
	 * 判断是否属于指定格式聊天消息;
	 * @param bodyStr
	 * @return
	 */
	public static ChatReqParam parseChatBody(String bodyStr){
		if(bodyStr == null) {
			return null;
		}
		try {
			return parseChatBody(bodyStr.getBytes(ImConst.CHARSET));
		} catch (Exception e) {
			log.error(e);
		}
		return null;
	}

     /**
      * 判断用户是否在线
      * @param userId 用户ID
      * @return
      */
     public static boolean isOnline(String userId ){
//		 if(isStore){
			ImServerConfig imServerConfig = ImConfig.Global.get();
			return imServerConfig.getMessageHelper().isOnline(userId);
//		 }
//    	 List<ImChannelContext> imChannelContexts = JimServerAPI.getByUserId(userId);
//    	 if(CollectionUtils.isNotEmpty(imChannelContexts)){
//    		 return true;
//    	 }
//    	 return false;
     }

	/**
	 * 获取双方会话ID
	 *
	 * @param from
	 * @param to
	 * @return
	 */
	public static String sessionId(String from, String to) {
		if (from.compareTo(to) <= 0) {
			return from + "-" + to;
		} else {
			return to + "-" + from;
		}
	}
}
