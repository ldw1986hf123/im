package com.kuailu.im.server.client;

import cn.hutool.json.JSONUtil;
import com.kuailu.im.core.packets.Command;
import com.kuailu.im.core.packets.ConversationListReq;
import com.kuailu.im.core.tcp.TcpPacket;
import com.kuailu.im.server.starter.BaseJunitTest;
import com.kuailu.im.server.starter.JimClientAPI;
import lombok.extern.slf4j.Slf4j;

/**
 * 版本: [1.0]
 */
@Slf4j
public class ConversationReqClientTest extends BaseJunitTest {


    /**
     * 启动程序入口
     */
    public static void main(String[] args) throws Exception {
        init(localHost);
        send(sid);
    }

    public static void send(String seid) {
        login("d9e46d24-dbe7-4d86-88b2-7cf8afa935a1", "0259b104-1449-4a62-abf6-a22f80e0ce6a");
        ConversationListReq messageReqBody = JSONUtil.toBean("{\"userId\":\"d9e46d24-dbe7-4d86-88b2-7cf8afa935a1\",\"cmd\":17,\"type\":2,\"seid\":\"0259b104-1449-4a62-abf6-a22f80e0ce6a\"}", ConversationListReq.class);
      /*  messageReqBody.setCount(5);
        messageReqBody.setEndTime(DateUtil.offsetHour(new Date(), -1).getTime());
        messageReqBody.setUserId("88497f17-02f1-4fc5-b376-da6a675c76e5");
        messageReqBody.setGroupId("484437f4d38743e0b9cb57665899f034");
        messageReqBody.setCmd(Command.COMMAND_GET_MESSAGE_REQ.getNumber());*/

        log.info("发送的消息  :{}", JSONUtil.toJsonStr(messageReqBody));

        TcpPacket chatPacket = new TcpPacket(Command.CONVERSATION_LIST, messageReqBody.toByte());
        JimClientAPI.send(imClientChannelContext, chatPacket);
    }
}
