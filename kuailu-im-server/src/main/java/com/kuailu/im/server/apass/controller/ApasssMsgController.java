package com.kuailu.im.server.apass.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.kuailu.im.core.apass.resp.ApassResult;
import com.kuailu.im.core.apass.resp.SendMsgVo;
import com.kuailu.im.core.apass.resp.status.ApassCode;
import com.kuailu.im.core.param.ApassChatReqParam;
import com.kuailu.im.server.dto.UserCacheDto;
import com.kuailu.im.server.model.entity.ChatMsg;
import com.kuailu.im.server.response.MessageHistoryResponse;
import com.kuailu.im.server.service.IChatMsgService;
import com.kuailu.im.server.service.IConversationService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

/**
 * 专门提供给apass服务调用
 */
@RestController
@RequestMapping(value = "/apass/message")
public class ApasssMsgController extends ApassBaseController {

    @Autowired
    IChatMsgService msgService;

    @Autowired
    IConversationService conversationService;

    /**
     * direction: early ，later
     *
     * @param groupId
     * @param messageId
     * @param direction
     * @return
     */
    @GetMapping("/getMessage")
    public ApassResult getMessage(@RequestParam(value = "groupId", required = false) String groupId,
                                  @RequestParam(value = "messageId", required = false) String messageId,
                                  @RequestParam(value = "direction") String direction,
                                  @RequestParam(value = "count", required = false) Integer count,
                                  HttpServletRequest request) {
        ApassResult ajaxResult = ApassResult.fail();
        List<ChatMsg> chatMsgList = new ArrayList<>();
        UserCacheDto userCacheDto = getCurrentUser(request);
        if (StringUtils.isEmpty(messageId)) {
            chatMsgList = msgService.getMessageContext(count, groupId);
        } else {
            chatMsgList = msgService.getMessageContext(count, groupId, messageId, direction);
        }
        MessageHistoryResponse messageData = msgService.buildChatList(chatMsgList, groupId);
        ajaxResult.success(messageData.getChatMsgList());
        return ajaxResult;
    }


    @DeleteMapping
    public ApassResult delete(@RequestParam(value = "groupId") String groupId,
                              @RequestParam(value = "messageId") String messageId,
                              HttpServletRequest request) {
        ApassResult ajaxResult = ApassResult.fail();

        UserCacheDto currentUser = getCurrentUser(request);

        ChatMsg chatMsg = msgService.getOne(new QueryWrapper<ChatMsg>().lambda().eq(ChatMsg::getGroupId, groupId).eq(ChatMsg::getMessageId, messageId));
        if (!chatMsg.getGroupId().equals(groupId) || !chatMsg.getSender().equals(currentUser.getUserId())) {
            ajaxResult.fail(ApassCode.ILLEGAL_PARAM);
            return ajaxResult;
        }
        msgService.deleteByMessageId(messageId, groupId);
        msgService.cleanMsgContent(messageId, groupId);
        ajaxResult.success();
        return ajaxResult;
    }

    @PostMapping("/send")
    public ApassResult send(@RequestBody ApassChatReqParam apassChatReqParam, HttpServletRequest request) {
        ApassResult apassResult = ApassResult.fail();
        UserCacheDto userCacheDto = getCurrentUser(request);

        Integer msgType = apassChatReqParam.getMsgType();
//        String senderName = apassChatReqParam.getSenderName();
        String receiver = apassChatReqParam.getReceiver();
        Integer chatType = apassChatReqParam.getChatType();
        String groupId = apassChatReqParam.getGroupId();
        ApassChatReqParam.MessageBody messageBody = apassChatReqParam.getMessageBody();
//        String conversationId = apassChatReqParam.getConversationId();

        String conversationId = conversationService.getConversationIdByGroupId(groupId, userCacheDto.getUserId());
        ChatMsg chatMsg = new ChatMsg.FileHelperBuilder(userCacheDto.getUserId(), userCacheDto.getUserName(), chatType, conversationId, messageBody, receiver, msgType, groupId).build();
        msgService.save(chatMsg);
        String messageId = chatMsg.getMessageId();


        msgService.send(messageId, userCacheDto.getUserId(), userCacheDto.getUserId(), groupId, conversationId, messageBody, msgType);
        SendMsgVo sendMsgVo = new SendMsgVo();
        sendMsgVo.setMessageId(messageId);
        apassResult.success(sendMsgVo);
        return apassResult;
    }


}
