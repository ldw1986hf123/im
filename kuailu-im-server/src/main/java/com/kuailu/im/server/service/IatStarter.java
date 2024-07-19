package com.kuailu.im.server.service;

import com.google.gson.JsonObject;
import com.kuailu.im.server.listener.MyWebSocketListener;
import com.kuailu.im.server.util.ApplicationContextHelper;
import lombok.extern.slf4j.Slf4j;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.WebSocket;
import org.springframework.core.env.Environment;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

@Slf4j
public class IatStarter {
    private final String appid = ApplicationContextHelper.get().getBean(Environment.class).getProperty("xunfei.APPID");    //在控制台-我的应用获取

    private WebSocket webSocket = null;

    public IatStarter(String userId) {
        OkHttpClient client = new OkHttpClient();
        String hostUrl = ApplicationContextHelper.get().getBean(Environment.class).getProperty("xunfei.hostUrl");
        //在控制台-我的应用获取
        String apiKey = ApplicationContextHelper.get().getBean(Environment.class).getProperty("xunfei.APIKey");
        //在控制台-我的应用获取
        String apiSecret = ApplicationContextHelper.get().getBean(Environment.class).getProperty("xunfei.APISecret");
        String authUrl = getAuthUrl(hostUrl, apiKey, apiSecret);
        //将url中的 schema http://和https://分别替换为ws:// 和 wss://
        String url = authUrl.replace("http://", "ws://").replace("https://", "wss://");
        Request request = new Request.Builder().url(url).build();
        webSocket = client.newWebSocket(request, new MyWebSocketListener(userId));
    }

    public void sendStart(String bytesStr,String language) {
        log.info("start---------");
        JsonObject frame = new JsonObject();
        JsonObject business = new JsonObject();  //第一帧必须发送
        JsonObject common = new JsonObject();  //第一帧必须发送
        JsonObject data = new JsonObject();  //每一帧都要发送
        // 填充common
        common.addProperty("app_id", appid);
        //填充business
        business.addProperty("language", language);
        //business.addProperty("language", "en_us");//英文
        //business.addProperty("language", "ja_jp");//日语，在控制台可添加试用或购买
        //business.addProperty("language", "ko_kr");//韩语，在控制台可添加试用或购买
        //business.addProperty("language", "ru-ru");//俄语，在控制台可添加试用或购买
        business.addProperty("domain", "iat");
        business.addProperty("pd", "game");
        business.addProperty("accent", "mandarin");//中文方言请在控制台添加试用，添加后即展示相应参数值
        business.addProperty("vad_eos", 4000);//中文方言请在控制台添加试用，添加后即展示相应参数值
        //business.addProperty("nunum", 0);
        //business.addProperty("ptt", 0);//标点符号
        business.addProperty("rlang", "zh-cn"); // zh-cn :简体中文（默认值）zh-hk :繁体香港(若未授权不生效，在控制台可免费开通)
//        business.addProperty("speexSize", 70); // zh-cn :简体中文（默认值）zh-hk :繁体香港(若未授权不生效，在控制台可免费开通)
        //business.addProperty("vinfo", 1);
        business.addProperty("dwa", "wpgs");//动态修正(若未授权不生效，在控制台可免费开通)
        business.addProperty("nbest", 5);// 句子多候选(若未授权不生效，在控制台可免费开通)
        business.addProperty("wbest", 3);// 词级多候选(若未授权不生效，在控制台可免费开通)
        //填充data
        int statusFirstFrame = 0;
        data.addProperty("status", statusFirstFrame);
        data.addProperty("format", "audio/L16;rate=16000");
        data.addProperty("encoding", "raw");
//                data.addProperty("audio", Base64.getEncoder().encodeToString(Arrays.copyOf(buffer, len)));
        data.addProperty("audio", bytesStr);

        //填充frame
        frame.add("common", common);
        frame.add("business", business);
        frame.add("data", data);
        webSocket.send(frame.toString());
    }

    public void sendEnd(String bytesStr) {
        log.info("  end----------------------------------");
        JsonObject frame2 = new JsonObject();
        JsonObject data2 = new JsonObject();
        int statusLastFrame = 2;
        data2.addProperty("status", statusLastFrame);
        data2.addProperty("audio", "");
        data2.addProperty("format", "audio/L16;rate=16000");
        data2.addProperty("encoding", "raw");
        frame2.add("data", data2);
        webSocket.send(frame2.toString());
        log.info("all data is send");
    }

    public void sendMiddle(String bytesStr) {
        JsonObject frame1 = new JsonObject();
        JsonObject data1 = new JsonObject();
        int statusContinueFrame = 1;
        data1.addProperty("status", statusContinueFrame);
        data1.addProperty("format", "audio/L16;rate=16000");
        data1.addProperty("encoding", "raw");
        data1.addProperty("audio", bytesStr);
        frame1.add("data", data1);
        webSocket.send(frame1.toString());
    }

    public void cleanSocket() {
        webSocket.close(1000, "Connection closed by client");
        webSocket = null;
    }


    private String getAuthUrl(String hostUrl, String apiKey, String apiSecret) {
        URL url = null;
        String date = null;
        String sha = "";
        Charset charset = Charset.forName("UTF-8");
        try {
            url = new URL(hostUrl);
            SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
            format.setTimeZone(TimeZone.getTimeZone("GMT"));
            date = format.format(new Date());
            StringBuilder builder = new StringBuilder("host: ").append(url.getHost()).append("\n").//
                    append("date: ").append(date).append("\n").//
                    append("GET ").append(url.getPath()).append(" HTTP/1.1");
            Mac mac = Mac.getInstance("hmacsha256");
            SecretKeySpec spec = new SecretKeySpec(apiSecret.getBytes(charset), "hmacsha256");
            mac.init(spec);
            byte[] hexDigits = mac.doFinal(builder.toString().getBytes(charset));
            sha = Base64.getEncoder().encodeToString(hexDigits);
        } catch (MalformedURLException e) {
            log.error("加密异常", e);
        } catch (NoSuchAlgorithmException e) {
            log.error("加密异常", e);
        } catch (InvalidKeyException e) {
            log.error("加密异常", e);
        }

        //System.out.println(sha);
        String authorization = String.format("api_key=\"%s\", algorithm=\"%s\", headers=\"%s\", signature=\"%s\"", apiKey, "hmac-sha256", "host date request-line", sha);
        //System.out.println(authorization);
        HttpUrl httpUrl = HttpUrl.parse("https://" + url.getHost() + url.getPath()).newBuilder().//
                addQueryParameter("authorization", Base64.getEncoder().encodeToString(authorization.getBytes(charset))).//
                addQueryParameter("date", date).//
                addQueryParameter("host", url.getHost()).//
                build();
        return httpUrl.toString();
    }

}
