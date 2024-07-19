/**
 *
 */
package com.kuailu.im.server.protocol;

import com.kuailu.im.core.ImChannelContext;
import com.kuailu.im.core.ImConst;
import com.kuailu.im.core.ImPacket;
import com.kuailu.im.core.ImStatus;
import com.kuailu.im.core.exception.ImException;
import com.kuailu.im.core.packets.Command;
import com.kuailu.im.core.packets.RespBody;
import com.kuailu.im.core.protocol.IProtocolConverter;
import com.kuailu.im.server.ImServerChannelContext;
import com.kuailu.im.server.config.ImServerConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

/**
 */
@Slf4j
public class ProtocolManager implements ImConst {
    private static Map<String, AbstractProtocolHandler> serverHandlers = new HashMap<String, AbstractProtocolHandler>();

    static {
        try {
            List<ProtocolHandlerConfiguration> configurations = ProtocolHandlerConfigurationFactory.parseConfiguration();
            init(configurations);
        } catch (Exception e) {
            log.error(e.toString(), e);
        }
    }

    private static void init(List<ProtocolHandlerConfiguration> configurations) throws Exception {
        for (ProtocolHandlerConfiguration configuration : configurations) {
            Class<AbstractProtocolHandler> serverHandlerClazz = (Class<AbstractProtocolHandler>) Class.forName(configuration.getServerHandler());
            AbstractProtocolHandler serverHandler = serverHandlerClazz.newInstance();
            addServerHandler(serverHandler);
        }
    }

    public static AbstractProtocolHandler addServerHandler(AbstractProtocolHandler serverHandler) throws ImException {
        if (Objects.isNull(serverHandler)) {
            throw new ImException("ProtocolHandler must not null ");
        }
        return serverHandlers.put(serverHandler.getProtocol().name(), serverHandler);
    }

    public static AbstractProtocolHandler removeServerHandler(String name) throws ImException {
        if (StringUtils.isEmpty(name)) {
            throw new ImException("server name must not empty");
        }
        return serverHandlers.remove(name);
    }

    public static AbstractProtocolHandler initProtocolHandler(ByteBuffer buffer, ImChannelContext imChannelContext) {
        ImServerChannelContext imServerChannelContext = (ImServerChannelContext) imChannelContext;
        for (Entry<String, AbstractProtocolHandler> entry : serverHandlers.entrySet()) {
            AbstractProtocolHandler protocolHandler = entry.getValue();
            try {
                if (protocolHandler.getProtocol().isProtocol(buffer, imServerChannelContext)) {
                    imServerChannelContext.setProtocolHandler(protocolHandler);
                    return protocolHandler;
                }
            } catch (Throwable e) {
                log.error(e.getMessage());
            }
        }
        return null;
    }

    public static <T> T getServerHandler(String name, Class<T> clazz) {
        AbstractProtocolHandler serverHandler = serverHandlers.get(name);
        if (Objects.isNull(serverHandler)) {
            return null;
        }
        return (T) serverHandler;
    }

    public static void init() {
        init((ImServerConfig) ImServerConfig.Global.get());
    }

    public static void init(ImServerConfig imServerConfig) {
        log.info("start init protocol [{}]", ImConst.KIM);
        for (Entry<String, AbstractProtocolHandler> entry : serverHandlers.entrySet()) {
            try {
                entry.getValue().init(imServerConfig);
            } catch (Exception e) {
                log.error(e.getMessage());
            }
        }
        log.info("init protocol is completed [{}]", ImConst.KIM);
    }

    public static class Converter {

        /**
         * 功能描述：[转换不同协议响应包]
         * @author：WChao 创建时间: 2017年9月21日 下午3:21:54
         * @param respBody 响应消息体
         * @param imChannelContext IM通道上下文
         * @return
         *
         */
        public static ImPacket respPacket(RespBody respBody, ImChannelContext imChannelContext) throws ImException {
            if (Objects.isNull(respBody)) {
                throw new ImException("响应包体不能为空!");
            }
            return respPacket(respBody.toByte(), respBody.getCmd(), imChannelContext);
        }

        /**
         * 功能描述：[转换不同协议响应包]
         * @param body 消息体字节
         * @param command 命令码
         * @param imChannelContext IM通道上下文
         * @return
         * @throws ImException
         */
        public static ImPacket respPacket(byte[] body, Command command, ImChannelContext imChannelContext) throws ImException {
            return getProtocolConverter(imChannelContext).RespPacket(body, command, imChannelContext);
        }

        /**
         * 功能描述：[转换不同协议响应包]
         * @param imPacket 消息包
         * @param imChannelContext IM通道上下文
         * @return
         * @throws ImException
         */
        public static ImPacket respPacket(ImPacket imPacket, ImChannelContext imChannelContext) throws ImException {
            return respPacket(imPacket, imPacket.getCommand(), imChannelContext);
        }

