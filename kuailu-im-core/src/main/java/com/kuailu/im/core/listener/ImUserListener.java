package com.kuailu.im.core.listener;

import com.kuailu.im.core.ImChannelContext;
import com.kuailu.im.core.exception.ImException;
import com.kuailu.im.core.packets.UserDto;

/**
 * @ClassName ImUserListener
 * @Description 绑定/解绑用户监听器
 * @author linjd
 * @Date 2020/1/12 14:24
 * @Version 1.0
 **/
public interface ImUserListener {
    /**
     * 绑定用户后回调该方法
     * @param imChannelContext IM通道上下文
     * @param user 绑定用户信息
     * @throws Exception
     * @author linjd
     */
    void onAfterBind(ImChannelContext imChannelContext, UserDto user) throws ImException;

    /**
     * 解绑用户后回调该方法
     * @param imChannelContext IM通道上下文
     * @param user 解绑用户信息
     * @throws Exception
     * @author linjd
     */
    void onAfterUnbind(ImChannelContext imChannelContext, UserDto user) throws ImException;
}
