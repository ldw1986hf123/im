package com.kuailu.im.server;


import com.kuailu.im.core.ImChannelContext;
import com.kuailu.im.core.config.ImConfig;
import com.kuailu.im.server.protocol.AbstractProtocolHandler;
import org.tio.core.ChannelContext;
import org.tio.utils.thread.pool.AbstractQueueRunnable;

/**
 **/
public class ImServerChannelContext extends ImChannelContext {

    protected AbstractQueueRunnable msgQue;

    protected AbstractProtocolHandler protocolHandler;

    public ImServerChannelContext(ImConfig imConfig, ChannelContext tioChannelContext) {
        super(imConfig, tioChannelContext);
    }

    public AbstractQueueRunnable getMsgQue() {
        return msgQue;
    }

    public void setMsgQue(AbstractQueueRunnable msgQue) {
        this.msgQue = msgQue;
    }

    public AbstractProtocolHandler getProtocolHandler() {
        return protocolHandler;
    }

    public void setProtocolHandler(AbstractProtocolHandler protocolHandler) {
        this.protocolHandler = protocolHandler;
    }

}
