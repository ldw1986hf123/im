package com.kuailu.im.server.command.handler;

import com.kuailu.im.core.ImChannelContext;
import com.kuailu.im.core.ImPacket;
import com.kuailu.im.core.ImStatus;
import com.kuailu.im.core.config.ImConfig;
import com.kuailu.im.core.exception.ImException;
import com.kuailu.im.core.message.MessageHelper;
import com.kuailu.im.core.packets.*;
import com.kuailu.im.core.utils.JsonKit;
import com.kuailu.im.server.ImServerChannelContext;
import com.kuailu.im.server.JimServerAPI;
import com.kuailu.im.server.command.AbstractCmdHandler;
import com.kuailu.im.server.config.ImServerConfig;
import com.kuailu.im.server.protocol.ProtocolManager;
import lombok.extern.slf4j.Slf4j;

import static com.kuailu.im.core.packets.Command.COMMAND_CHAT_RESP;

/**
 * todo chattype=2的时候，消息助手不用做该操作,应该让前端直接不调用该接口，
 */
@Slf4j
public class ChatMsgReadConfirmReqHandler extends AbstractCmdHandler {
    @Override
    public Command command() {
        return Command.COMMAND_MSG_READ_CONFIRM_REQ;
    }

    @Override
    public ImPacket handler(ImPacket imPacket, ImChannelContext imChannelContext) throws ImException {
        ChatMsgReadConfirmBody chatMsgReadConfirmBody = JsonKit.toBean(imPacket.getBody(), ChatMsgReadConfirmBody.class);
        ImServerChannelContext imServerChannelContext = (ImServerChannelContext) imChannelContext;

        String currentUserId = imChannelContext.getUserId();
        String groupId = chatMsgReadConfirmBody.getGroupId();
        String messageId = chatMsgReadConfirmBody.getMsgId();
        try {
            imPacket.setBody(chatMsgReadConfirmBody.toByte());

            JimServerAPI.sendToGroup(groupId, imPacket);

            //把传过来的消息之前的都置为已读
            iChatMsgService.confirmReadMsg(currentUserId, groupId, messageId);
            //更新缓存中的未读数
            conversationService.updateConversationCache(groupId, currentUserId);

            return ProtocolManager.Packet.success(imServerChannelContext);
        } catch (Exception e) {
            log.error("已读消息回执异常 chatMsgReadConfirmBody:{}", chatMsgReadConfirmBody, e);
        }
        RespBody resPacket = new RespBody(COMMAND_CHAT_RESP, ImStatus.ERROR);
        return ProtocolManager.Converter.respPacket(resPacket, imChannelContext);
    }
}
