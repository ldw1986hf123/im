package com.kuailu.im.server.helper.redis;

import com.kuailu.im.core.ImChannelContext;
import com.kuailu.im.core.ImSessionContext;
import com.kuailu.im.core.config.ImConfig;
import com.kuailu.im.core.exception.ImException;
import com.kuailu.im.core.listener.AbstractImStoreBindListener;
import com.kuailu.im.core.message.MessageHelper;
import com.kuailu.im.core.packets.GroupDto;
import com.kuailu.im.core.packets.UserDto;
import com.kuailu.im.core.packets.UserStatusType;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;

/**
 * 消息持久化绑定监听器
 * @author linjd
 * @date 2018年4月8日 下午4:12:31
 */
public class RedisImStoreBindListener extends AbstractImStoreBindListener {

	private static Logger logger = LoggerFactory.getLogger(RedisImStoreBindListener.class);

	private static final String SUFFIX = ":";

	public RedisImStoreBindListener(ImConfig imConfig, MessageHelper messageHelper){
		super(imConfig, messageHelper);
	}

	@Override
	public void onAfterGroupBind(ImChannelContext imChannelContext, GroupDto group) throws ImException {

		initGroupUsers(group, imChannelContext);
	}

	@Override
	public void onAfterGroupUnbind(ImChannelContext imChannelContext, GroupDto group) throws ImException {
		logger.debug(
				"User unbind Group User:{},node:{},group:{}",
				imChannelContext.getUserId(),
				imChannelContext.getClientNode().toString(),
				group.getGroupId());
		String userId = imChannelContext.getUserId();
		String groupId = group.getGroupId();
		//移除群组成员;
//		RedisCacheManager.getCache(GROUP).listRemove(groupId+SUFFIX+USER, userId);
//		//移除成员群组;
//		RedisCacheManager.getCache(USER).listRemove(userId+SUFFIX+GROUP, groupId);
//		//移除群组离线消息
//		RedisCacheManager.getCache(PUSH).remove(GROUP+SUFFIX+group+SUFFIX+userId);
	}

	@Override
	public void onAfterUserBind(ImChannelContext imChannelContext, UserDto user){
		if( Objects.isNull(user)) {
			return;
		}
		user.setStatus(UserStatusType.ONLINE.getStatus());
//		this.messageHelper.updateUserTerminal(user);
//		initUserInfo(user);
	}

	@Override
	public void onAfterUserUnbind(ImChannelContext imChannelContext, UserDto user) throws ImException {
		logger.debug(
				"用户退出终端：用户：{} node:{}",
				imChannelContext.getUserId(),
				imChannelContext.getClientNode().toString());
//		if(!isStore() || Objects.isNull(user)) {
//			return;
//		}
		user.setStatus(UserStatusType.OFFLINE.getStatus());
//		this.messageHelper.updateUserTerminal(user);
	}

	/**
	 * 初始化群组用户;
	 * @param group
	 * @param imChannelContext
	 */
	public void initGroupUsers(GroupDto group , ImChannelContext imChannelContext){
		String groupId = group.getGroupId();
		String userId = imChannelContext.getUserId();
		if(StringUtils.isEmpty(groupId) || StringUtils.isEmpty(userId)) {
			return;
		}
		//将用户ID加入到群组的缓存中
/*		String group_user_key = groupId+SUFFIX+USER;
		RedisCache groupCache = RedisCacheManager.getCache(GROUP);*/
	/*	List<String> users = groupCache.listGetAll(group_user_key);
		if(!users.contains(userId)){
			groupCache.listPushTail(group_user_key, userId);
		}*/
		// 将群组ID加入该用户的群组缓存
//		initUserGroups(userId, groupId);
		//更新redis 当前用户的群聊组的详细信息
		ImSessionContext imSessionContext = imChannelContext.getSessionContext();
		UserDto onlineUser = imSessionContext.getImClientNode().getUser();
		if(onlineUser == null) {
			return;
		}
		List<GroupDto> groups = onlineUser.getGroups();
		if(groups == null) {
			return;
		}
		/*for(GroupDto storeGroup : groups){
			if(!groupId.equals(storeGroup.getGroupId()))continue;
			groupCache.put(groupId+SUFFIX+INFO, storeGroup);
			break;
		}*/
	}

	/**
	 * 初始化用户拥有哪些群组;
	 * @param userId
	 * @param group
	 */
	public void initUserGroups(String userId, String group){
		if(StringUtils.isEmpty(group) || StringUtils.isEmpty(userId)) {
			return;
		}
	/*	List<String> groups = RedisCacheManager.getCache(USER).listGetAll(userId+SUFFIX+GROUP);
		if(groups.contains(group))return;*/
//		RedisCacheManager.getCache(USER).listPushTail(userId+SUFFIX+GROUP, group);
	}

	/**
	 * 初始化用户终端协议类型;
	 * @param user
	 */
/*	public void initUserInfo(UserDto user){
		if( user == null) {
			return;
		}
		String userId = user.getUserId();
		if(StringUtils.isEmpty(userId)) {
			return;
		}
		RedisCache userCache = RedisCacheManager.getCache(USER);
		userCache.put(userId+SUFFIX+INFO, user.clone());
	}*/



/*	static{
		RedisCacheManager.register(USER, Integer.MAX_VALUE, Integer.MAX_VALUE);
		RedisCacheManager.register(GROUP, Integer.MAX_VALUE, Integer.MAX_VALUE);
		RedisCacheManager.register(STORE, Integer.MAX_VALUE, Integer.MAX_VALUE);
		RedisCacheManager.register(PUSH, Integer.MAX_VALUE, Integer.MAX_VALUE);
	}*/

}
