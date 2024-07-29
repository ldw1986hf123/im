package com.kuailu.im.server.service;

import cn.xfyun.api.RtasrClient;
import cn.xfyun.model.response.rtasr.RtasrResponse;
import cn.xfyun.service.rta.AbstractRtasrWebSocketListener;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.gson.JsonObject;
import com.kuailu.im.server.listener.MyWebSocketListener;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;

import javax.annotation.Nullable;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.CountDownLatch;

@Slf4j
public class RatAsrStarter {
    private static final String hostUrl = "https://iat-api.xfyun.cn/v2/iat"; //中英文，http url 不支持解析 ws/wss schema
    // private static final String hostUrl = "https://iat-niche-api.xfyun.cn/v2/iat";//小语种
    private static final String appid = "678702c2"; //在控制台-我的应用获取
    private static final String apiSecret = "ZDNhNzc5YTk2YzA0OTI3ZGJlODA5Mzk0"; //在控制台-我的应用-语音听写（流式版）获取
    private static final String apiKey = "266e047c2e7a1f145a6e11f22a28f488"; //在控制台-我的应用-语音听写（流式版）获取
    private static final String file = "C:\\Users\\ludw\\Desktop\\16k_10.pcm"; // 中文
    public static final int StatusFirstFrame = 0;
    public static final int StatusContinueFrame = 1;
    public static final int StatusLastFrame = 2;
    RtasrClient client = null;

  /*  public static void main(String[] args) throws Exception {
        IatStarter iatStarter = new IatStarter();
        iatStarter.send();
    }*/


    public void send(byte[] bytesStr) throws Exception {
        if (null == client) {
            client = new RtasrClient.Builder()
                    .signature("678702c2", "b7a82d29a149a891551c1d09a8c326c3").build();
        }

    /*    File file = new File(resourcePath + filePath);
        FileInputStream inputStream = new FileInputStream(file);
        byte[] buffer = new byte[1024000];
        inputStream.read(buffer);
        CountDownLatch latch = new CountDownLatch(1);*/
        client.send(bytesStr, null, new AbstractRtasrWebSocketListener() {
            @Override
            public void onSuccess(WebSocket webSocket, String text) {
                RtasrResponse response = JSONObject.parseObject(text, RtasrResponse.class);
                log.info(getContent(response.getData()));
            }

            @Override
            public void onFail(WebSocket webSocket, Throwable t, @Nullable Response response) {
                log.info("onFail");
            }

            @Override
            public void onBusinessFail(WebSocket webSocket, String text) {
                System.out.println(text);
                log.info("onBusinessFail");
            }

            @Override
            public void onClosed() {
                log.info("onClosed");
            }
        });

    }

    // 把转写结果解析为句子
    private String getContent(String message) {
        StringBuffer resultBuilder = new StringBuffer();
        try {
            JSONObject messageObj = JSON.parseObject(message);
            JSONObject cn = messageObj.getJSONObject("cn");
            JSONObject st = cn.getJSONObject("st");
            JSONArray rtArr = st.getJSONArray("rt");
            for (int i = 0; i < rtArr.size(); i++) {
                JSONObject rtArrObj = rtArr.getJSONObject(i);
                JSONArray wsArr = rtArrObj.getJSONArray("ws");
                for (int j = 0; j < wsArr.size(); j++) {
                    JSONObject wsArrObj = wsArr.getJSONObject(j);
                    JSONArray cwArr = wsArrObj.getJSONArray("cw");
                    for (int k = 0; k < cwArr.size(); k++) {
                        JSONObject cwArrObj = cwArr.getJSONObject(k);
                        String wStr = cwArrObj.getString("w");
                        resultBuilder.append(wStr);
                    }
                }
            }
        } catch (Exception e) {
            return message;
        }

        return resultBuilder.toString();
    }


}
