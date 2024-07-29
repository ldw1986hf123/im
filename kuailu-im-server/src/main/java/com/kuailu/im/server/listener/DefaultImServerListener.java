package com.kuailu.im.server.listener;


import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.kuailu.im.core.ImChannelContext;
import com.kuailu.im.core.ImPacket;
import lombok.extern.slf4j.Slf4j;

/**

 **/
@Slf4j
public class DefaultImServerListener implements ImServerListener {


    @Override
    public boolean onHeartbeatTimeout(ImChannelContext channelContext, Long interval, int heartbeatTimeoutCount) {
//        log.info("心跳超时。channelContext{}", JSON.toJSONString(channelContext));
        return false;
    }

    @Override
    public void onAfterConnected(ImChannelContext channelContext, boolean isConnected, boolean isReconnect) throws Exception {
//        log.info("连接建立。channelContext{}", channelContext);
    }

    @Override
    public void onAfterDecoded(ImChannelContext channelContext, ImPacket packet, int packetSize) throws Exception {

//        log.info("onAfterDecoded。channelContext{}", channelContext);
    }

    @Override
    public void onAfterReceivedBytes(ImChannelContext channelContext, int receivedBytes) throws Exception {

//        log.info("onAfterReceivedBytes。channelContext{}", channelContext);
    }

    @Override
    public void onAfterSent(ImChannelContext channelContext, ImPacket packet, boolean isSentSuccess) throws Exception {

//        log.info("onAfterSent。channelContext{}", channelContext);
    }

    @Override
    public void onAfterHandled(ImChannelContext channelContext, ImPacket packet, long cost) throws Exception {

//        log.info("onAfterHandled。channelContext{}", channelContext);
    }

    @Override
    public void onBeforeClose(ImChannelContext channelContext, Throwable throwable, String remark, boolean isRemove) throws Exception {

//        log.info("onBeforeClose。channelContext{}", channelContext);
    }
}
