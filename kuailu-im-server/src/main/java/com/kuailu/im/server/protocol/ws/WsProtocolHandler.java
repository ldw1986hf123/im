/**
 *
 */
package com.kuailu.im.server.protocol.ws;

import cn.hutool.json.JSONUtil;
import com.kuailu.im.core.ImChannelContext;
import com.kuailu.im.core.ImPacket;
import com.kuailu.im.core.ImStatus;
import com.kuailu.im.core.config.ImConfig;
import com.kuailu.im.core.exception.ImDecodeException;
import com.kuailu.im.core.exception.ImException;
import com.kuailu.im.core.http.HttpRequest;
import com.kuailu.im.core.http.HttpRequestDecoder;
import com.kuailu.im.core.http.HttpResponse;
import com.kuailu.im.core.http.HttpResponseEncoder;
import com.kuailu.im.core.packets.Command;
import com.kuailu.im.core.packets.LoginRespBody;
import com.kuailu.im.core.packets.Message;
import com.kuailu.im.core.packets.RespBody;
import com.kuailu.im.core.protocol.AbstractProtocol;
import com.kuailu.im.core.utils.JsonKit;
import com.kuailu.im.core.ws.*;
import com.kuailu.im.server.command.AbstractCmdHandler;
import com.kuailu.im.server.command.CommandManager;
import com.kuailu.im.server.config.ImServerConfig;

import com.kuailu.im.server.JimServerAPI;
import com.kuailu.im.server.protocol.AbstractProtocolHandler;
import com.kuailu.im.server.protocol.ProtocolManager;
import com.kuailu.im.server.service.IConversationService;
import com.kuailu.im.server.service.IUserAccountService;
import com.kuailu.im.server.util.ApplicationContextHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.Objects;

@Slf4j
public class WsProtocolHandler extends AbstractProtocolHandler {


    private WsConfig wsServerConfig;

    private IWsMsgHandler wsMsgHandler;

    private IUserAccountService userAccountService;

    public WsProtocolHandler() {
        this.protocol = new WsProtocol(new WsConvertPacket());
    }

    public WsProtocolHandler(WsConfig wsServerConfig, AbstractProtocol protocol) {
        super(protocol);
        this.wsServerConfig = wsServerConfig;
    }

    @Override
    public void init(ImServerConfig imServerConfig) {
        WsConfig wsConfig = imServerConfig.getWsConfig();
        if (Objects.isNull(wsConfig)) {
            wsConfig = WsConfig.newBuilder().build();
            imServerConfig.setWsConfig(wsConfig);
        }
        IWsMsgHandler wsMsgHandler = wsConfig.getWsMsgHandler();
        if (Objects.isNull(wsMsgHandler)) {
            wsConfig.setWsMsgHandler(new WsMsgHandler());
        }
        this.wsServerConfig = wsConfig;
        this.wsMsgHandler = wsServerConfig.getWsMsgHandler();
        this.userAccountService = ApplicationContextHelper.get().getBean(IUserAccountService.class);
        log.info("WebSocket Protocol  initialized");
    }

    @Override
    public ByteBuffer encode(ImPacket imPacket, ImConfig imConfig, ImChannelContext imChannelContext) {
        WsSessionContext wsSessionContext = (WsSessionContext) imChannelContext.getSessionContext();
        WsResponsePacket wsResponsePacket = (WsResponsePacket) imPacket;
        if (wsResponsePacket.getCommand() == Command.COMMAND_HANDSHAKE_RESP) {
            //握手包
            HttpResponse handshakeResponsePacket = wsSessionContext.getHandshakeResponsePacket();
            return HttpResponseEncoder.encode(handshakeResponsePacket, imChannelContext, true);
        } else {
            return WsServerEncoder.encode(wsResponsePacket, imChannelContext);
        }
    }

