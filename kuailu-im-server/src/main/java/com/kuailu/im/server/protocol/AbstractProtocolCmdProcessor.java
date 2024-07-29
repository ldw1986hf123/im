package com.kuailu.im.server.protocol;


import com.kuailu.im.core.ImChannelContext;
import com.kuailu.im.core.packets.Message;
import com.kuailu.im.server.processor.ProtocolCmdProcessor;

/**
 * @author linjd
 * @Desc
 * @date 2020-05-02 16:23
 */
public abstract class AbstractProtocolCmdProcessor implements ProtocolCmdProcessor {

    @Override
    public void process(ImChannelContext imChannelContext, Message message) {

    }
}
