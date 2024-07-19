package com.kuailu.im.server.controller;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.kuailu.im.core.ImStatus;
import com.kuailu.im.core.exception.AppException;
import com.kuailu.im.core.packets.ChatType;
import com.kuailu.im.server.JimServerAPI;
import com.kuailu.im.server.annotation.ApiVersion;
import com.kuailu.im.server.enums.MemberRoleType;
import com.kuailu.im.server.model.ResponseModel;
import com.kuailu.im.server.model.entity.ChatGroupMember;
import com.kuailu.im.server.model.entity.WhiteList;
import com.kuailu.im.server.model.entity.WhiteListMember;
import com.kuailu.im.server.service.*;
import com.kuailu.im.server.util.SpringContextHolder;
import com.kuailu.im.server.vo.ChatGroupDetailVo;
import com.kuailu.im.server.vo.ChatGroupMemberOperationVO;
import com.kuailu.im.server.vo.ChatGroupMemberVo;
import com.kuailu.im.server.vo.ChatGroupVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @description: 群组接口
 */

@Slf4j
@RestController
@RequestMapping("/chatgroup")
public class GroupController {

    @Autowired
    IChatGroupService chatGroupService;

    @Autowired
    IChatGroupMemberService chatGroupMemberService;

    @Autowired
    IConversationService conversationService;

    @Autowired
    IUserAccountService userAccountService;

    @Autowired
    IChatGroupMemberService groupMemberService;

    @Autowired
    IWhiteListMemberService whiteListMemberService;

    @Autowired
    IWhiteListService whiteListService;

    @PostMapping("/saveOrUpdate")
    public ResponseModel saveOrUpdate(@RequestBody ChatGroupVo chatGroupVo) {
        log.info("saveOrUpdate 参数.  {}", JSONUtil.toJsonStr(chatGroupVo));
        ResponseModel model = new ResponseModel();
        List<ChatGroupMemberVo> chatGroupMemberVos = chatGroupVo.getMembers();
        if (CollectionUtils.isEmpty(chatGroupMemberVos)) {
            model.setCode(String.valueOf(ImStatus.ERROR.getCode()));
            return model;
        } else if (ArrayUtil.isEmpty(chatGroupVo.getMembers()) && ArrayUtil.isEmpty(chatGroupVo.getDeptIds())) {
            throw new AppException(ImStatus.INVALID_VERIFICATION.getCode(), "人员或者部门不能全为空");
        }

        model.setCode(String.valueOf(ImStatus.OK.getCode()));
        try {
            ChatGroupDetailVo chatGroup = chatGroupService.saveOrUpdateChatGroup(chatGroupVo);
            String groupId = chatGroup.getGroupId();

            List<String> userIds = chatGroupVo.getMembers().stream().map(ChatGroupMemberVo::getUserId).collect(Collectors.toList());
            ThreadUtil.execAsync(() -> {
                String conversationId = chatGroup.getConversationId();
                //异步绑定新建的群组用户
                JimServerAPI.bindUserListToGroup(userIds, groupId);
                //推送给前端
                conversationService.sendConversation(chatGroup.getConversationName(), groupId, conversationId, chatGroup.getCreatedTime(), chatGroup.getChatType(), chatGroup.getGroupOwner(), chatGroup.getAvatar());
                userAccountService.cleanUserGroupCache(userIds);
                conversationService.cleanUserConversationIdCache(userIds);
                conversationService.cleanContentCache(userIds, conversationId);
                groupMemberService.cleanGroupMemberCache(groupId);
            });
            constructRet(chatGroup, chatGroupVo.getOwner());
            model.setData(chatGroup);
        } catch (AppException passException) {
            model.setMsg(passException.getMessage());
            model.setCode(String.valueOf(passException.getCode()));
        } catch (Exception e) {
            log.error("saveOrUpdate 异常。chatGroupVo：{}", JSONUtil.toJsonStr(chatGroupVo), e);
        }

        return model;
    }