        /**
         * 功能描述：[转换不同协议响应包]
         * @author：WChao 创建时间: 2017年9月21日 下午3:21:54
         * @param imPacket 消息包
         * @param command 命令码
         * @param imChannelContext IM通道上下文
         * @return
         *
         */
        public static ImPacket respPacket(ImPacket imPacket, Command command, ImChannelContext imChannelContext) throws ImException {
            return getProtocolConverter(imChannelContext).RespPacket(imPacket, command, imChannelContext);
        }

        /**
         * 通过通道获取当前通道协议
         * @param imChannelContext IM通道上下文
         * @return
         * @throws ImException
         */
        private static IProtocolConverter getProtocolConverter(ImChannelContext imChannelContext) throws ImException {
            ImServerChannelContext serverChannelContext = (ImServerChannelContext) imChannelContext;
            AbstractProtocolHandler protocolHandler = serverChannelContext.getProtocolHandler();
            if (Objects.isNull(protocolHandler)) {
                throw new ImException("协议[ProtocolHandler]未初始化,协议包转化失败");
            }
            IProtocolConverter converter = protocolHandler.getProtocol().getConverter();
            if (converter != null) {
                return converter;
            } else {
                throw new ImException("未获取到协议转化器[ProtocolConverter]");
            }
        }

    }

    public static class Packet {
        /**
         * 数据格式不正确响应包
         * @param imChannelContext
         * @return imPacket
         * @throws ImException
         */
        public static ImPacket dataInCorrect(ImChannelContext imChannelContext) throws ImException {
            RespBody chatDataInCorrectRespPacket = new RespBody(Command.COMMAND_CHAT_RESP, ImStatus.DATA_FORMAT_ERROR);
            ImPacket respPacket = Converter.respPacket(chatDataInCorrectRespPacket, imChannelContext);
            respPacket.setStatus(ImStatus.DATA_FORMAT_ERROR);
            return respPacket;
        }

        /**
         * 发送成功响应包
         * @param imChannelContext
         * @return imPacket
         * @throws ImException
         */
        public static ImPacket success(ImChannelContext imChannelContext) throws ImException {
            RespBody chatDataInCorrectRespPacket = new RespBody(Command.COMMAND_CHAT_RESP, ImStatus.OK);
            ImPacket respPacket = Converter.respPacket(chatDataInCorrectRespPacket, imChannelContext);
            respPacket.setStatus(ImStatus.OK);
            return respPacket;
        }

        public static ImPacket success(ImChannelContext imChannelContext,Object data) throws ImException {
            RespBody chatDataInCorrectRespPacket = new RespBody(Command.COMMAND_CHAT_RESP, ImStatus.OK,data);
            ImPacket respPacket = Converter.respPacket(chatDataInCorrectRespPacket, imChannelContext);
            respPacket.setStatus(ImStatus.OK);
            return respPacket;
        }

        public static ImPacket success(ImChannelContext imChannelContext,Command command,Object data) throws ImException {
            RespBody chatDataInCorrectRespPacket = new RespBody(command, ImStatus.OK,data);
            ImPacket respPacket = Converter.respPacket(chatDataInCorrectRespPacket, imChannelContext);
            respPacket.setStatus(ImStatus.OK);
            return respPacket;
        }
        /**
         * 用户不在线响应包
         * @param imChannelContext
         * @return
         * @throws ImException
         */
        public static ImPacket offline(ImChannelContext imChannelContext) throws ImException {
            RespBody chatDataInCorrectRespPacket = new RespBody(Command.COMMAND_CHAT_RESP, ImStatus.C10001);
            ImPacket respPacket = Converter.respPacket(chatDataInCorrectRespPacket, imChannelContext);
            respPacket.setStatus(ImStatus.C10001);
            return respPacket;
        }


        /**
         * 用户不在线响应包
         * @param imChannelContext
         * @return
         * @throws ImException
         */
        public static ImPacket offline(ImChannelContext imChannelContext,Command command, Object data) throws ImException {
            RespBody chatDataInCorrectRespPacket = new RespBody(command, ImStatus.C10001, data);
            ImPacket respPacket = Converter.respPacket(chatDataInCorrectRespPacket, imChannelContext);
            respPacket.setStatus(ImStatus.C10001);
            return respPacket;
        }

        /**
         * 非对方白名单响应包
         * @param imChannelContext
         * @return imPacket
         * @throws ImException
         */
        public static ImPacket notWhiteList(ImChannelContext imChannelContext, Object data) throws ImException {
            RespBody chatDataInCorrectRespPacket = new RespBody(Command.COMMAND_CHAT_REQ_2, ImStatus.NOT_WHITE_LIST, data);
            ImPacket respPacket = Converter.respPacket(chatDataInCorrectRespPacket, imChannelContext);
            respPacket.setStatus(ImStatus.NOT_WHITE_LIST);
            return respPacket;
        }
    }
}
