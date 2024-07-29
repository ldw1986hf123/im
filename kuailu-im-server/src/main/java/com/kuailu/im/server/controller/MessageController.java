package com.kuailu.im.server.controller;

import com.kuailu.im.server.model.ResponseModel;
import com.kuailu.im.server.model.entity.ChatMsg;
import com.kuailu.im.server.processor.param.RevokeMessageParam;
import com.kuailu.im.server.response.MessageContextVo;
import com.kuailu.im.server.service.IChatMsgService;
import com.kuailu.im.server.service.IMergedMsgService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping(value = "/message")
public class MessageController {

    @Autowired
    IChatMsgService messageService;

    @Autowired
    IMergedMsgService mergedMsgService;

    @PostMapping("/revoke")
    public ResponseModel revoke(@RequestBody RevokeMessageParam revokeMessageParam) {
        return messageService.revoke(revokeMessageParam);
    }

  /*  @GetMapping("/navigate")
    public ResponseModel navigate(@RequestParam(value = "messageId") String messageId,
                                  @RequestParam(value = "groupId") String groupId,
                                  @RequestParam(value = "count") Integer count) {
        return messageService.navigate(messageId, groupId, count);
    }
*/

 /*   @GetMapping("/context")
    public ResponseModel context(@RequestParam(value = "messageId") String messageId,
                                 @RequestParam(value = "groupId") String groupId,
                                 @RequestParam(value = "direction") String direction,
                                 @RequestParam(value = "count",required = false,defaultValue = "50") Integer count ) {
        //查询历史记录
        List<ChatMsg> chatMsgList = messageService.getMessageContext(count, groupId ,messageId, direction);
        List<MessageContextVo> messageData = messageService.buildContextChatList(chatMsgList, groupId);
        return ResponseModel.success(messageData);
    }*/


    @GetMapping("getMergeEntityDetail")
    public ResponseModel getMergeEntityDetail(@RequestParam(value = "messageId") String messageId) {
        return mergedMsgService.getMergeEntityDetail(messageId);
    }

}
