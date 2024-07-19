/**
 *
 */
package com.kuailu.im.server.protocol.tcp;

import cn.hutool.json.JSONUtil;
import com.kuailu.im.core.ImChannelContext;
import com.kuailu.im.core.ImPacket;
import com.kuailu.im.core.ImStatus;
import com.kuailu.im.core.config.ImConfig;
import com.kuailu.im.core.exception.ImDecodeException;
import com.kuailu.im.core.exception.ImException;
import com.kuailu.im.core.packets.Command;
import com.kuailu.im.core.packets.RespBody;
import com.kuailu.im.core.protocol.AbstractProtocol;
import com.kuailu.im.core.tcp.*;
import com.kuailu.im.server.command.AbstractCmdHandler;
import com.kuailu.im.server.command.CommandManager;
import com.kuailu.im.server.config.ImServerConfig;

import com.kuailu.im.server.JimServerAPI;
import com.kuailu.im.server.protocol.AbstractProtocolHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.Objects;

/**
 * 版本: [1.0]
 * 功能说明: 
 * @author : WChao 创建时间: 2017年8月3日 下午7:44:48
 */
public class TcpProtocolHandler extends AbstractProtocolHandler {

    Logger logger = LoggerFactory.getLogger(TcpProtocolHandler.class);

    public TcpProtocolHandler() {
        this.protocol = new TcpProtocol(new TcpConvertPacket());
    }

    public TcpProtocolHandler(AbstractProtocol protocol) {
        super(protocol);
    }

    @Override
    public void init(ImServerConfig imServerConfig) {
        logger.info("Socket TCP Protocol initialized");
    }

    @Override
    public ByteBuffer encode(ImPacket imPacket, ImConfig imConfig, ImChannelContext imChannelContext) {
        TcpPacket tcpPacket = (TcpPacket) imPacket;
        return TcpServerEncoder.encode(tcpPacket, imConfig, imChannelContext);
    }

    @Override
    public void handler(ImPacket packet, ImChannelContext imChannelContext) throws ImException {
        TcpPacket tcpPacket = (TcpPacket) packet;
        AbstractCmdHandler cmdHandler = CommandManager.getCommand(tcpPacket.getCommand());
        if (cmdHandler == null) {
            ImPacket imPacket = new ImPacket(Command.COMMAND_UNKNOW, new RespBody(Command.COMMAND_UNKNOW, ImStatus.C10017).toByte());
            JimServerAPI.send(imChannelContext, imPacket);
            return;
        }


        ImPacket response = cmdHandler.handler(tcpPacket, imChannelContext);
        if (Objects.nonNull(response) && tcpPacket.getSynSeq() < 1) {
            JimServerAPI.send(imChannelContext, response);
        }
    }

    @Override
    public TcpPacket decode(ByteBuffer buffer, int limit, int position, int readableLength, ImChannelContext imChannelContext) throws ImDecodeException {
        TcpPacket tcpPacket = TcpServerDecoder.decode(buffer, imChannelContext);
        return tcpPacket;
    }

}
