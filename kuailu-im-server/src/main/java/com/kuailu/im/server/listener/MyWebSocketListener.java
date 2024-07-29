package com.kuailu.im.server.listener;

import com.alibaba.fastjson.JSON;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.kuailu.im.core.ImChannelContext;
import com.kuailu.im.core.ImPacket;
import com.kuailu.im.core.ImStatus;
import com.kuailu.im.core.packets.Command;
import com.kuailu.im.core.packets.RespBody;
import com.kuailu.im.server.JimServerAPI;
import com.kuailu.im.server.command.handler.IatReqHandler;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.apache.commons.lang3.StringUtils;
import org.tio.core.ChannelContext;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.*;

@Slf4j
public class MyWebSocketListener extends WebSocketListener {
    Decoder decoder = new Decoder();
    private String userId;

    public MyWebSocketListener(String userId) {
        this.userId = userId;
    }

    @Override
    public void onOpen(WebSocket webSocket, Response response) {
        log.info("open  {}", webSocket);
    }

    @Override
    public void onMessage(WebSocket webSocket, String text) {
        ResponseData resp = json.fromJson(text, ResponseData.class);
        if (resp != null) {
            if (resp.getCode() != 0) {
                log.info("code=>" + resp.getCode() + " error=>" + resp.getMessage() + " sid=" + resp.getSid());
                log.info("错误码查询链接：https://www.xfyun.cn/document/error-code");
                return;
            }
            if (resp.getData() != null) {
                if (resp.getData().getResult() != null) {
                    Text te = resp.getData().getResult().getText();
                    try {
                        decoder.decode(te);
                        log.info("IM 中间识别结果 ==》" + decoder.toString());
                        sendResult(decoder.toString());
                    } catch (Exception e) {
                        log.error("IM 中间识别结果 异常", e);
                    }
                }
                if (resp.getData().getStatus() == 2) {
                    // todo  resp.data.status ==2 说明数据全部返回完毕，可以关闭连接，释放资源
                    log.info("session end ");
                    log.info("最终识别结果 ==》" + decoder.toString());
                    log.info("本次识别sid ==》" + resp.getSid());
                    sendResult(decoder.toString());
                    webSocket.close(1000, (String) null);
                    decoder.discard();
                } else {
                    // todo 根据返回的数据处理
                }
            }
        }
    }

    @Override
    public void onClosed(WebSocket webSocket, int code, String reason) {
        log.info("onClosed  {}", webSocket);
    }

    @Override
    public void onFailure(WebSocket webSocket, Throwable t, Response response) {
        log.info("onFailure   response", response, t);
        super.onFailure(webSocket, t, response);
    }


    private void sendResult(String result) {
        ImPacket resultImPacket = new ImPacket(new RespBody(Command.forNumber(Command.IAT_ASR.getNumber()), result).toByte());
        JimServerAPI.sendToUser(userId, resultImPacket);
    }


    public final Gson json = new Gson();

    public class ResponseData {
        private int code;
        private String message;
        private String sid;
        private Data data;

        public int getCode() {
            return code;
        }

        public String getMessage() {
            return this.message;
        }

        public String getSid() {
            return sid;
        }

        public Data getData() {
            return data;
        }
    }

    public static class Data {
        private int status;
        private Result result;

        public int getStatus() {
            return status;
        }

        public Result getResult() {
            return result;
        }
    }

    public class Result {
        int bg;
        int ed;
        String pgs;
        int[] rg;
        int sn;
        Ws[] ws;
        boolean ls;
        JsonObject vad;

        public Text getText() {
            Text text = new Text();
            StringBuilder sb = new StringBuilder();
            for (Ws ws : this.ws) {
                sb.append(ws.cw[0].w);
            }
            text.sn = this.sn;
            text.text = sb.toString();
            text.sn = this.sn;
            text.rg = this.rg;
            text.pgs = this.pgs;
            text.bg = this.bg;
            text.ed = this.ed;
            text.ls = this.ls;
            text.vad = this.vad == null ? null : this.vad;
            return text;
        }
    }

    public class Ws {
        Cw[] cw;
        int bg;
        int ed;
    }

    public class Cw {
        int sc;
        String w;
    }

    public class Text {
        int sn;
        int bg;
        int ed;
        String text;
        String pgs;
        int[] rg;
        boolean deleted;
        boolean ls;
        JsonObject vad;

        @Override
        public String toString() {
            return "Text{" +
                    "bg=" + bg +
                    ", ed=" + ed +
                    ", ls=" + ls +
                    ", sn=" + sn +
                    ", text='" + text + '\'' +
                    ", pgs=" + pgs +
                    ", rg=" + Arrays.toString(rg) +
                    ", deleted=" + deleted +
                    ", vad=" + (vad == null ? "null" : vad.getAsJsonArray("ws").toString()) +
                    '}';
        }
    }

    //解析返回数据，仅供参考
    public class Decoder {
        private Text[] texts;
        private int defc = 10;

        public Decoder() {
            this.texts = new Text[this.defc];
        }

        public synchronized void decode(Text text) {
            if (text.sn >= this.defc) {
                this.resize();
            }
            if ("rpl".equals(text.pgs)) {
                for (int i = text.rg[0]; i <= text.rg[1]; i++) {
                    this.texts[i].deleted = true;
                }
            }
            this.texts[text.sn] = text;
        }

        public String toString() {
            StringBuilder sb = new StringBuilder();
            for (Text t : this.texts) {
                if (t != null && !t.deleted) {
                    sb.append(t.text);
                }
            }
            return sb.toString();
        }

        public void resize() {
            int oc = this.defc;
            this.defc <<= 1;
            Text[] old = this.texts;
            this.texts = new Text[this.defc];
            for (int i = 0; i < oc; i++) {
                this.texts[i] = old[i];
            }
        }

        public void discard() {
            for (int i = 0; i < this.texts.length; i++) {
                this.texts[i] = null;
            }
        }
    }
}
