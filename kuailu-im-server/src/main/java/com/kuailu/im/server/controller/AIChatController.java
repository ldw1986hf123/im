package com.kuailu.im.server.controller;

import cn.hutool.json.JSONUtil;
import com.kuailu.im.server.model.ResponseModel;
import com.kuailu.im.server.processor.param.*;
import com.kuailu.im.server.service.IAIChatMsgService;
import com.kuailu.im.server.service.IChatMsgService;
import com.kuailu.im.server.service.IMergedMsgService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping(value = "/aiChat")
@Slf4j
public class AIChatController {

    @Autowired
    IAIChatMsgService iaiChatMsgService;

    @PostMapping("/history")
    public ResponseModel history(@RequestBody AIChatHistoryParam param) {
        return iaiChatMsgService.history(param.getUserId(), param.getMessageId(),param.getTopicId(), param.getCount());
    }

    @GetMapping("/getUserStatus")
    public ResponseModel getUserStatus(@RequestParam("userId") String userId) {
        return iaiChatMsgService.getUserStatus(userId);
    }

    @PostMapping("/closeTopic")
    public ResponseModel closeTopic(@RequestBody AICreateTopicParam param) {
        return iaiChatMsgService.closeTopic(param.getUserId(), param.getTopicId());
    }

    @PostMapping("/continueTopic")
    public ResponseModel continueTopic(@RequestBody AICreateTopicParam param) {
        return iaiChatMsgService.continueTopic(param.getUserId() );
    }

    @PostMapping("/praiseOrNot")
    public ResponseModel praise(@RequestBody PraiseParam param) {
        return iaiChatMsgService.praise(param.getMessageId(), param.getOperation(), param.getComment());
    }

  /*  @Deprecated
    @PostMapping("/trampleOrNot")
    public ResponseModel trample(@RequestBody AITrampleParam trampleParam) {
        return iaiChatMsgService.trample(trampleParam);
    }*/

    @GetMapping("/feedbackList")
    public ResponseModel feedbackList() {
        return iaiChatMsgService.feedbackList();
    }

    @GetMapping("/commandCenter")
    public ResponseModel commandCenter() {
        return iaiChatMsgService.commandCenter();
    }
}