    @Override
    public void handler(ImPacket imPacket, ImChannelContext imChannelContext) throws ImException {
        WsRequestPacket wsRequestPacket = (WsRequestPacket) imPacket;
        AbstractCmdHandler cmdHandler = CommandManager.getCommand(wsRequestPacket.getCommand());
        if (cmdHandler == null) {
            //是否ws分片发包尾帧包
            if (!wsRequestPacket.isWsEof()) {
                return;
            }
            ImPacket wsPacket = new ImPacket(Command.COMMAND_UNKNOW, new RespBody(Command.COMMAND_UNKNOW, ImStatus.C10017).toByte());
            JimServerAPI.send(imChannelContext, wsPacket);
            return;
        }

        String wsBodyText = wsRequestPacket.getWsBodyText();

        if (StringUtils.isNotEmpty(wsBodyText)) {
            if (Command.COMMAND_HEARTBEAT_REQ.getNumber() != wsRequestPacket.getCommand().getNumber()) {
                log.info("收到的参数: {}", wsBodyText);
            }
        }

        Command command = wsRequestPacket.getCommand();
        String userId = imChannelContext.getUserId();
        if (!wsRequestPacket.isHandShake()) {
            int commandNumber = command.getNumber();
            if (Command.COMMAND_LOGIN_REQ.getNumber() != commandNumber && Command.COMMAND_CLOSE_REQ.getNumber() != commandNumber) {
                if (StringUtils.isEmpty(userId) || !userAccountService.isOnLine(userId)) {
                    log.info("未登录，不进行业务处理。对方主机：imChannelContext{}   command:{}  userId：{}", imChannelContext, command, userId);
                    ImPacket needLoginPacket = new ImPacket(new RespBody(command, ImStatus.NEED_LOGIN).toByte());
                    JimServerAPI.send(imChannelContext, needLoginPacket);
                    return;
                }
            }
        }
        ImPacket response = cmdHandler.handler(wsRequestPacket, imChannelContext);
        if (Objects.nonNull(response)) {
            JimServerAPI.send(imChannelContext, response);
        }
    }

    @Override
    public ImPacket decode(ByteBuffer buffer, int limit, int position, int readableLength, ImChannelContext imChannelContext) throws ImDecodeException {
        WsSessionContext wsSessionContext = (WsSessionContext) imChannelContext.getSessionContext();
        //握手
        if (!wsSessionContext.isHandshaked()) {
            HttpRequest httpRequest = HttpRequestDecoder.decode(buffer, imChannelContext, true);
            if (httpRequest == null) {
                return null;
            }
            //升级到WebSocket协议处理
            HttpResponse httpResponse = WsServerDecoder.updateWebSocketProtocol(httpRequest, imChannelContext);
            if (httpResponse == null) {
                throw new ImDecodeException("http协议升级到webSocket协议失败");
            }
            wsSessionContext.setHandshakeRequestPacket(httpRequest);
            wsSessionContext.setHandshakeResponsePacket(httpResponse);

            WsRequestPacket wsRequestPacket = new WsRequestPacket();
            wsRequestPacket.setHandShake(true);
            wsRequestPacket.setCommand(Command.COMMAND_HANDSHAKE_REQ);
            return wsRequestPacket;
        } else {
            WsRequestPacket wsRequestPacket = WsServerDecoder.decode(buffer, imChannelContext);
            if (wsRequestPacket == null) {
                return null;
            }
            Command command = null;
            if (wsRequestPacket.getWsOpcode() == Opcode.CLOSE) {
                command = Command.COMMAND_CLOSE_REQ;
            } else {
                try {
                    Message message = JsonKit.toBean(wsRequestPacket.getBody(), Message.class);

                    command = Command.forNumber(message.getCmd());
                } catch (Exception e) {
                    return wsRequestPacket;
                }
            }
            wsRequestPacket.setCommand(command);
            return wsRequestPacket;
        }
    }

    public WsConfig getWsServerConfig() {
        return wsServerConfig;
    }

    public void setWsServerConfig(WsConfig wsServerConfig) {
        this.wsServerConfig = wsServerConfig;
    }

    public IWsMsgHandler getWsMsgHandler() {
        return wsMsgHandler;
    }

    public void setWsMsgHandler(IWsMsgHandler wsMsgHandler) {
        this.wsMsgHandler = wsMsgHandler;
    }

}
