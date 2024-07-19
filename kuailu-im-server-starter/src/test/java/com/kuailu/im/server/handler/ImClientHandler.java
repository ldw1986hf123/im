package com.kuailu.im.server.handler;

import com.kuailu.im.core.ImChannelContext;
import com.kuailu.im.core.ImHandler;
import com.kuailu.im.core.ImPacket;

/**
 *
 * 客户端回调
 * @author WChao 
 *
 */
public interface ImClientHandler extends ImHandler {
    /**
     * 心跳包接口
     * @param imChannelContext
     * @return
     */
    ImPacket heartbeatPacket(ImChannelContext imChannelContext);
}
