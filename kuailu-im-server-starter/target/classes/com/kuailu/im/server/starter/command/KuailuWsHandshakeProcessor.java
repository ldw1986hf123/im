package com.kuailu.im.server.starter.command;

import com.kuailu.im.core.ImChannelContext;
import com.kuailu.im.core.ImConst;
import com.kuailu.im.core.ImPacket;
import com.kuailu.im.core.ImStatus;
import com.kuailu.im.core.exception.ImException;
import com.kuailu.im.core.http.HttpRequest;
import com.kuailu.im.core.packets.Command;
import com.kuailu.im.core.packets.LoginReqBody;
import com.kuailu.im.core.packets.RespBody;
import com.kuailu.im.core.utils.JsonKit;
import com.kuailu.im.server.JimServerAPI;
import com.kuailu.im.server.command.CommandManager;
import com.kuailu.im.server.command.handler.LoginReqHandler;
import com.kuailu.im.server.processor.handshake.WsHandshakeProcessor;
import com.kuailu.im.server.protocol.ProtocolManager;
import lombok.extern.slf4j.Slf4j;

/**
 * @description:
 * @time: 2022/11/30 18:44
 */
@Slf4j
public class KuailuWsHandshakeProcessor extends WsHandshakeProcessor {
    @Override
    public void onAfterHandshake(ImPacket packet, ImChannelContext imChannelContext) throws ImException {
        RespBody resPacket = new RespBody(Command.COMMAND_WS_CONNECTED, ImStatus.OK);
        ImPacket imPacket=new ImPacket();
        imPacket.setBody(resPacket.toByte());
//        log.info("握手完成，连接建立  对方主机：{}", imChannelContext.getClientNode().toString());
        JimServerAPI.send(imChannelContext, imPacket);
    }
}
