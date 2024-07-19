package com.kuailu.im.server.command.handler;

import cn.hutool.core.bean.BeanUtil;
import com.kuailu.im.core.ImChannelContext;
import com.kuailu.im.core.ImPacket;
import com.kuailu.im.core.ImStatus;
import com.kuailu.im.core.config.ImConfig;
import com.kuailu.im.core.exception.ImException;
import com.kuailu.im.core.message.MessageHelper;
import com.kuailu.im.core.packets.*;
import com.kuailu.im.core.utils.JsonKit;
import com.kuailu.im.server.command.AbstractCmdHandler;
import com.kuailu.im.server.config.ImServerConfig;
import com.kuailu.im.server.model.entity.ChatMsg;
import com.kuailu.im.server.req.MessageHistoryReqBody;
import com.kuailu.im.server.service.IChatMsgService;
import com.kuailu.im.server.util.SpringContextHolder;
import com.kuailu.im.server.response.MessageHistoryResponse;

import com.kuailu.im.server.protocol.ProtocolManager;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 todo chattype=2的时候，消息助手不用做该操作,应该让前端直接不调用该接口，
 */
@Slf4j
public class MessageHistoryHandler extends AbstractCmdHandler {

    @Override
    public Command command() {
        return Command.COMMAND_GET_MESSAGE_HISTORY;
    }

    @Override
    public ImPacket handler(ImPacket packet, ImChannelContext imChannelContext) throws ImException {
        MessageHistoryReqBody messageReqBody = JsonKit.toBean(packet.getBody(), MessageHistoryReqBody.class);
        String currentUserId = imChannelContext.getUserId();
        //群组ID;
        String groupId = messageReqBody.getGroupId();

        String receiver = messageReqBody.getReceiver();
        //消息区间结束时间;
        Long endTime = messageReqBody.getEndTime();
        //分页数量;
        Integer count = messageReqBody.getCount();

        RespBody respBody = new RespBody(messageReqBody.getCmd());
        /**************************************************************校验入参是否合法*********************************************/
        if (!validateChatRqePara(messageReqBody, respBody)) {
            return ProtocolManager.Converter.respPacket(respBody, imChannelContext);
        }
        /**************************************************************校验入参是否合法*********************************************/
        //查询历史记录
        List<ChatMsg> chatMsgList = iChatMsgService.getMessageHistory(count, groupId, endTime);
        MessageHistoryResponse messageData = iChatMsgService.buildChatList(chatMsgList, receiver, groupId);
        messageData.setLastUnReadAtMsg(atMsgService.getLastUnReadMsg(groupId, currentUserId));

        RespBody resPacket = new RespBody(messageReqBody.getCmd(), messageData);
        return ProtocolManager.Converter.respPacket(resPacket, imChannelContext);
    }

    private Boolean validateChatRqePara(MessageHistoryReqBody messageHistoryReqBody, RespBody respBody) {
        if (StringUtils.isEmpty(messageHistoryReqBody.getReceiver())) {
            respBody.setCode(ImStatus.DATA_FORMAT_ERROR.getCode()).setMsg("receiver 不能为空");
            return false;
        } /*else if (null == messageHistoryReqBody.getChatType()) {
            respBody.setCode(ImStatus.DATA_FORMAT_ERROR.getCode()).setMsg("chatType 不能为空");
            return false;
        } */ else if (null == messageHistoryReqBody.getEndTime()) {
            respBody.setCode(ImStatus.DATA_FORMAT_ERROR.getCode()).setMsg("endTime 不能为空");
            return false;
        } else if (messageHistoryReqBody.getCount() > 500 || messageHistoryReqBody.getCount() < 0) {
            respBody.setCode(ImStatus.DATA_FORMAT_ERROR.getCode()).setMsg("count 必须在0-500之间");
            return false;
        }
        return true;
    }
}
