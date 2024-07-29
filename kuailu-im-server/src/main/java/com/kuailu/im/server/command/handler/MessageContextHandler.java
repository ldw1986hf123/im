package com.kuailu.im.server.command.handler;

import com.kuailu.im.core.ImChannelContext;
import com.kuailu.im.core.ImPacket;
import com.kuailu.im.core.ImStatus;
import com.kuailu.im.core.config.ImConfig;
import com.kuailu.im.core.exception.ImException;
import com.kuailu.im.core.message.MessageHelper;
import com.kuailu.im.core.packets.ChatMsgReadConfirmBody;
import com.kuailu.im.core.packets.Command;
import com.kuailu.im.core.packets.Message;
import com.kuailu.im.core.packets.RespBody;
import com.kuailu.im.core.utils.JsonKit;
import com.kuailu.im.server.command.AbstractCmdHandler;
import com.kuailu.im.server.config.ImServerConfig;
import com.kuailu.im.server.model.entity.ChatMsg;
import com.kuailu.im.server.protocol.ProtocolManager;
import com.kuailu.im.server.req.ChatReqParam;
import com.kuailu.im.server.req.MessageBody;
import com.kuailu.im.server.req.MessageContextReqBody;
import com.kuailu.im.server.req.MessageHistoryReqBody;
import com.kuailu.im.server.response.MessageHistoryResponse;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 获取消息上下文
 */
@Slf4j
public class MessageContextHandler extends AbstractCmdHandler {


    @Override
    public Command command() {
        return Command.COMMAND_GET_MESSAGE_CONTEXT;
    }

    @Override
    public ImPacket handler(ImPacket packet, ImChannelContext imChannelContext) throws ImException {
        MessageContextReqBody messageReqBody = JsonKit.toBean(packet.getBody(), MessageContextReqBody.class);

        //群组ID;
        String groupId = messageReqBody.getGroupId();
        String messageId = messageReqBody.getMessageId();

        //分页数量;
        Integer count = messageReqBody.getCount();

        RespBody respBody = new RespBody(messageReqBody.getCmd());
        /**************************************************************校验入参是否合法*********************************************/
        if (!validateChatRqePara(messageReqBody, respBody)) {
            return ProtocolManager.Converter.respPacket(respBody, imChannelContext);
        }
        /**************************************************************校验入参是否合法*********************************************/
        //查询历史记录
        List<ChatMsg> chatMsgList = iChatMsgService.getMessageContext(count, groupId, messageId, messageReqBody.getDirection());
        MessageHistoryResponse messageData = iChatMsgService.buildChatList(chatMsgList, groupId);
        RespBody resPacket = new RespBody(messageReqBody.getCmd(), messageData);

        return ProtocolManager.Converter.respPacket(resPacket, imChannelContext);
    }


    private Boolean validateChatRqePara(MessageContextReqBody messageHistoryReqBody, RespBody respBody) {
        if (StringUtils.isEmpty(messageHistoryReqBody.getDirection())) {
            respBody.setCode(ImStatus.DATA_FORMAT_ERROR.getCode()).setMsg("direction 不能为空");
            return false;
        } else if (!MessageContextReqBody.DIRECTION_EARLY.equals(messageHistoryReqBody.getDirection())
                && !MessageContextReqBody.DIRECTION_LATER.equals(messageHistoryReqBody.getDirection())) {
            respBody.setCode(ImStatus.DATA_FORMAT_ERROR.getCode()).setMsg("direction 值只能是early或者later");
            return false;
        } else if (null == messageHistoryReqBody.getGroupId()) {
            respBody.setCode(ImStatus.DATA_FORMAT_ERROR.getCode()).setMsg("groupId 不能为空");
            return false;
        } else if (null == messageHistoryReqBody.getMessageId()) {
            respBody.setCode(ImStatus.DATA_FORMAT_ERROR.getCode()).setMsg("messageId 不能为空");
            return false;
        }
        return true;
    }
}
