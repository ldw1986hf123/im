package com.kuailu.im.server.service.impl;

import com.kuailu.im.core.utils.HttpUtil;
import com.kuailu.im.server.dto.GroupCacheDto;
import com.kuailu.im.server.dto.UserCacheDto;
import com.kuailu.im.server.model.entity.Conversation;
import com.kuailu.im.server.service.IConversationService;
import com.kuailu.im.server.starter.BaseJunitTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class ConversationServiceImplTest extends BaseJunitTest {

    @Value("${kuailu.apiUrl}")
    String kuailuApiUrl;


    @Autowired
    IConversationService conversationService;

    @Test
    void getConversationListBySender() {
    }

    @Test
    void getConversationListByGroupId() {

    }

    @Test
    void getCurrentUserConversations() {
        Conversation conversation = new Conversation();
        conversation.setConversationId("123");
        conversation.setUserId("12345");
        conversation.setChatgroupId("aa");
        conversation.setChatType(1);
        conversationService.save(conversation);
    }

    @Test
    void builder() {
        conversationService.getTotalUnReadMsgCount("c9f9e947-d8e8-44be-a7c8-2559d7418fff");
    }

    @Test
    public void getMsgHelperUnReadCount(){
        String url = kuailuApiUrl + "j" + "?" + "appid=com.kuailu.app.notification&method=getUnReadNumber&seid=" + "3d943ed5-39ae-4b3c-b0a5-dcfadd6e62ce";
        String strResp = HttpUtil.doPostBody(url,"");
        printResult(strResp);
    }

}