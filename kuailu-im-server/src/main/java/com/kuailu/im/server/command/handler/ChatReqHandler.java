package com.kuailu.im.server.command.handler;

import cn.hutool.core.bean.BeanUtil;
import com.kuailu.im.core.ImChannelContext;
import com.kuailu.im.core.ImPacket;
import com.kuailu.im.core.ImStatus;
import com.kuailu.im.core.exception.ImException;
import com.kuailu.im.core.packets.ChatType;
import com.kuailu.im.core.packets.Command;
import com.kuailu.im.core.packets.Message;
import com.kuailu.im.core.packets.RespBody;
import com.kuailu.im.core.utils.JsonKit;
import com.kuailu.im.server.ImServerChannelContext;
import com.kuailu.im.server.JimServerAPI;
import com.kuailu.im.server.command.AbstractCmdHandler;
import com.kuailu.im.server.constant.RedisCacheKey;
import com.kuailu.im.server.dto.UserCacheDto;
import com.kuailu.im.server.enums.MessageOperaTypeEnum;
import com.kuailu.im.server.enums.MessageTypeEnum;
import com.kuailu.im.server.protocol.ProtocolManager;
import com.kuailu.im.server.queue.MsgQueueRunnable;
import com.kuailu.im.server.req.ChatReqParam;
import com.kuailu.im.server.req.MessageBody;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Objects;

@Slf4j
public class ChatReqHandler extends AbstractCmdHandler {

    @Override
    public ImPacket handler(ImPacket packet, ImChannelContext channelContext) throws ImException {
        ImServerChannelContext imServerChannelContext = (ImServerChannelContext) channelContext;
        String currentUserId = channelContext.getUserId();

        ChatReqParam chatReqParam = JsonKit.toBean(packet.getBody(), ChatReqParam.class);
        Integer chatType = chatReqParam.getChatType();
        Integer msgType = chatReqParam.getMsgType();
        String receiver = chatReqParam.getReceiver();
        UserCacheDto userCacheDto = redisService.getHashValue(RedisCacheKey.ONLINE_USER_CACHE + currentUserId, UserCacheDto.class);
        chatReqParam.setSender(currentUserId);
        chatReqParam.setSenderName(userCacheDto.getUserName());
        RespBody respBody = new RespBody(chatReqParam.getCmd());

        /**************************************************************校验入参是否合法*********************************************/
        if (!validateChatRqePara(chatReqParam, respBody)) {
            return ProtocolManager.Converter.respPacket(respBody, channelContext);
        }
        /**************************************************************校验入参是否合法*********************************************/


        /**************************通过这句，DefaultAsyncChatMessageProcessor 中的process方法才会得到异步执行，目前主要是异步保持到数据库和redis*/
        MsgQueueRunnable msgQueueRunnable = getMsgQueueRunnable(imServerChannelContext);
        msgQueueRunnable.executor.execute(msgQueueRunnable);
        msgQueueRunnable.addMsg(chatReqParam);
        /**************************通过这句，DefaultAsyncChatMessageProcessor 中的process方法才会得到异步执行，目前主要是异步保持到数据库和redis*/


        /************************** 构建推送给前端的结构体 ********************/
        PushMessage pushMessage = BeanUtil.copyProperties(chatReqParam, PushMessage.class);
        MessageBody messageBody = pushMessage.getMessageBody();
        if (MessageTypeEnum.MERGE_REDIRECT.getCode() == msgType) {
            String mergedUserName = chatReqParam.getMergedUserName();
            String operaType = chatReqParam.getOperaType();
            List<String> mergedMessageIdList = chatReqParam.getMergedMessageIdList();
            if (MessageOperaTypeEnum.MergeRedirect.getCode().equals(operaType)) {
                messageBody = mergedMsgService.formMergePushMessage(mergedMessageIdList, chatType, mergedUserName, imServerChannelContext.getUserId());
            } else if (MessageOperaTypeEnum.RedirectMerge.getCode().equals(operaType)) {
                messageBody = mergedMsgService.formMergePushMessage(chatReqParam.getMergedMessageId(), mergedUserName);
            }
        } else if (MessageTypeEnum.ATMessage.getCode() == msgType) {
            pushMessage.setLastAtAvatar(userAccountService.getAvatarUrl(currentUserId));
        }
        pushMessage.setMessageBody(messageBody);


        ImPacket chatPacket = new ImPacket(new RespBody(Command.COMMAND_CHAT_REQ, pushMessage).toByte());
        /************************** 构建推送给前端的结asdasd构体 ********************/

        //todo,序列号是干什么用的设置同步序列号;
        chatPacket.setSynSeq(packet.getSynSeq());
        //私聊
        if (ChatType.CHAT_TYPE_PRIVATE.getNumber() == chatReqParam.getChatType() || ChatType.FILE_HELPER.getNumber() == chatType) {
            JimServerAPI.sendToUser(receiver, chatPacket);
            //发送成功响应包
            return ProtocolManager.Packet.success(channelContext, Command.forNumber(chatReqParam.getCmd()), chatReqParam.getId());
        } else if (ChatType.CHAT_TYPE_PUBLIC.getNumber() == chatReqParam.getChatType()) {
            return sendToGroup(chatReqParam, chatPacket, channelContext);
        }
        return null;
    }