    //todo
    @GetMapping("/details")
    @ApiVersion(("1.8.8"))
    public ResponseModel details8(@RequestParam(value = "groupId") String groupId,
                                  @RequestParam(value = "conversationId") String conversationId,
                                  @RequestParam(value = "userId") String userId) {
        log.info("details 参数.  groupId {},conversationId:{},userId:{}", groupId, conversationId, userId);
        ResponseModel model = new ResponseModel();
        model.setCode(String.valueOf(ImStatus.OK.getCode()));
        ChatGroupDetailVo chatGroupDetailVo = chatGroupService.getDetailsByGroupId(groupId, userId);

        String receiver = conversationService.getReceiver(userId, conversationId);
        int status = whiteListMemberService.inWhiteList(receiver, userId);
        chatGroupDetailVo.setStatus(status + "");
     /*   if (chatGroupDetailVo.getChatType() == ChatType.CHAT_TYPE_PRIVATE.getNumber()) {
            // todo 判断对方是否开启白名单，如对方开启白名单，是否在对方的白名单中
            String receiver = chatGroupMemberService.getOne(new LambdaQueryWrapper<ChatGroupMember>()
                    .eq(ChatGroupMember::getGroupId, groupId)
                    .ne(ChatGroupMember::getUserId, userId)).getUserId();
            if (userId.equals(receiver)) {
                chatGroupDetailVo.setStatus("1");
            } else {
                chatGroupDetailVo.setStatus("1");
                WhiteList whiteList = whiteListService.getOne(new LambdaQueryWrapper<WhiteList>().eq(WhiteList::getUserId, receiver));
                if (whiteList != null) {
                    List<String> userList = whiteListMemberService.list(new LambdaQueryWrapper<WhiteListMember>()
                            .eq(WhiteListMember::getMUserId, receiver)).stream()
                            .map(WhiteListMember::getUserId).collect(Collectors.toList());
                    if (!CollectionUtils.isEmpty(userList)) {
                        if (!userList.contains(userId)) {
                            chatGroupDetailVo.setStatus("0");
                        }
                    } else {
                        chatGroupDetailVo.setStatus("0");
                    }
                }
            }
        }*/
        constructRet(chatGroupDetailVo);
        model.setData(chatGroupDetailVo);
        return model;
    }


    /**
     * 1.3.0版本之前的在用
     *
     * @param groupId
     * @param userId
     * @return
     */
    @GetMapping("/details")
    public ResponseModel details(@RequestParam(value = "groupId") String groupId,
                                 @RequestParam(value = "userId") String userId) {
        log.info("details 参数.  groupId {}", JSONUtil.toJsonStr(groupId));
        ResponseModel model = new ResponseModel();
        model.setCode(String.valueOf(ImStatus.OK.getCode()));
        ChatGroupDetailVo chatGroupDetailVo = chatGroupService.getDetailsByGroupId(groupId);
        if (chatGroupDetailVo.getChatType() == ChatType.CHAT_TYPE_PRIVATE.getNumber()) {
            // todo 判断对方是否开启白名单，如对方开启白名单，是否在对方的白名单中
            String receiver = chatGroupMemberService.getOne(new LambdaQueryWrapper<ChatGroupMember>().eq(ChatGroupMember::getGroupId, groupId).ne(ChatGroupMember::getUserId, userId)).getUserId();
            if (userId.equals(receiver)) {
                chatGroupDetailVo.setStatus("1");
            } else {
                chatGroupDetailVo.setStatus("1");
                WhiteList whiteList = whiteListService.getOne(new LambdaQueryWrapper<WhiteList>().eq(WhiteList::getUserId, receiver));
                if (whiteList != null) {
                    List<String> userList = whiteListMemberService.list(new LambdaQueryWrapper<WhiteListMember>().eq(WhiteListMember::getMUserId, receiver)).stream().map(WhiteListMember::getUserId).collect(Collectors.toList());
                    if (!CollectionUtils.isEmpty(userList)) {
                        if (!userList.contains(userId)) {
                            chatGroupDetailVo.setStatus("0");
                        }
                    } else {
                        chatGroupDetailVo.setStatus("0");
                    }
                }
            }
        }
        if (chatGroupDetailVo == null) {
            model.setCode(String.valueOf(ImStatus.CANNOT_FIND_DATA.getCode()));
            model.setMsg(ImStatus.CANNOT_FIND_DATA.getMsg());
        } else {
            constructRet(chatGroupDetailVo);
            model.setData(chatGroupDetailVo);
        }
        return model;
    }


