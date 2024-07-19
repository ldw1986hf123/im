package com.kuailu.im.server.service.impl;

import com.kuailu.im.server.service.IChatGroupService;
import com.kuailu.im.server.starter.BaseJunitTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class ChatGroupServiceImplTest extends BaseJunitTest {

    @Autowired
    IChatGroupService groupService;

    @Test
    void saveOrUpdateChatGroup() {
    }

    @Test
    void getDetailsByGroupId() {
        groupService.getDetailsByGroupId("838bf22d0d0749268258f028ed2251f9","869af7dc-89c3-4886-bed4-0129229e298e");
    }

    @Test
    void memberInviteChatGroup() {
    }

    @Test
    void getChatGroupResps() {
        groupService.getChatGroupResps("838bf22d0d0749268258f028ed2251f9","869af7dc-89c3-4886-bed4-0129229e298e");
    }



    @Test
    void saveGroupCache() {

        List<String> list = new ArrayList<>();
        list.add("0");
        list.add("1");
        list.add("2");
        list.add("3");
        list.add("4");
        list.add("5");
        list.add("6");
        list.add("7");
        list.add("8");
        list.add("9");

        list.add("0");
        list.add("打");
        list.add("打");
        list.add("打");
        list.add("a");
        list.add("b");
        list.add("6");
        list.add("7");
        list.add("8");
        list.add("9");

        list.add("0");
        list.add("1");
        list.add("2");
        list.add("3");
        list.add("4");
        list.add("5");
        list.add("6");
        list.add("7");
        list.add("8");
        list.add("9");

        String groupName = String.join(",", list);
        if (groupName.length() > 30) {
            groupName = groupName.substring(0, 30);
        }
        System.out.println(groupName);
    }
}