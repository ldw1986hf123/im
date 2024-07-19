package com.kuailu.im.server.mapper;

import com.kuailu.im.server.enums.MemberRoleType;
import com.kuailu.im.server.model.entity.ChatGroupMember;
import com.kuailu.im.server.starter.BaseJunitTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ChatGroupMemberMapperTest extends BaseJunitTest {

    @Autowired
    ChatGroupMemberMapper chatGroupMemberMapper;

    @Test
    void getUserChatGroup() {
    }

    @Test
    void getPrivateGroup() {
    }

    @Test
    void saveOrUpdateBatch() {
        ChatGroupMember groupMember=new ChatGroupMember();
        groupMember.setGroupId("grpupId1");
        groupMember.setUserId("411");
        groupMember.setUserNo("1asd");
        groupMember.setUserName("1ad");
        groupMember.setCreatedBy("1");
        groupMember.setCreatedTime(new Date());
        groupMember.setRoleType(MemberRoleType.MEMBER.getCode());


        ChatGroupMember groupMember2=new ChatGroupMember();
        groupMember2.setGroupId("grpupId1");
        groupMember2.setUserId("21");
        groupMember2.setUserNo("2");
        groupMember2.setUserName("2");
        groupMember2.setCreatedBy("2");
        groupMember2.setCreatedBy("2");
        groupMember2.setCreatedTime(new Date());
        groupMember2.setRoleType(MemberRoleType.MEMBER.getCode());

        List<ChatGroupMember> groupMemberList=new ArrayList<>();
        groupMemberList.add(groupMember);
        groupMemberList.add(groupMember2);

    }
}