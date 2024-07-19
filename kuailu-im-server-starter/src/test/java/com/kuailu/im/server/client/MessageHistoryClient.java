package com.kuailu.im.server.client;

import cn.hutool.json.JSONUtil;
import com.kuailu.im.core.packets.Command;
import com.kuailu.im.core.tcp.TcpPacket;
import com.kuailu.im.server.req.MessageHistoryReqBody;
import com.kuailu.im.server.starter.BaseJunitTest;
import com.kuailu.im.server.starter.JimClientAPI;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;

/**
 * 版本: [1.0]
 * 功能说明:
 * 作者: WChao 创建时间: 2017年8月30日 下午1:05:17
 */
@Slf4j
public class MessageHistoryClient extends BaseJunitTest {
    /**
     * 启动程序入口
     */
    public static void main(String[] args) throws Exception {
        //连上后，发条消息玩玩
        init(localHost);
        send();
    }


    private static void send() throws Exception {
        login("d9e46d24-dbe7-4d86-88b2-7cf8afa935a1", "b24bc9f6-b0a9-4b25-bd2e-c0aa4cbe719e");
        MessageHistoryReqBody messageReqBody = JSONUtil.toBean("{\"userId\":\"d9e46d24-dbe7-4d86-88b2-7cf8afa935a1\",\"groupId\":\"99967f9fd5f44a1bb5ebd468297cb34a\",\"endTime\":1683542386184,\"count\":50,\"cmd\":19,\"type\":1,\"receiver\":\"99967f9fd5f44a1bb5ebd468297cb34a\",\"chatType\":1}", MessageHistoryReqBody.class);
      /*  messageReqBody.setCount(5);
        messageReqBody.setEndTime(DateUtil.offsetHour(new Date(), -1).getTime());
        messageReqBody.setUserId("88497f17-02f1-4fc5-b376-da6a675c76e5");
        messageReqBody.setGroupId("484437f4d38743e0b9cb57665899f034");
        messageReqBody.setCmd(Command.COMMAND_GET_MESSAGE_REQ.getNumber());*/

        messageReqBody.setEndTime(new Date().getTime());
//        messageReqBody.setChatType(ChatType.CHAT_TYPE_PRIVATE.getNumber());
        log.info("发送的消息  :{}", JSONUtil.toJsonStr(messageReqBody));

        TcpPacket chatPacket = new TcpPacket(Command.COMMAND_GET_MESSAGE_HISTORY, messageReqBody.toByte());
        JimClientAPI.send(imClientChannelContext, chatPacket);
    }

}
