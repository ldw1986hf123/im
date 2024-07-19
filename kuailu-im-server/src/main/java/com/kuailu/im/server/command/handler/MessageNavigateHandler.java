package com.kuailu.im.server.command.handler;

import com.kuailu.im.core.ImChannelContext;
import com.kuailu.im.core.ImPacket;
import com.kuailu.im.core.ImStatus;
import com.kuailu.im.core.exception.AppException;
import com.kuailu.im.core.exception.ImException;
import com.kuailu.im.core.packets.Command;
import com.kuailu.im.core.packets.Message;
import com.kuailu.im.core.packets.RespBody;
import com.kuailu.im.core.utils.JsonKit;
import com.kuailu.im.server.JimServerAPI;
import com.kuailu.im.server.command.AbstractCmdHandler;
import com.kuailu.im.server.model.entity.ChatMsg;
import com.kuailu.im.server.protocol.ProtocolManager;
import com.kuailu.im.server.req.MessageBody;
import com.kuailu.im.server.req.MessageNavigateReqBody;
import com.kuailu.im.server.response.MessageHistoryResponse;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 定位消息
 */
@Slf4j
public class MessageNavigateHandler extends AbstractCmdHandler {

    @Override
    public Command command() {
        return Command.COMMAND_MESSAGE_NAVIGATE;
    }

    @Override
    public ImPacket handler(ImPacket packet, ImChannelContext imChannelContext) throws ImException {

        MessageNavigateReqBody messageReqBody = JsonKit.toBean(packet.getBody(), MessageNavigateReqBody.class);
        Integer cmd = messageReqBody.getCmd();
        ImPacket messageNavigatePacket = new ImPacket(cmd);
        //群组ID;

        String messageId = messageReqBody.getMessageId();
        String groupId = messageReqBody.getGroupId();
        //分页数量;
        Integer count = messageReqBody.getCount();

        RespBody respBody = new RespBody(cmd);
        /**************************************************************校验入参是否合法*********************************************/
        if (!validateChatRqePara(messageReqBody, respBody)) {
            return ProtocolManager.Converter.respPacket(respBody, imChannelContext);
        }
        /**************************************************************校验入参是否合法*********************************************/
        List<ChatMsg> chatMsgList = new ArrayList<>();
        try {
            chatMsgList = iChatMsgService.navigate(messageId, groupId, count);
        } catch (AppException e) {
            log.error("定位消息异常", e);
            RespBody resPacket = new RespBody(Command.forNumber(messageReqBody.getCmd()), ImStatus.CANNOT_FIND_DATA);
            messageNavigatePacket.setBody(resPacket.toByte());
            JimServerAPI.send(imChannelContext, messageNavigatePacket);
            return null;
        }
        //查询历史记录
        MessageHistoryResponse messageData = iChatMsgService.buildChatList(chatMsgList, groupId);

        RespBody resPacket = new RespBody(Command.forNumber(cmd), ImStatus.OK, messageData);
        messageNavigatePacket.setBody(resPacket.toByte());
        JimServerAPI.send(imChannelContext, messageNavigatePacket);

        return null;
    }


   /* @Data
    class MessageNavigateParam extends Message {
        private String messageId;
        *//**
     * 群组id;
     *//*
        private String groupId;
        */

    /**
     * 数量
     *//*
        private Integer count = 50;

    }*/

  /*  @Data
    public class MessageContextResponse {
        private String messageId;
        private String sender;
        private String senderName;
        private String receiver;
        private Integer msgType;
        private Integer chatType;
        private MessageBody messageBody;
        protected Long createdTime;
        private String groupId;
        private Integer status;
        private Integer isRead;
    }
*/
    private Boolean validateChatRqePara(MessageNavigateReqBody messageHistoryReqBody, RespBody respBody) {
      /*  if (StringUtils.isEmpty(messageHistoryReqBody.getDirection())) {
            respBody.setCode(ImStatus.DATA_FORMAT_ERROR.getCode()).setMsg("direction 不能为空");
            return false;
        } else*/
        if (null == messageHistoryReqBody.getGroupId()) {
            respBody.setCode(ImStatus.DATA_FORMAT_ERROR.getCode()).setMsg("groupId 不能为空");
            return false;
        } else if (null == messageHistoryReqBody.getMessageId()) {
            respBody.setCode(ImStatus.DATA_FORMAT_ERROR.getCode()).setMsg("messageId 不能为空");
            return false;
        }
        return true;
    }
}