    /**
     * 批量邀请群成员
     *
     * @return
     * @throws Exception
     */
    @PostMapping("/members")
    public ResponseModel members(@RequestBody ChatGroupMemberOperationVO operationVO) {
        log.info("members 参数.  {}", JSONUtil.toJsonStr(operationVO));
        ResponseModel model = new ResponseModel();
        model.setCode(String.valueOf(ImStatus.OK.getCode()));
        String groupId = operationVO.getGroupId();
        try {
//            String updateUser = operationVO.getUpdateUser();
//            List<ChatGroupMemberVo> chatGroupMemberVos = operationVO.getMembers();
//            Iterator<ChatGroupMemberVo> iterator = chatGroupMemberVos.iterator();
          /*  while (iterator.hasNext()) {
                ChatGroupMemberVo chatGroupMemberVo = iterator.next();
                WhiteList whiteList = whiteListService.getOne(new LambdaQueryWrapper<WhiteList>().eq(WhiteList::getUserId, chatGroupMemberVo.getUserId()));
                if (whiteList != null) {
                    WhiteListMember whiteListMember = whiteListMemberService.getOne(new LambdaQueryWrapper<WhiteListMember>().eq(WhiteListMember::getMUserId, chatGroupMemberVo.getUserId()).eq(WhiteListMember::getUserId, updateUser));
                    if (whiteListMember == null) {
                        iterator.remove();
                    }
                }
            }
            if (CollectionUtils.isEmpty(chatGroupMemberVos)) {
                model.setCode(String.valueOf(ImStatus.CAN_NOT_INVITE_GROUP.getCode()));
                model.setMsg("您不在对方的白名单中，不可邀请入群。");
                return model;
            }*/

//            operationVO.setMembers(chatGroupMemberVos);
            ChatGroupDetailVo chatGroupDetailVo = chatGroupService.memberInviteChatGroup(operationVO);
            List<String> userIds = operationVO.getMembers().stream().map(ChatGroupMemberVo::getUserId).collect(Collectors.toList());
            ThreadUtil.execAsync(() -> {
                JimServerAPI.bindUserListToGroup(userIds, groupId);
                conversationService.sendConversation(chatGroupDetailVo.getConversationName(), groupId, chatGroupDetailVo.getConversationId(), chatGroupDetailVo.getCreatedTime(), chatGroupDetailVo.getChatType(), chatGroupDetailVo.getGroupOwner(), chatGroupDetailVo.getAvatar());
            });
            model.setData(chatGroupDetailVo);
        } catch (Exception e) {
            log.error("saveOrUpdate 异常。chatGroupVo：{}", JSONUtil.toJsonStr(operationVO), e);
        }
        return model;
    }

    /**
     * 批量剔除群成员
     *
     * @return
     * @throws Exception
     */
    @PostMapping("/members/remove")
    public ResponseModel memberRemoveChatGroup(@RequestBody ChatGroupMemberOperationVO operationVO) {
        log.info("memberRemoveChatGroup 参数.  {}", JSONUtil.toJsonStr(operationVO));
        ResponseModel model = new ResponseModel();
        model.setCode(String.valueOf(ImStatus.OK.getCode()));
        String groupId = operationVO.getGroupId();
        List<ChatGroupMemberVo> removedMembers = operationVO.getMembers();

        if (CollectionUtils.isEmpty(removedMembers) || removedMembers.size() == 0) {
            model.error("移除成员不能为空");
            return model;
        }

        List<String> userList = removedMembers.stream().map(ChatGroupMemberVo::getUserId).collect(Collectors.toList());
        try {
            chatGroupService.memberRemoveChatGroup(groupId, userList);
            userAccountService.cleanUserGroupCache(userList);
            conversationService.cleanUserConversationIdCache(userList);
            groupMemberService.cleanGroupMemberCache(groupId);
        } catch (AppException e) {
            log.error("参数异常operationVO:{}", JSONUtil.toJsonStr(operationVO), e);
            model.error("移除失败，请稍后再试");
        }
        return model;
    }


