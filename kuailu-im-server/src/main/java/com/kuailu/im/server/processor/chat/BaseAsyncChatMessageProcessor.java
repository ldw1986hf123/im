package com.kuailu.im.server.processor.chat;

import com.kuailu.im.core.ImChannelContext;
import com.kuailu.im.core.config.ImConfig;
import com.kuailu.im.core.message.MessageHelper;
import com.kuailu.im.core.packets.ChatBody;
import com.kuailu.im.core.packets.ChatType;
import com.kuailu.im.core.packets.Message;
import com.kuailu.im.server.config.ImServerConfig;
import com.kuailu.im.server.service.IMergedMsgService;
import com.kuailu.im.server.util.ChatKit;

import com.kuailu.im.server.processor.SingleProtocolCmdProcessor;

import java.util.List;

/**
 * @author 林坚丁
 * @date 2022年5月3日 下午1:13:32
 */
public abstract class BaseAsyncChatMessageProcessor implements SingleProtocolCmdProcessor {

    protected ImServerConfig imServerConfig = ImConfig.Global.get();
    /**
     * 供子类拿到消息进行业务处理(如:消息持久化到数据库等)的抽象方法
     *
     * @param chatBody
     * @param imChannelContext
     */
    protected abstract void doProcess(Object chatBody, ImChannelContext imChannelContext);

    @Override
    public void process(ImChannelContext imChannelContext, Message message) {
        doProcess(message, imChannelContext);
    }

}
