package com.kuailu.im.server.command.handler;

import com.google.gson.JsonObject;
import com.kuailu.im.core.ImChannelContext;
import com.kuailu.im.core.ImPacket;
import com.kuailu.im.core.packets.Command;
import com.kuailu.im.core.packets.Message;
import com.kuailu.im.core.utils.JsonKit;
import com.kuailu.im.server.ImServerChannelContext;
import com.kuailu.im.server.command.AbstractCmdHandler;
import com.kuailu.im.server.constant.RedisCacheKey;
import com.kuailu.im.server.listener.MyWebSocketListener;
import com.kuailu.im.server.service.IatStarter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.WebSocket;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Slf4j
public class IatReqHandler extends AbstractCmdHandler {

    @Override
    public ImPacket handler(ImPacket packet, ImChannelContext channelContext) {
        String currentUserId = channelContext.getUserId();
        IatParam iatParam = JsonKit.toBean(packet.getBody(), IatParam.class);
        String bytesStr = iatParam.getBytes();
        Integer state = iatParam.getState();
        String language=iatParam.getLanguage();
        IatStarter iatStarter = null;
        int statusFirstFrame = 0;
        int statusLastFrame = 2;
        int statusContinueFrame = 1;
        if (statusFirstFrame == state) {
            log.info("statusFirstFrame ：{}   bytesStr:{}",state,bytesStr);
            iatStarter = new IatStarter(currentUserId);
            guavaCache.put(currentUserId, iatStarter);
            iatStarter.sendStart(bytesStr,language);
        } else if (statusContinueFrame == state) {
            iatStarter = guavaCache.get(currentUserId, IatStarter.class);
            iatStarter.sendMiddle(bytesStr);
        } else if (statusLastFrame == state) {
            log.info("statusLastFrame ：{}   bytesStr:{}",state,bytesStr);
            iatStarter = guavaCache.get(currentUserId, IatStarter.class);
            iatStarter.sendEnd(bytesStr);
        }
        return null;
    }

    @Override
    public Command command() {
        return Command.IAT_ASR;
    }
}


class IatParam extends Message {
    private String bytes;
    private String serial;
    private Integer state;
    private String language="zh_cn";

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

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }
}