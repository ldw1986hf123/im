package com.kuailu.im.server.processor.chat;


import cn.hutool.json.JSONUtil;
import com.kuailu.im.core.ImChannelContext;
import com.kuailu.im.core.packets.ChatType;
import com.kuailu.im.server.constant.RedisCacheKey;
import com.kuailu.im.server.enums.MessageOperaTypeEnum;
import com.kuailu.im.server.enums.MessageTypeEnum;
import com.kuailu.im.server.enums.YesOrNoEnum;
import com.kuailu.im.server.model.entity.ChatMsg;
import com.kuailu.im.server.req.ChatReqParam;
import com.kuailu.im.server.req.MessageBody;
import com.kuailu.im.server.schduler.PushScheduledTask;
import com.kuailu.im.server.service.*;
import com.kuailu.im.server.service.impl.KafkaService;
import com.kuailu.im.server.util.RedisService;
import lombok.extern.slf4j.Slf4j;

/**
 * 异步处理器，异步保存到数据库，以便不影响发送消息的流程
 */
@Slf4j
public class ChatReqProcessor extends BaseAsyncChatMessageProcessor {
    IConversationService conversationService;
    IChatGroupService groupService;
    IChatMsgService chatMsgService;
    IMergedMsgService mergedMsgService;
    IChatGroupMemberService groupMemberService;
    IUserAccountService userAccountService;

    RedisService redisService;
    IAtMsgService atMsgService;
    PushScheduledTask pushScheduledTask;

    public ChatReqProcessor(IConversationService conversationService, IChatGroupService groupService,
                            IChatMsgService chatMsgService,
                            IMergedMsgService mergedMsgService,
                            IChatGroupMemberService groupMemberService,
                            IUserAccountService userAccountService,
                            PushScheduledTask pushScheduledTask,
                            RedisService redisService,
                            IAtMsgService atMsgService) {
        this.conversationService = conversationService;
        this.groupService = groupService;
        this.chatMsgService = chatMsgService;
        this.mergedMsgService = mergedMsgService;
        this.groupMemberService = groupMemberService;
        this.userAccountService = userAccountService;
        this.redisService = redisService;
        this.atMsgService = atMsgService;
        this.pushScheduledTask = pushScheduledTask;
    }


    @Override
    public void doProcess(Object object, ImChannelContext imChannelContext) {
        ChatReqParam chatReqParam = (ChatReqParam) object;
        String currentUserId = chatReqParam.getSender();
        Integer chatType = chatReqParam.getChatType();
        String groupId = chatReqParam.getGroupId();
        if (ChatType.CHAT_TYPE_PUBLIC.getNumber() == chatType && !groupMemberService.isInGroup(groupId, currentUserId)) {
            //todo 应该让前端直接让不在群用户不能再发消息
            log.info("userId:  {},groupId:{}  已经不在群里。消息不入库", currentUserId, groupId);
            return;
        }
        Long createdTime = chatReqParam.getCreatedTime();
        String messageId = chatReqParam.getId();
        Integer msgType = chatReqParam.getMsgType();
        String operaType = chatReqParam.getOperaType();
        String senderName = chatReqParam.getSenderName();
        String receiver = chatReqParam.getReceiver();
        String conversationId = chatReqParam.getConversationId();

        //更新 会话消息内容
        conversationService.updatePublicConversationMsgCache(conversationId, groupId);
        chatMsgService.updateUnReadMsgCount(groupId,currentUserId);
        //先处理文本信息
        MessageBody messageBody = chatReqParam.getMessageBody();
        ChatMsg chatMsg = new ChatMsg(messageId, currentUserId, senderName, chatType, conversationId, JSONUtil.toJsonPrettyStr(chatReqParam.getMessageBody()), operaType, receiver, msgType, groupId, createdTime, messageBody.getContent());

        chatMsgService.save(chatMsg);
        //保存合并消息
        if (MessageTypeEnum.MERGE_REDIRECT.getCode() == msgType) {
            String mergedUserName = chatReqParam.getMergedUserName();
            if (MessageOperaTypeEnum.MergeRedirect.getCode().equals(operaType)) {
                mergedMsgService.saveMergeMessage(chatReqParam.getMergedMessageIdList(), messageId, msgType, senderName, receiver, mergedUserName, currentUserId);
            } else {
                mergedMsgService.saveMergeMessage(chatReqParam.getMergedMessageId(), messageId, msgType, senderName, receiver, mergedUserName);
            }
        } else if (MessageTypeEnum.ATMessage.getCode() == msgType) {
            atMsgService.saveAtMsg(messageBody, messageId, currentUserId, groupId, conversationId);
        }

        pushScheduledTask.addToPushQueue(chatMsg);

        //todo 必须是保存到数据库之后，才刷新缓存这是因为web端还没有建立websocket连接，才需要这样强制刷新缓存，后期要优化掉
      /*  if (ChatType.FILE_HELPER.getNumber() == chatType) {
            chatMsgService..cleanApassMessageCache();
        }*/
    }

}
