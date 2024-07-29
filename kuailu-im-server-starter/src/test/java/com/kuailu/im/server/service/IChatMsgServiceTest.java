package com.kuailu.im.server.service;

import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kuailu.im.core.ImPacket;
import com.kuailu.im.core.packets.ChatType;
import com.kuailu.im.core.utils.JsonKit;
import com.kuailu.im.server.mapper.ChatMsgMapper;
import com.kuailu.im.server.model.entity.ChatGroupMember;
import com.kuailu.im.server.model.entity.ChatMsg;
import com.kuailu.im.server.req.MessageHistoryReqBody;
import com.kuailu.im.server.starter.BaseJunitTest;
import org.junit.Before;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import javax.xml.crypto.Data;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class IChatMsgServiceTest extends BaseJunitTest {

    @Autowired
    IChatMsgService chatMsgService;

    @Autowired
    ChatMsgMapper chatMsgMapper;

    @Test
    void getUserGroupUnReadMsgCount() {
        QueryWrapper<ChatMsg> queryWrapper = new QueryWrapper();
        queryWrapper.last("limit " + 10).orderByDesc("created_time").lambda();

        List<ChatMsg> chatMsgList = chatMsgService.list(queryWrapper);
        for (ChatMsg chatMsg : chatMsgList) {
            System.out.println(chatMsg.getMessageId() + ":" + chatMsg.getCreatedTime().getTime());
        }
    }

    @Test
    void getMessageHistory() {
//        printResult(chatMsgService.getMessageHistory(10, "79ddb97e-8129-40fc-8b1e-931944324dfc", "9cf6acff-5bf5-4c7f-87d5-6d89bec2de21"
//                , new Date().getTime(), ChatType.CHAT_TYPE_PRIVATE.getNumber()));
    }


    @Test
    void getConversationList() {
//        printResult(chatMsgService.getConversationList("a02f5714-0a56-4ecd-a935-9691da6355a2"));
    }

    @Test
    void jsonTest() {
    }


    private ObjectMapper objectMapper;

    @Before
    public void setup() {

    }

    @Test
    public void always() throws JsonProcessingException {
        objectMapper = new ObjectMapper();
    }

    @Test
    public void non_null() throws JsonProcessingException {
    }

    @org.junit.Test
    public void non_absent() throws JsonProcessingException {
        this.objectMapper.setSerializationInclusion(JsonInclude.Include.NON_ABSENT);
        String result = this.objectMapper.writeValueAsString(new JsonTest.Value());

        System.out.println(result);
    }

    @org.junit.Test
    public void non_empty() throws JsonProcessingException {
        this.objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        String result = this.objectMapper.writeValueAsString(new JsonTest.Value());

        System.out.println(result);
    }

    @org.junit.Test
    public void non_default() throws JsonProcessingException {
        this.objectMapper.setSerializationInclusion(JsonInclude.Include.NON_DEFAULT);
        String result = this.objectMapper.writeValueAsString(new JsonTest.Value());

        System.out.println(result);
    }

    @org.junit.Test
    public void custom() throws JsonProcessingException {
        this.objectMapper.setSerializationInclusion(JsonInclude.Include.CUSTOM);
        String result = this.objectMapper.writeValueAsString(new JsonTest.Value());
        System.out.println(result);
    }

    @org.junit.Test
    public void use_defaults() throws JsonProcessingException {
        this.objectMapper.setSerializationInclusion(JsonInclude.Include.USE_DEFAULTS);
        String result = this.objectMapper.writeValueAsString(new JsonTest.Value());
        System.out.println(result);
    }

    @Test
    void ListTest() {
        QueryWrapper<ChatMsg> queryWrapper = new QueryWrapper<ChatMsg>().select("id").groupBy("id");
        List<ChatMsg> groupMemberList = chatMsgService.list(queryWrapper.lambda().eq(ChatMsg::getGroupId, "7b871904-fcb7-4dba-a070-22afdf221573"));
    }

    @Test
    void timestaptest() {
        LambdaQueryWrapper queryWrapper = new QueryWrapper<ChatMsg>().lambda().eq(ChatMsg::getMessageId, "0182128357714361b952eec6cf6c65eb");
        ChatMsg existedMsg = chatMsgService.getOne(queryWrapper);

        existedMsg.setUpdatedTime(new Date());
        chatMsgService.update(existedMsg, new UpdateWrapper<ChatMsg>().eq("message_id", "0182128357714361b952eec6cf6c65eb"));
    }

    @Test
    public void byteTest() {
    }

    @Test
    public void navigate() {
        chatMsgService.navigate("836e896bf92641be8850535d3767befd", "417cfc4a4bce4d0685c8574ee81bca2a", 3);
    }

    @Test
    public void getPrivateChatRecords() {
        chatMsgService.getPrivateChatRecords("869af7dc-89c3-4886-bed4-0129229e298e", "s");
    }

    /**
     * 测试高并发转台下自增id有重复的问题,
     * 还有updateTime自动由数据库生成的问题
     */
    @Test
    public void BatchSave() {
        ChatMsg chatMsg = new ChatMsg.TipBuilder("312", "111", "1333").build();

    }


}