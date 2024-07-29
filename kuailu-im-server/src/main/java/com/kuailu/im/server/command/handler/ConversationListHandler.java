
package com.kuailu.im.server.command.handler;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONUtil;
import com.kuailu.im.core.ImChannelContext;
import com.kuailu.im.core.ImPacket;
import com.kuailu.im.core.ImStatus;
import com.kuailu.im.core.exception.ImException;
import com.kuailu.im.core.packets.ChatType;
import com.kuailu.im.core.packets.Command;
import com.kuailu.im.core.packets.ConversationListReq;
import com.kuailu.im.core.packets.RespBody;
import com.kuailu.im.core.utils.JsonKit;
import com.kuailu.im.server.command.AbstractCmdHandler;
import com.kuailu.im.server.constant.RedisCacheKey;
import com.kuailu.im.server.dto.ConversationCacheDto;
import com.kuailu.im.server.dto.GroupCacheDto;
import com.kuailu.im.server.model.entity.ChatGroupMember;
import com.kuailu.im.server.model.entity.ChatMsg;
import com.kuailu.im.server.model.entity.NoDisturb;
import com.kuailu.im.server.protocol.ProtocolManager;
import com.kuailu.im.server.req.ChatReqParam;
import com.kuailu.im.server.req.MessageBody;
import com.kuailu.im.server.response.ConversationDto;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 会话列表
 */
@Slf4j
public class ConversationListHandler extends AbstractCmdHandler {

    @Override
    public Command command() {
        return Command.CONVERSATION_LIST;
    }

    @Override
    public ImPacket handler(ImPacket packet, ImChannelContext imChannelContext) throws ImException {
        ConversationListReq sessionListReq = JsonKit.toBean(packet.getBody(), ConversationListReq.class);
        List<ConversationDto> conversationDtoList = new ArrayList<>();
        String currentUserId = imChannelContext.getUserId();
        Integer cmd=sessionListReq.getCmd();
        String seid=sessionListReq.getSeid();

        List<GroupCacheDto> groupCacheDtoList = groupMemberService.getUserGroups(currentUserId);

        if (CollectionUtils.isEmpty(groupCacheDtoList)) {
            log.info("userId :{} 会话为空", currentUserId);
            RespBody resPacket = new RespBody(Command.forNumber(cmd), ImStatus.OK, conversationDtoList);
            return ProtocolManager.Converter.respPacket(resPacket, imChannelContext);
        }
        List<ConversationCacheDto> conversationList = new ArrayList<>();
        try {
            List<String> groupIdList = groupCacheDtoList.stream().map(GroupCacheDto::getGroupId).collect(Collectors.toList());
            conversationList = conversationService.getCurrentUserConversations(groupIdList, currentUserId,seid);

            for (ConversationCacheDto conversationCacheDto : conversationList) {
                String singleGroupId = conversationCacheDto.getGroupId();
                Integer chatType = conversationCacheDto.getChatType();
                ConversationDto conversationDto = new ConversationDto();

                ChatReqParam chatBody = new ChatReqParam();
                ChatMsg lastMessage = conversationCacheDto.getLastMsg();
                if (null != lastMessage) {
                    chatBody = BeanUtil.copyProperties(lastMessage, ChatReqParam.class);
                    if (StringUtils.isNotEmpty(lastMessage.getMsg())) {
                        chatBody.setMessageBody(JSONUtil.toBean(lastMessage.getMsg(), MessageBody.class));
                    }
                    chatBody.setId(lastMessage.getMessageId());
                    chatBody.setCreatedTime(lastMessage.getCreatedTime().getTime());
                    chatBody.setMsgType(lastMessage.getMsgType());
                }

                conversationDto.setLastMsg(chatBody);
                conversationDto.setGroupId(singleGroupId);
                conversationDto.setConversationId(conversationCacheDto.getConversationId());
                conversationDto.setChatType(chatType);
                conversationDto.setLastAtAvatar(conversationCacheDto.getLastAtAvatar());
                conversationDto.setAvatar(conversationCacheDto.getAvatar());
                conversationDto.setCreateTime(conversationCacheDto.getCreatedTime());
                conversationDto.setGroupOwner(conversationCacheDto.getGroupOwner());
                conversationDto.setUnReaderMsgCount(conversationCacheDto.getUnReaderMsgCount());
                conversationDto.setDisturbType(conversationCacheDto.getDisturbType());
                conversationDto.setConversationName(conversationCacheDto.getConversationName());
                conversationDto.setReceiver(conversationCacheDto.getReceiver());
                conversationDtoList.add(conversationDto);
            }

            RespBody resPacket = new RespBody(Command.forNumber(cmd), ImStatus.OK, conversationDtoList);
            return ProtocolManager.Converter.respPacket(resPacket, imChannelContext);
        } catch (Exception e) {
            log.error("获取回话列表异常。sessionListReq：{},userId  {}", JSONUtil.toJsonStr(sessionListReq), currentUserId, e);
            groupMemberService.cleanUserUserGroupCache(currentUserId);
            List<String> conversationIdList = conversationList.stream().map(ConversationCacheDto::getConversationId).collect(Collectors.toList());
            conversationService.cleanUserAllConversationContent(conversationIdList, currentUserId);
            log.info("清除会话 群缓存  conversationIdList{},userId:{}", JSONUtil.toJsonStr(conversationIdList), currentUserId);
        }
        RespBody resPacket = new RespBody(Command.forNumber(cmd), ImStatus.ERROR);
        return ProtocolManager.Converter.respPacket(resPacket, imChannelContext);
    }

}
