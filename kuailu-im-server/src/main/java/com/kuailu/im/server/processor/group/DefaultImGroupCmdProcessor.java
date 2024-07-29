package com.kuailu.im.server.processor.group;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.kuailu.im.core.ImChannelContext;
import com.kuailu.im.core.packets.GroupDto;
import com.kuailu.im.core.packets.JoinGroupRespBody;
import com.kuailu.im.core.packets.Message;
import com.kuailu.im.server.model.entity.ChatGroup;
import com.kuailu.im.server.service.IChatGroupService;
import com.kuailu.im.server.util.ApplicationContextHelper;

/**
 * @description:
 * @author: 林坚丁
 * @time: 2022/12/12 13:55
 */
public class DefaultImGroupCmdProcessor  implements GroupCmdProcessor{
    @Override
    public void process(ImChannelContext imChannelContext, Message message) {
        // do nothing
    }
    @Override
    public JoinGroupRespBody join(GroupDto joinGroup, ImChannelContext imChannelContext) {
        IChatGroupService chatGroupService = ApplicationContextHelper.get().getBean(IChatGroupService.class);
        QueryWrapper<ChatGroup> chatGroupQueryWrapper=new QueryWrapper<>();
        chatGroupQueryWrapper.lambda().eq(ChatGroup::getGroupId, joinGroup.getGroupId());
        ChatGroup chatGroup=chatGroupService.getOne(chatGroupQueryWrapper);
        if(chatGroup!=null){
            joinGroup.setChatType(chatGroup.getChatType());
            JoinGroupRespBody respBody = JoinGroupRespBody.success();
            respBody.setGroup(joinGroup.getGroupId());
            respBody.setChatType(chatGroup.getChatType());
            respBody.setConversationId(joinGroup.getGroupId());
            return respBody;
        }

        return  JoinGroupRespBody.failed();

    }
}