    /**
     * 组装返回数据
     */
    private void constructRet(ChatGroupDetailVo chatGroupDetailVo, String ownerUserId) {
        String groupId = chatGroupDetailVo.getGroupId();
        if (ChatType.CHAT_TYPE_PUBLIC.getNumber() == chatGroupDetailVo.getChatType()) {
            chatGroupDetailVo.setReceiver(groupId);
            chatGroupDetailVo.setConversationName(chatGroupDetailVo.getGroupName());
        } else {
//            todo 低频调用，可以不走缓存
            List<ChatGroupMember> allMembers = groupMemberService.getAllMembers(groupId);
            if (CollectionUtil.isEmpty(allMembers) || allMembers.size() != 2) {
                log.error("群缓存数据有误 chatGroupDetailVo{}  ownerUserId：{} 。直接从数据库查", JSONUtil.toJsonStr(chatGroupDetailVo),ownerUserId);
                groupMemberService.cleanGroupMemberCache(groupId);
                allMembers = groupMemberService.list(new QueryWrapper<ChatGroupMember>()
                        .select("group_id", "user_id", "user_name", "user_no", "role_type", "created_time")
                        .lambda()
                        .eq(ChatGroupMember::getGroupId, groupId));
            }
            //有可能自己跟自己聊天
            ChatGroupMember owner = allMembers.stream().filter(item -> ownerUserId.equals(item.getUserId())).findAny().get();
            int ownnerIndex = allMembers.indexOf(owner);
            ChatGroupMember member = allMembers.get(1 - ownnerIndex);
            if (ownerUserId.equals(member.getUserId())) {
                //自己跟自己聊天
                chatGroupDetailVo.setReceiver(ownerUserId);
                chatGroupDetailVo.setConversationName(member.getUserName());
            } else {
                chatGroupDetailVo.setReceiver(member.getUserId());
                chatGroupDetailVo.setConversationName(member.getUserName());
            }
        }

    }

    /**
     * 组装返回数据
     *
     * @param chatGroupDetailVo
     */
    private void constructRet(ChatGroupDetailVo chatGroupDetailVo) {
        String groupId = chatGroupDetailVo.getGroupId();
        if (ChatType.CHAT_TYPE_PUBLIC.getNumber() == chatGroupDetailVo.getChatType()) {
            chatGroupDetailVo.setReceiver(groupId);
            chatGroupDetailVo.setConversationName(chatGroupDetailVo.getGroupName());
        } else {
            List<ChatGroupMember> allMembers = groupMemberService.getAllMembers(groupId);
            if (CollectionUtil.isEmpty(allMembers) || allMembers.size() != 2) {
                log.error("群数据有误 chatGroupDetailVo{}.直接从数据库查", JSONUtil.toJsonStr(chatGroupDetailVo));
                groupMemberService.cleanGroupMemberCache(groupId);
                allMembers = groupMemberService.list(new QueryWrapper<ChatGroupMember>()
                        .select("group_id", "user_id", "user_name", "user_no", "role_type", "created_time")
                        .lambda()
                        .eq(ChatGroupMember::getGroupId, groupId));
            }
            //有可能自己跟自己聊天
            ChatGroupMember owner = allMembers.stream().filter(item -> MemberRoleType.OWNER.getCode().equals(item.getRoleType())).findAny().get();
            ChatGroupMember member = allMembers.stream().filter(item -> MemberRoleType.MEMBER.getCode().equals(item.getRoleType())).findAny().get();
            if (owner.getUserId().equals(member.getUserId())) {
                //自己跟自己聊天
                chatGroupDetailVo.setReceiver(owner.getUserId());
                chatGroupDetailVo.setConversationName(owner.getUserName());
            } else {
                chatGroupDetailVo.setReceiver(member.getUserId());
                chatGroupDetailVo.setConversationName(member.getUserName());
            }
        }

    }

}
