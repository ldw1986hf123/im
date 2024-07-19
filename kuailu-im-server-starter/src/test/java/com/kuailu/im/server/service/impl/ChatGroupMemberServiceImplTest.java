package com.kuailu.im.server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.kuailu.im.core.packets.GroupDto;
import com.kuailu.im.server.dto.GroupCacheDto;
import com.kuailu.im.server.enums.MemberRoleType;
import com.kuailu.im.server.mapper.ChatGroupMemberMapper;
import com.kuailu.im.server.model.entity.ChatGroupMember;
import com.kuailu.im.server.service.IChatGroupMemberService;
import com.kuailu.im.server.starter.BaseJunitTest;
import com.kuailu.im.server.util.RedisService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

class ChatGroupMemberServiceImplTest extends BaseJunitTest {

    @Autowired
    ChatGroupMemberMapper groupMemberMapper;
    @Autowired
    IChatGroupMemberService groupMemberService;


    String groupId = "99967f9fd5f44a1bb5ebd468297cb34a";
    String key = "groupMemver_list_" + groupId;
    @Autowired
    RedisService redisService;

    @Autowired
    RedisTemplate redisTemplate;


    @Test
    void queryChatGroupByUserId() {
    }

    @Test
    void getPrivateGroup() {
    }

    @Test
    void getNewGroupName() {
    }

    @Test
    void getPrivateChatterMember() {
    }

    @Test
    void getAllMembers() {
            String originalString = "This is a test string";
            byte[] originalBytes = originalString.getBytes(StandardCharsets.ISO_8859_1);

            // encode to string
            String encodedString = Base64.getEncoder().encodeToString(originalBytes);

            //decode back to byte[]
            byte[] decodedBytes = Base64.getDecoder().decode(encodedString.getBytes(StandardCharsets.UTF_8));

            String decodedString = new String(decodedBytes, StandardCharsets.UTF_8);

            System.out.println("Original String: " + originalString);
            System.out.println("Encoded String : " + encodedString);
            System.out.println("Decoded Bytes  : " + decodedBytes);
            System.out.println("Decoded String : " + decodedString);

    }

    @Test
    void getAllJoinGroupId() {
//        List<ChatGroupMember> groupMemberList = groupMemberService.getAllJoinGroupId("889316eb-0b5e-4f73-a1b5-b412212b32c7bfdf");
       /* groupMemberList.forEach(row -> {
            String groupId = row.getGroupId();
            GroupDto groupDto = new GroupDto();
        });*/
    }

    @Test
    void getMemberByGroupId() {
        List<String> groupIds = new ArrayList<>();
        groupIds.add("1199a9cad4a746e5b42b06be05e9ccf4");
        groupIds.add("a6fcc8dfe22245ce85ff8e09607a05d4");


        List<ChatGroupMember> resultList = groupMemberService.list(
                new QueryWrapper<ChatGroupMember>().select("user_id,group_id").groupBy("user_id,group_id").in("group_id", groupIds));
        Map<String, List<ChatGroupMember>> grouped = resultList.stream()
                .collect(Collectors.groupingBy(ChatGroupMember::getGroupId,
                        Collectors.mapping(Function.identity(), Collectors.toList())));
        printResult(grouped);
    }

    @Test
    public void addgroupMemberCachetest() {
        LambdaQueryWrapper queryWrapper = new QueryWrapper<ChatGroupMember>().lambda().eq(ChatGroupMember::getGroupId, groupId);
        List<ChatGroupMember> groupMemberList = groupMemberService.list(queryWrapper);
        for (ChatGroupMember chatGroupMember : groupMemberList) {
            redisTemplate.opsForHash().put(key, chatGroupMember.getUserId(), chatGroupMember);
        }
//        redisTemplate.expire(key, 1, TimeUnit.MINUTES);
    }

    @Test
    public void deletegroupMemberCachetest() {
        redisTemplate.opsForHash().delete(key, "565f4bfc-1b50-46bb-a80f-bd289e6e27fe");
        ChatGroupMember user = (ChatGroupMember) redisTemplate.opsForHash().get(key, "565f4bfc-1b50-46bb-a80f-bd289e6e27fe");
        System.out.println(user);
    }

    @Test
    public void put() {
        ChatGroupMember groupMember = new ChatGroupMember();
        groupMember.setGroupId("1");
        groupMember.setUserId("1");
        groupMember.setUserName("1");
        groupMember.setRoleType(MemberRoleType.MEMBER.getCode());
        groupMember.setCreatedTime(new Date());
        groupMember.setGroupId("grpupId1");


        ChatGroupMember groupMember2 = new ChatGroupMember();
        groupMember2.setGroupId("2");
        groupMember2.setUserId("2");
        groupMember2.setCreatedTime(new Date());
        groupMember2.setUserName("2");
        groupMember2.setRoleType(MemberRoleType.MEMBER.getCode());
        groupMember2.setGroupId("grpupId2");

        List<ChatGroupMember> groupMemberList = new ArrayList<>();
        groupMemberList.add(groupMember);
        groupMemberList.add(groupMember2);

        groupMemberService.saveOrUpdateBatch(groupMemberList);


    }

    @Test
    public void getUserGroups() {
        List<GroupCacheDto> list = groupMemberService.getUserGroups("f0758101-b742-4baf-81af-57c36a6fa3fb");
        printResult(list);
    }


    @Test
    public void getMemberCachetest() {
        ChatGroupMember user = (ChatGroupMember) redisTemplate.opsForHash().get(key, "71af054e-e005-40a8-8710-bc2bb2909e02");
        System.out.println(user);
    }

}