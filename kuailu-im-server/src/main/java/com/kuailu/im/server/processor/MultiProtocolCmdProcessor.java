package com.kuailu.im.server.processor;


import com.kuailu.im.core.ImChannelContext;

/**
 * @desc 多协议CMD命令处理器
 * @date 2020-03-19
 * @author WChao
 */
public interface MultiProtocolCmdProcessor extends ProtocolCmdProcessor {
    /**
     * 不同协议判断方法
     * @param imChannelContext
     * @return
     */
    boolean isProtocol(ImChannelContext imChannelContext);
}
