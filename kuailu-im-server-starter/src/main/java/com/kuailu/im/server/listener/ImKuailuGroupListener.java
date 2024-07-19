package com.kuailu.im.server.listener;

import com.kuailu.im.core.ImChannelContext;
import com.kuailu.im.core.ImPacket;
import com.kuailu.im.core.exception.ImException;
import com.kuailu.im.core.packets.*;
import com.kuailu.im.server.JimServerAPI;
import lombok.extern.slf4j.Slf4j;

/**
 * @description:
 * @author: 林坚丁
 * @time: 2022/11/30 18:38
 */
@Slf4j
public class ImKuailuGroupListener extends AbstractImGroupListener {
    @Override
    public void doAfterBind(ImChannelContext imChannelContext, GroupDto group) throws ImException {
        log.debug("用户id：{} ,群组id:{},群名:{},绑定成功!", imChannelContext.getUserId(), group.getGroupId(), group.getGroupName());
//        JoinGroupRespBody joinGroupRespBody = JoinGroupRespBody.success();
        //回一条消息，告诉对方进群结果
//        joinGroupRespBody.setGroup(group.getGroupId());
//        ImPacket respPacket = ProtocolManager.Converter.respPacket(joinGroupRespBody, imChannelContext);
        //Jim.send(imChannelContext, respPacket);
        //发送进房间通知;
//        joinGroupNotify(group, imChannelContext);
    }

    /**
     * @param imChannelContext
     * @param group
     * @throws Exception
     * @author: WChao
     */
    @Override
    public void doAfterUnbind(ImChannelContext imChannelContext, GroupDto group) throws ImException {
        //发退出房间通知  COMMAND_EXIT_GROUP_NOTIFY_RESP
        ExitGroupNotifyRespBody exitGroupNotifyRespBody = new ExitGroupNotifyRespBody();
        exitGroupNotifyRespBody.setGroup(group.getGroupId());
        UserDto clientUser = imChannelContext.getSessionContext().getImClientNode().getUser();
        if (clientUser == null) {
            return;
        }
        UserDto notifyUser = UserDto.builder().userId(clientUser.getUserId()).userName(clientUser.getUserName()).build();
        exitGroupNotifyRespBody.setUser(notifyUser);

        RespBody respBody = new RespBody(Command.COMMAND_EXIT_GROUP_NOTIFY_RESP, exitGroupNotifyRespBody);
        ImPacket imPacket = new ImPacket(Command.COMMAND_EXIT_GROUP_NOTIFY_RESP, respBody.toByte());
        JimServerAPI.sendToGroup(group.getGroupId(), imPacket);
    }

    /**
     * 发送进房间通知;
     *
     * @param group            群组对象
     * @param imChannelContext
     */
  /*  @Deprecated
    public void joinGroupNotify(GroupDto group, ImChannelContext imChannelContext) throws ImException {
        ImSessionContext imSessionContext = imChannelContext.getSessionContext();
        UserDto clientUser = imSessionContext.getImClientNode().getUser();
        UserDto notifyUser = UserDto.builder().userId(clientUser.getUserId()).userName(clientUser.getUserName()).status(UserStatusType.ONLINE.getStatus()).build();
        ImServerConfig imServerConfig = ImConfig.Global.get();
        MessageHelper messageHelper = imServerConfig.getMessageHelper();
        String groupId = group.getGroupId();
        //发进房间通知  COMMAND_JOIN_GROUP_NOTIFY_RESP
        JoinGroupNotifyRespBody joinGroupNotifyRespBody = JoinGroupNotifyRespBody.success();
        joinGroupNotifyRespBody.setGroup(groupId).setUser(notifyUser);
        GroupDto cacheGroup = messageHelper.getGroupUsers(groupId, 2);
        joinGroupNotifyRespBody.setGroupName(cacheGroup.getGroupName());
        joinGroupNotifyRespBody.setGroupOwner(cacheGroup.getGroupOwner());
        joinGroupNotifyRespBody.setChatType(cacheGroup.getChatType());
        joinGroupNotifyRespBody.setCreateTime(cacheGroup.getCreatedTime());
        if (cacheGroup.getChatType() == ChatType.CHAT_TYPE_PRIVATE.getNumber()) {
            for (UserDto userItem : cacheGroup.getUsers()) {
                if (userItem.getUserId().equals(cacheGroup.getGroupOwner())) {
                    joinGroupNotifyRespBody.setGroupName(userItem.getUserName());
                    break;
                }
            }
        }
        JimServerAPI.sendToGroup(groupId, ProtocolManager.Converter.respPacket(joinGroupNotifyRespBody, imChannelContext));
    }*/
}
