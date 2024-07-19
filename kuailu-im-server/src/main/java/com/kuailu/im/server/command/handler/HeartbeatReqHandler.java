package com.kuailu.im.server.command.handler;

import com.alibaba.fastjson.JSON;
import com.kuailu.im.core.ImChannelContext;
import com.kuailu.im.core.ImPacket;
import com.kuailu.im.core.ImStatus;
import com.kuailu.im.core.exception.ImException;
import com.kuailu.im.core.packets.*;
import com.kuailu.im.core.utils.JsonKit;
import com.kuailu.im.server.JimServerAPI;
import com.kuailu.im.server.command.AbstractCmdHandler;

import com.kuailu.im.server.model.entity.UserAccount;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

/**
 *
 */
@Slf4j
public class HeartbeatReqHandler extends AbstractCmdHandler {
    @Override
    public ImPacket handler(ImPacket packet, ImChannelContext channelContext) throws ImException {
        HeartbeatBody heartbeatReqBody = JsonKit.toBean(packet.getBody(), HeartbeatBody.class);
        String userId = channelContext.getUserId();

        ImPacket LOGIN_EXPIREPacket = null;
        if (StringUtils.isEmpty(userId) || !userAccountService.isOnLine(userId)) {
            log.error("登录状态丢失，需要重新登录 heartbeatReqBody: {} ", JSON.toJSONString(heartbeatReqBody));
            LOGIN_EXPIREPacket = new ImPacket(new RespBody(Command.forNumber(heartbeatReqBody.getCmd()), ImStatus.NEED_LOGIN).toByte());
        } else {
            LOGIN_EXPIREPacket = new ImPacket(new RespBody(Command.forNumber(heartbeatReqBody.getCmd()), ImStatus.OK).toByte());
        }
        JimServerAPI.send(channelContext, LOGIN_EXPIREPacket);
        return null;
    }

    @Override
    public Command command() {
        return Command.COMMAND_HEARTBEAT_REQ;
    }

    static class HeartbeatBody extends Message {
        private Long timeStamp;

        public HeartbeatBody() {
        }

        public HeartbeatBody(Long hbbyte) {
            this.timeStamp = hbbyte;
        }

        public Long getTimeStamp() {
            return timeStamp;
        }

        public void setTimeStamp(Long timeStamp) {
            this.timeStamp = timeStamp;
        }
    }


}
