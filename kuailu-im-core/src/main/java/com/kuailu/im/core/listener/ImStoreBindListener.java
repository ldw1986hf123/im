package com.kuailu.im.core.listener;

import com.kuailu.im.core.ImChannelContext;
import com.kuailu.im.core.exception.ImException;
import com.kuailu.im.core.packets.GroupDto;
import com.kuailu.im.core.packets.UserDto;
/**
 * IM持久化绑定用户及群组监听器;
 * @author linjd
 * @date 2018年4月8日 下午4:09:14
 */
public interface ImStoreBindListener {
	/**
	 * 绑定群组后持久化回调该方法
	 * @param imChannelContext 通道上下文
	 * @param group 绑定群组信息
	 * @throws Exception
	 */
	void onAfterGroupBind(ImChannelContext imChannelContext, GroupDto group) throws ImException;

	/**
	 * 解绑群组后持久化回调该方法
	 * @param imChannelContext 通道上下文
	 * @param group 解绑群组信息
	 * @throws Exception
	 */
	void onAfterGroupUnbind(ImChannelContext imChannelContext, GroupDto group) throws ImException;
	/**
	 * 绑定用户后持久化回调该方法
	 * @param imChannelContext 通道上下文
	 * @param user 绑定用户信息
	 * @throws Exception
	 */
	void onAfterUserBind(ImChannelContext imChannelContext, UserDto user) throws ImException;

	/**
	 * 解绑用户后回调该方法
	 * @param imChannelContext 通道上下文
	 * @param user 解绑用户信息
	 * @throws Exception
	 */
	void onAfterUserUnbind(ImChannelContext imChannelContext, UserDto user) throws ImException;
}
