package com.kuailu.im.server.command.handler;

import cn.hutool.json.JSONUtil;
import com.kuailu.im.core.ImChannelContext;
import com.kuailu.im.core.ImPacket;
import com.kuailu.im.core.ImStatus;
import com.kuailu.im.core.exception.ImException;
import com.kuailu.im.core.packets.CloseReqBody;
import com.kuailu.im.core.packets.Command;
import com.kuailu.im.core.packets.LoginRespBody;
import com.kuailu.im.core.packets.RespBody;
import com.kuailu.im.core.utils.JsonKit;
import com.kuailu.im.server.JimServerAPI;
import com.kuailu.im.server.command.AbstractCmdHandler;

import com.kuailu.im.server.protocol.ProtocolManager;
import com.kuailu.im.server.req.MessageHistoryReqBody;
import com.kuailu.im.server.service.IUserAccountService;
import com.kuailu.im.server.util.ApplicationContextHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

/**
 * 版本: [1.0]
 */
@Slf4j
public class CloseReqHandler extends AbstractCmdHandler {
    @Override
    public ImPacket handler(ImPacket packet, ImChannelContext imChannelContext) throws ImException {
        CloseReqBody closeReqBody = JsonKit.toBean(packet.getBody(), CloseReqBody.class);
        log.info("closeReqBody:{}，收到关闭ws请求", JSONUtil.toJsonStr(closeReqBody));
        String userId = "";
        if (null == closeReqBody) {
            userId = imChannelContext.getUserId();
            if (StringUtils.isNotEmpty(userId)) {
                log.info("userId:{}，收到关闭ws请求", userId);
                JimServerAPI.remove(userId, "收到关闭请求!");
            }
        } else {
            Integer command = closeReqBody.getCmd();
            RespBody respBody = new RespBody(command);
            if (!validateChatRqePara(closeReqBody, imChannelContext, respBody)) {
                return ProtocolManager.Converter.respPacket(respBody, imChannelContext);
            }
            userId = closeReqBody.getUserId();
            JimServerAPI.bSend(imChannelContext, ProtocolManager.Converter.respPacket(new RespBody(command, ImStatus.C10021), imChannelContext));
            JimServerAPI.remove(userId, "收到关闭请求!");
        }
        JimServerAPI.remove(imChannelContext, "收到关闭请求");
        userAccountService.logout(userId);
        log.info("userId:{} 成功退出登录", userId);
        return null;
    }


    private Boolean validateChatRqePara(CloseReqBody closeReqBody, ImChannelContext imChannelContext, RespBody respBody) {
        if (StringUtils.isEmpty(closeReqBody.getUserId())) {
            respBody.setCode(ImStatus.INVALID_VERIFICATION.getCode()).setMsg("传入userId 不能为空");
            return false;
        } else if (StringUtils.isEmpty(imChannelContext.getUserId())) {
            respBody.setCode(ImStatus.INVALID_VERIFICATION.getCode()).setMsg("当前用户未登录");
            return false;
        } else if (!closeReqBody.getUserId().equals(imChannelContext.getUserId())) {
            log.error("退出登录请求，传入userID和登录用户不一致。{}，{}", closeReqBody.getUserId(), imChannelContext.getUserId());
            respBody.setCode(ImStatus.INVALID_VERIFICATION.getCode()).setMsg("传入userID和登录用户不一致");
            return false;
        }
        return true;
    }

    @Override
    public Command command() {
        return Command.COMMAND_CLOSE_REQ;
    }
}
