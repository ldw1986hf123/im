package com.kuailu.im.server.client;

import cn.hutool.json.JSONUtil;
import com.kuailu.im.core.packets.ChatMsgReadConfirmBody;
import com.kuailu.im.core.packets.Command;
import com.kuailu.im.core.packets.ConversationListReq;
import com.kuailu.im.core.tcp.TcpPacket;
import com.kuailu.im.server.starter.BaseJunitTest;
import com.kuailu.im.server.starter.JimClientAPI;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ChatMsgReadConfirmClient extends BaseJunitTest {

    /**
     * 启动程序入口
     */
    public static void main(String[] args) throws Exception {
        init(devHost);
        send(sid);
    }

    public static void send(String seid) {
        login("2ab66a1d-1aad-43ff-901b-9c588335db90", "037e5b7d-6ff7-4233-95fe-d5cce2160fde");
        ChatMsgReadConfirmBody messageReqBody = JSONUtil.toBean("{\"userId\":\"3856eca9-c16c-4c75-b0f3-8bdb34afdea7\",\"otherUserId\":\"03bd42b9-1ec4-48e6-8694-3ea7d830b100\",\"groupId\":\"44063cc24a394ff4ae43a1a271ff1af1\",\"msgId\":\"68c04d8f3a704044ae861cbe12391de1\",\"cmd\":23,\"readMsgTime\":1680330067000}", ChatMsgReadConfirmBody.class);
      /*  messageReqBody.setCount(5);
        messageReqBody.setEndTime(DateUtil.offsetHour(new Date(), -1).getTime());
        messageReqBody.setUserId("88497f17-02f1-4fc5-b376-da6a675c76e5");
        messageReqBody.setGroupId("484437f4d38743e0b9cb57665899f034");
        messageReqBody.setCmd(Command.COMMAND_GET_MESSAGE_REQ.getNumber());*/

        log.info("发送的消息  :{}", JSONUtil.toJsonStr(messageReqBody));

        TcpPacket chatPacket = new TcpPacket(Command.COMMAND_MSG_READ_CONFIRM_REQ, messageReqBody.toByte());
        JimClientAPI.send(imClientChannelContext, chatPacket);
    }
}