    @Override
    public Command command() {
        return Command.COMMAND_CHAT_REQ_2;
    }

    /**
     * 获取聊天业务处理异步消息队列
     *
     * @param imServerChannelContext IM通道上下文
     * @return
     */
    private MsgQueueRunnable getMsgQueueRunnable(ImServerChannelContext imServerChannelContext) {
        MsgQueueRunnable msgQueueRunnable = (MsgQueueRunnable) imServerChannelContext.getMsgQue();
        if (Objects.nonNull(msgQueueRunnable.getProtocolCmdProcessor())) {
            return msgQueueRunnable;
        }
        synchronized (MsgQueueRunnable.class) {
            msgQueueRunnable.setProtocolCmdProcessor(this.getSingleProcessor());
        }
        return msgQueueRunnable;
    }

    /**
     * 发送群聊消息
     *
     * @param chatBody
     * @param chatPacket
     * @param channelContext
     * @return
     */
    private ImPacket sendToGroup(ChatReqParam chatBody, ImPacket chatPacket, ImChannelContext channelContext) throws ImException {
        String groupId = chatBody.getGroupId();
        String currentUserId = channelContext.getUserId();
        //todo 后续应该优化调，用户退群之后，直接告诉前端，不能再继续群里发消息了
        if (!groupMemberService.isInGroup(groupId, currentUserId)) {
            log.info("发送者已不在该群，不能发送消息了，userId:{}", currentUserId);
            return ProtocolManager.Packet.success(channelContext, Command.forNumber(chatBody.getCmd()), chatBody.getId());
        } else {
            JimServerAPI.sendToGroup(groupId, chatPacket);
        }
        //发送成功响应包
        return ProtocolManager.Packet.success(channelContext, Command.forNumber(chatBody.getCmd()), chatBody.getId());
    }


    private Boolean validateChatRqePara(ChatReqParam chatBody, RespBody respBody) throws ImException {
        if (StringUtils.isEmpty(chatBody.getReceiver())) {
            respBody.setCode(ImStatus.DATA_FORMAT_ERROR.getCode()).setMsg("receiver 不能为空");
            return false;
        }
        if (StringUtils.isEmpty(chatBody.getConversationId())) {
            respBody.setCode(ImStatus.DATA_FORMAT_ERROR.getCode()).setMsg("conversationId 不能为空");
            return false;
        }
        return true;
    }

    class PushMessage extends Message {
        /**
         * todo 发送用户id  不需要前端传;
         */
        private String sender;

        //todo 不需要前端传
        private String senderName;
        /**
         * 目标用户id;
         */
        private String receiver;
        /**
         * msgType：0:文本、1:文件、2:语音、3:视频、 4：合并转发消息、5、图片
         */
        private Integer msgType;
        /**
         * 聊天类型;(如公聊、私聊)
         */
        private Integer chatType;
        /**
         * 消息内容;
         */
        private MessageBody messageBody;
        /**
         * 消息发到哪个群组;
         */
        private String groupId;

        private Long timestamp;

        private Integer status;

        /**
         * 合并消息的时候，才用到这个属性
         */
        private List<String> mergedMessageIdList;

        private String conversationId;

        private String lastAtAvatar;


        public String getSender() {
            return sender;
        }

        public void setSender(String sender) {
            this.sender = sender;
        }

        public String getSenderName() {
            return senderName;
        }

        public void setSenderName(String senderName) {
            this.senderName = senderName;
        }

        public String getReceiver() {
            return receiver;
        }

        public void setReceiver(String receiver) {
            this.receiver = receiver;
        }

        public Integer getMsgType() {
            return msgType;
        }

        public void setMsgType(Integer msgType) {
            this.msgType = msgType;
        }

        public Integer getChatType() {
            return chatType;
        }

        public void setChatType(Integer chatType) {
            this.chatType = chatType;
        }

        public MessageBody getMessageBody() {
            return messageBody;
        }

        public void setMessageBody(MessageBody messageBody) {
            this.messageBody = messageBody;
        }

        public String getGroupId() {
            return groupId;
        }

        public void setGroupId(String groupId) {
            this.groupId = groupId;
        }

        public Long getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(Long timestamp) {
            this.timestamp = timestamp;
        }

        public Integer getStatus() {
            return status;
        }

        public void setStatus(Integer status) {
            this.status = status;
        }

        public List<String> getMergedMessageIdList() {
            return mergedMessageIdList;
        }

        public void setMergedMessageIdList(List<String> mergedMessageIdList) {
            this.mergedMessageIdList = mergedMessageIdList;
        }

        public String getConversationId() {
            return conversationId;
        }

        public void setConversationId(String conversationId) {
            this.conversationId = conversationId;
        }

        public String getLastAtAvatar() {
            return lastAtAvatar;
        }

        public void setLastAtAvatar(String lastAtAvatar) {
            this.lastAtAvatar = lastAtAvatar;
        }
    }


}
