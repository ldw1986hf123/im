package com.kuailu.im.server.client;

import cn.hutool.core.util.IdUtil;
import cn.hutool.json.JSONUtil;
import cn.xfyun.api.RtasrClient;
import cn.xfyun.model.response.rtasr.RtasrResponse;
import cn.xfyun.service.rta.AbstractRtasrWebSocketListener;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.kuailu.im.core.packets.ChatType;
import com.kuailu.im.core.packets.Command;
import com.kuailu.im.core.tcp.TcpPacket;
import com.kuailu.im.server.enums.MessageTypeEnum;
import com.kuailu.im.server.req.ChatReqParam;
import com.kuailu.im.server.req.MessageBody;
import com.kuailu.im.server.starter.BaseJunitTest;
import com.kuailu.im.server.starter.JimClientAPI;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Response;
import okhttp3.WebSocket;
import okio.ByteString;

import javax.annotation.Nullable;
import java.io.File;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.concurrent.CountDownLatch;

/**
 * 版本: [1.0]
 */
@Slf4j
public class RtAsrClientTest extends BaseJunitTest {


    /**
     * 启动程序入口
     */
    public static void main(String[] args) throws Exception {
        init(localHost);
        sendAtMsg();
    }

    private static void sendAtMsg() {
        login("701b1947-693a-4d6a-b646-0901e9d94b47", "430336d7-f0a9-4331-8e61-ea5104ffb1a3");
        ChatReqParam chatBody = new ChatReqParam();
        chatBody.setId(IdUtil.fastUUID());
        chatBody.setMsgType(MessageTypeEnum.ATMessage.getCode());
        chatBody.setCreatedTime(new Date().getTime());
        chatBody.setReceiver("0d5cef8525354ea1b024554d7fb9a864");
        chatBody.setGroupId("0d5cef8525354ea1b024554d7fb9a864");
        chatBody.setChatType(ChatType.CHAT_TYPE_PUBLIC.getNumber());
        chatBody.setConversationId("33d325fe5945423580912805516c5094");

        MessageBody messageBody = JSONUtil.toBean("{\n" +
                "    \"atUsersInfo\": [\n" +
                "        {\n" +
                "            \"userID\": \"b354a2db-3bf0-4946-9965-04a152ef3fe7\",\n" +
                "            \"nickname\": \"朱玲.ZhuLing\"\n" +
                "        }\n" +
                "    ],\n" +
                "    \"atUserIdContent\": \" @b354a2db-3bf0-4946-9965-04a152ef3fe7 dasf\",\n" +
                "    \"content\": \"@朱玲.ZhuLing dasf\",\n" +
                "    \"isAtSelf\": false,\n" +
                "    \"atUserList\": [\n" +
                "        \"b354a2db-3bf0-4946-9965-04a152ef3fe7\"\n" +
                "    ]\n" +
                "}", MessageBody.class);
        chatBody.setMessageBody(messageBody);
        chatBody.setCmd(Command.COMMAND_CHAT_REQ_2.getNumber());
        log.info("发送的消息  :{}", JSONUtil.toJsonStr(chatBody));
        TcpPacket chatPacket = new TcpPacket(Command.COMMAND_CHAT_REQ_2, chatBody.toByte());
        JimClientAPI.send(imClientChannelContext, chatPacket);
    }

}
