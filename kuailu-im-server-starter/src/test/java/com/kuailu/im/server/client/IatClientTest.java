package com.kuailu.im.server.client;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.IdUtil;
import cn.hutool.json.JSONUtil;
import com.google.gson.JsonObject;
import com.kuailu.im.core.packets.ChatType;
import com.kuailu.im.core.packets.Message;
import com.kuailu.im.server.enums.MessageOperaTypeEnum;
import com.kuailu.im.server.enums.MessageTypeEnum;
import com.kuailu.im.server.req.ChatReqParam;
import com.kuailu.im.core.packets.Command;
import com.kuailu.im.core.tcp.TcpPacket;
import com.kuailu.im.server.req.MessageBody;
import com.kuailu.im.server.starter.BaseJunitTest;
import com.kuailu.im.server.starter.JimClientAPI;
import lombok.extern.slf4j.Slf4j;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 版本: [1.0]
 */
@Slf4j
public class IatClientTest extends BaseJunitTest {

    static String file = "C:\\Users\\ludw\\Desktop\\audio_qbh.wav"; // 中文

    /**
     * 启动程序入口
     */
    public static void main(String[] args) throws Exception {
//        sendMessage();
        init(localHost);
        senFile();
    }

  /*  private static void sendMessage() {
//        init(localHost);
//        login(pengsisi, "430336d7-f0a9-4331-8e61-ea5104ffb1a3");
        ChatReqParam chatBody = new ChatReqParam();

        chatBody.setId(IdUtil.fastUUID());
        chatBody.setMsgType(MessageTypeEnum.TEXT.getCode());
        chatBody.setCreatedTime(new Date().getTime());
        chatBody.setReceiver(pengsisi);
        chatBody.setGroupId("7151830d61ae44ffac12ce996bef944c");
        chatBody.setChatType(ChatType.CHAT_TYPE_PUBLIC.getNumber());
        chatBody.setConversationId("49fdb7fd870042d58652a1a30d712f87");
        MessageBody messageBody = new MessageBody();
        messageBody.setContent("dad" + new Date());
        chatBody.setMessageBody(messageBody);
        chatBody.setCmd(Command.COMMAND_CHAT_REQ_2.getNumber());
        log.info("发送的消息  :{}", JSONUtil.toJsonStr(chatBody));
        TcpPacket chatPacket = new TcpPacket(Command.COMMAND_CHAT_REQ_2, chatBody.toByte());
        JimClientAPI.send(imClientChannelContext, chatPacket);
    }

    private void sendMessage(String sender, String receiver, String groupId, String conversationId) {
        login(sender, "430336d7-f0a9-4331-8e61-ea5104ffb1a3");
        ChatReqParam chatBody = new ChatReqParam();
        chatBody.setId(IdUtil.fastUUID());
        chatBody.setMsgType(MessageTypeEnum.TEXT.getCode());
        chatBody.setCreatedTime(new Date().getTime());
        chatBody.setReceiver(receiver);
        chatBody.setGroupId(groupId);
        chatBody.setChatType(ChatType.CHAT_TYPE_PUBLIC.getNumber());
        chatBody.setConversationId(conversationId);
        MessageBody messageBody = new MessageBody();
        messageBody.setContent("dad" + new Date());
        chatBody.setMessageBody(messageBody);
        chatBody.setCmd(Command.COMMAND_CHAT_REQ_2.getNumber());
        log.info("发送的消息  :{}", JSONUtil.toJsonStr(chatBody));
        TcpPacket chatPacket = new TcpPacket(Command.COMMAND_CHAT_REQ_2, chatBody.toByte());
        JimClientAPI.send(imClientChannelContext, chatPacket);
    }
*/

    private static void senFile() {
        login("701b1947-693a-4d6a-b646-0901e9d94b47", "430336d7-f0a9-4331-8e61-ea5104ffb1a3");
        int frameSize = 1280; //每一帧音频的大小,建议每 40ms 发送 122B
        try  {
            byte[] buffer = new byte[frameSize];
            // 发送音频
            int len = 0;
            FileInputStream fis = new FileInputStream(file);

            int serial = 0;
            int status = 0;
            while (true) {
                len = fis.read(buffer);
                if (len != -1) {
                    IatParam iatParam = new IatParam();
                    iatParam.setBytes(Base64.getEncoder().encodeToString(Arrays.copyOf(buffer, len)));
                    iatParam.setSerial(serial + "");
                    iatParam.setState(status);
                    TcpPacket chatPacket = new TcpPacket(Command.IAT_ASR, iatParam.toByte());
                    JimClientAPI.send(imClientChannelContext, chatPacket);
                    status = 1;
                } else {
                    status=2;
                    IatParam iatParam = new IatParam();
                    iatParam.setState(status);
                    iatParam.setSerial(serial + "");
                    TcpPacket chatPacket = new TcpPacket(Command.IAT_ASR, iatParam.toByte());
                    JimClientAPI.send(imClientChannelContext, chatPacket);
                    break;
                }
                serial++;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }


}

class IatParam extends Message {
    private String bytes;
    private String serial;

    // start end
    private int state;

    public IatParam() {
    }

    public String getBytes() {
        return bytes;
    }

    public void setBytes(String bytes) {
        this.bytes = bytes;
    }

    public String getSerial() {
        return serial;
    }

    public void setSerial(String serial) {
        this.serial = serial;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }
}