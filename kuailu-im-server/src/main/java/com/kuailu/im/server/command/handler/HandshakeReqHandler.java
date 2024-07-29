package com.kuailu.im.server.command.handler;

import com.alibaba.fastjson.JSON;
import com.kuailu.im.core.ImChannelContext;
import com.kuailu.im.core.ImPacket;
import com.kuailu.im.core.exception.ImException;
import com.kuailu.im.core.http.HttpRequest;
import com.kuailu.im.core.packets.Command;
import com.kuailu.im.core.ws.WsSessionContext;
import com.kuailu.im.server.JimServerAPI;
import com.kuailu.im.server.command.AbstractCmdHandler;
import com.kuailu.im.server.processor.handshake.HandshakeCmdProcessor;
import lombok.extern.slf4j.Slf4j;


import java.util.Objects;

/**
 * 版本: [1.0]
 * 功能说明: 心跳cmd命令处理器
 *
 * @author : WChao 创建时间: 2017年9月21日 下午3:33:23
 */
@Slf4j
public class HandshakeReqHandler extends AbstractCmdHandler {

    @Override
    public ImPacket handler(ImPacket packet, ImChannelContext channelContext) throws ImException {
//        log.info("HandshakeReqHandler 进行握手。packet：{}", JSON.toJSONString(packet));
        HandshakeCmdProcessor handshakeProcessor = this.getMultiProcessor(channelContext, HandshakeCmdProcessor.class);
        if (Objects.isNull(handshakeProcessor)) {
            JimServerAPI.remove(channelContext, "没有对应的握手协议处理器HandshakeCmdProcessor...");
            return null;
        }
        ImPacket handShakePacket = handshakeProcessor.handshake(packet, channelContext);
        if (handShakePacket == null) {
            JimServerAPI.remove(channelContext, "业务层不同意握手");
            return null;
        }
        JimServerAPI.send(channelContext, handShakePacket);
        WsSessionContext wsSessionContext = (WsSessionContext) channelContext.getSessionContext();
        HttpRequest request = wsSessionContext.getHandshakeRequestPacket();
        handshakeProcessor.onAfterHandshake(request, channelContext);
        return null;
    }

    @Override
    public Command command() {
        return Command.COMMAND_HANDSHAKE_REQ;
    }
}
