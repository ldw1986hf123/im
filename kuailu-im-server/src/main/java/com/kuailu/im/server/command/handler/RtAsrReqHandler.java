package com.kuailu.im.server.command.handler;

import cn.hutool.json.JSONUtil;
import cn.xfyun.api.RtasrClient;
import cn.xfyun.model.response.rtasr.RtasrResponse;
import cn.xfyun.service.rta.AbstractRtasrWebSocketListener;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.kuailu.im.core.ImChannelContext;
import com.kuailu.im.core.ImPacket;
import com.kuailu.im.core.exception.ImException;
import com.kuailu.im.core.packets.Command;
import com.kuailu.im.core.packets.Message;
import com.kuailu.im.core.utils.JsonKit;
import com.kuailu.im.server.ImServerChannelContext;
import com.kuailu.im.server.command.AbstractCmdHandler;
import com.kuailu.im.server.req.ChatReqParam;
import com.kuailu.im.server.service.RatAsrStarter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Response;
import okhttp3.WebSocket;
import okio.ByteString;

import javax.annotation.Nullable;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.SignatureException;
import java.util.Arrays;
import java.util.Base64;
import java.util.concurrent.CountDownLatch;

@Slf4j
public class RtAsrReqHandler extends AbstractCmdHandler {

    @SneakyThrows
    @Override
    public ImPacket handler(ImPacket packet, ImChannelContext channelContext) {
        ImServerChannelContext imServerChannelContext = (ImServerChannelContext) channelContext;
        String currentUserId = channelContext.getUserId();

//        RtAsrParam rtAsrParam = JsonKit.toBean(packet.getBody(), RtAsrParam.class);

        String bodyStr = new String(packet.getBody());
        RtAsrParam rtAsrParam = JSONUtil.toBean(bodyStr, RtAsrParam.class);
        byte[] bytes =  rtAsrParam.getBytes();
        byte[] bytesAll = Arrays.copyOfRange(bytes,0,bytes.length);
        log.info("bytes:{}  ", bytesAll);
        RatAsrStarter ratAsrStarter = new RatAsrStarter();
        ratAsrStarter.send(bytesAll);
        return null;
    }


    @Override
    public Command command() {
        return Command.RT_ASR;
    }

}

class RtAsrParam extends Message {
    private byte[] bytes;
    private String serial;

    public RtAsrParam() {
    }

    public byte[] getBytes() {
        return bytes;
    }

    public void setBytes(byte[] bytes) {
        this.bytes = bytes;
    }

    public String getSerial() {
        return serial;
    }

    public void setSerial(String serial) {
        this.serial = serial;
    }
}