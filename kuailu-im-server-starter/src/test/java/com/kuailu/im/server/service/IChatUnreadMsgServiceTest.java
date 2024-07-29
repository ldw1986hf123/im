package com.kuailu.im.server.service;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.kuailu.im.core.packets.Command;
import com.kuailu.im.core.packets.LoginReqBody;
import com.kuailu.im.core.tcp.TcpPacket;
import com.kuailu.im.core.ws.WsServerDecoder;
import com.kuailu.im.server.model.entity.ChatUnreadMsg;
import com.kuailu.im.server.starter.BaseJunitTest;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.utils.ByteUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
class IChatUnreadMsgServiceTest extends BaseJunitTest {

    @Autowired
    IChatUnreadMsgService unreadMsgService;

    @Test
    void saveOrUpdateTest() {
        String userId = "userId1234aa";
        String groupId = "groupId123aa";

        ChatUnreadMsg chatUnreadMsg = new ChatUnreadMsg();
        chatUnreadMsg.setMsgId("messid123ccc");
        chatUnreadMsg.setUserId(userId);
        chatUnreadMsg.setGroupId(groupId);
        UpdateWrapper<ChatUnreadMsg> subject_name_cn = new UpdateWrapper<ChatUnreadMsg>()
                .eq("user_id", userId).eq("group_id", groupId);

        unreadMsgService.saveOrUpdate(chatUnreadMsg, subject_name_cn);

    }


    @Test
    void loiginTest() {
        LoginReqBody loginReqBody = new LoginReqBody();
        loginReqBody.setUserId("3856eca9-c16c-4c75-b0f3-8bdb34afdea7");
        loginReqBody.setSeid("989662d9-a420-4a38-b0f2-af13d446bda4");
        loginReqBody.setClientType("Android");
        loginReqBody.setCmd(Command.COMMAND_LOGIN_REQ.getNumber());
        byte[] loginBody = loginReqBody.toByte();
        log.info("发送的参数：{}", JSONUtil.toJsonStr(loginReqBody));
        TcpPacket loginPacket = new TcpPacket(Command.COMMAND_LOGIN_REQ, loginBody);

        byte[] bytes = JSONUtil.toJsonStr(loginReqBody).getBytes();
        System.out.println(Arrays.toString(bytes));

        System.out.println(new String(bytes));

    }

}