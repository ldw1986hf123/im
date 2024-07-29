package com.kuailu.im.server.command.handler;

import com.kuailu.im.core.ImChannelContext;
import com.kuailu.im.core.ImPacket;
import com.kuailu.im.core.ImStatus;
import com.kuailu.im.core.exception.ImException;
import com.kuailu.im.core.packets.*;
import com.kuailu.im.core.utils.JsonKit;
import com.kuailu.im.server.ImServerChannelContext;
import com.kuailu.im.server.JimServerAPI;
import com.kuailu.im.server.command.AbstractCmdHandler;
import com.kuailu.im.server.constant.CLIENT_TYPE;
import com.kuailu.im.server.constant.RedisCacheKey;
import com.kuailu.im.server.dto.GroupCacheDto;
import com.kuailu.im.server.dto.UserCacheDto;
import com.kuailu.im.server.model.entity.UserAccount;
import com.kuailu.im.server.protocol.ProtocolManager;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 登录消息命令处理器
 */
@Slf4j
public class LoginReqHandler extends AbstractCmdHandler {

    @Override
    public ImPacket handler(ImPacket packet, ImChannelContext imChannelContext) throws ImException {
        ImServerChannelContext imServerChannelContext = (ImServerChannelContext) imChannelContext;
        LoginReqBody loginReqBody = JsonKit.toBean(packet.getBody(), LoginReqBody.class);
        ImPacket loginSuccessPacket = new ImPacket(loginReqBody.getCmd());

        String seid = loginReqBody.getSeid();
        String currentUserId = loginReqBody.getUserId();
        if (StringUtils.isEmpty(seid) || StringUtils.isEmpty(currentUserId)) {
            log.error("登录参数seid：{}，currentUserId：{} 不能为空", seid, currentUserId);
            JimServerAPI.send(imServerChannelContext, ProtocolManager.Converter.respPacket(LoginRespBody.failed(), imChannelContext));
            return null;
        }

        if (StringUtils.isNotEmpty(imChannelContext.getUserId()) && userAccountService.isOnLine(imChannelContext.getUserId())) {
            log.info("已经登录过，无需重新登录：currentUserId: {}，seId:{} ", imChannelContext.getUserId(), seid);
            UserCacheDto userCacheDto = redisService.getHashValue(RedisCacheKey.ONLINE_USER_CACHE + currentUserId, UserCacheDto.class);
            RespBody resPacket = new RespBody(Command.forNumber(loginReqBody.getCmd()), ImStatus.OK, userCacheDto);
            loginSuccessPacket.setBody(resPacket.toByte());
            JimServerAPI.send(imServerChannelContext, loginSuccessPacket);
            return null;
        }

        UserDto currentUser = null;
        UserAccount loginUserAccount = null;
        try {
            loginUserAccount = userAccountService.getByUserIdLogin(seid, currentUserId);
            currentUser = UserDto.builder()
                    .userId(loginUserAccount.getUserId())
                    .userName(loginUserAccount.getUserName())
                    .userNo(loginUserAccount.getUserNo())
                    .avatar(userAccountService.getAvatarUrl(currentUserId, seid))
                    .build();
            loginUserAccount.setSeid(seid);
            loginUserAccount.setClientType(CLIENT_TYPE.APP);
        } catch (Exception e) {
            log.error("登录组装用户失败。currentUserId:{}", currentUserId, e);
            RespBody resPacket = new RespBody(Command.forNumber(loginReqBody.getCmd()), ImStatus.NEED_LOGIN);
            resPacket.setMsg("Pass服务获取不到该用户信息,登录失败");
            loginSuccessPacket.setBody(resPacket.toByte());
            JimServerAPI.send(imServerChannelContext, loginSuccessPacket);
            return null;
        }

        /********************************登录成功******************************************/
        log.info("登录成功：currentUserId: {}，seId:{}, username: {},对方主机：{}", currentUserId, seid, currentUser.getUserName(), imChannelContext.getClientNode().toString());
        imChannelContext.setUserId(currentUserId);
        JimServerAPI.bindUser(imServerChannelContext, currentUser);

        UserCacheDto userCacheDto = userAccountService.cacheUserInLogin(loginUserAccount);
        RespBody resPacket = new RespBody(Command.forNumber(loginReqBody.getCmd()), ImStatus.OK, userCacheDto);
        loginSuccessPacket.setBody(resPacket.toByte());
        JimServerAPI.send(imServerChannelContext, loginSuccessPacket);

        //绑定所属群组
        List<GroupCacheDto> groupCacheDtoList = groupMemberService.cacheLoginUserGroups(currentUserId);
        if (CollectionUtils.isNotEmpty(groupCacheDtoList)) {
            List<String> groupIdList = groupCacheDtoList.stream().map(GroupCacheDto::getGroupId).collect(Collectors.toList());
            iChatGroupService.initBindUserGroup(currentUserId, groupIdList);
        }
        userAccountService.updateByLogin(loginUserAccount);
        //初始化消息助手会话
        userAccountService.initMsgHelper(currentUserId);
        return null;
    }

    @Override
    public Command command() {
        return Command.COMMAND_LOGIN_REQ;
    }
}
