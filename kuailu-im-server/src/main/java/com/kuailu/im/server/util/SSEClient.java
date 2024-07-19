package com.kuailu.im.server.util;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

@Slf4j
@Component
public class SSEClient {
    // timeout
    private static Integer DEFAULT_TIME_OUT = 2 * 60 * 1000;

    @Value("${algorithm.url}")
    String urlPath;

    /**
     * 获取SSE输入流
     */
    private InputStream getSseInputStream(String urlPath, int timeoutMill) throws IOException {
        HttpURLConnection urlConnection = getHttpURLConnection(urlPath, timeoutMill);
        InputStream inputStream = urlConnection.getInputStream();
        return new BufferedInputStream(inputStream);
    }


    private HttpURLConnection getHttpURLConnection(String urlPath, int timeoutMill) throws IOException {
        URL url = new URL(urlPath);
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setDoOutput(true);
        urlConnection.setDoInput(true);
        urlConnection.setUseCaches(false);
        urlConnection.setRequestMethod("GET");
        urlConnection.setRequestProperty("Connection", "Keep-Alive");
        urlConnection.setRequestProperty("Charset", "UTF-8");
        // text/plain模式
        urlConnection.setRequestProperty("Content-Type", "text/plain; charset=UTF-8");
        // 读过期时间
        urlConnection.setReadTimeout(timeoutMill);
        return urlConnection;
    }

    /**
     * 消息处理接口
     */
/*    interface MsgHandler {
        void handleMsg(String line);
    }*/
   /* public String getAnswer(String question) {
        // 单订阅
        String sb = "";
        InputStream inputStream = null;
        try {
            inputStream = getSseInputStream(urlPath+"execute_sql_stream?question" + question, DEFAULT_TIME_OUT);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            try {
                String line = "";
                while ((line = reader.readLine()) != null) {
                    log.info("算法接口返回：  {} ", line);
//                    if (line.contains("[DONE]") || line.contains("event:close")) {
                        log.info("算法接口返回结束");
                *//*        break;
                    } else {*//*
                        sb=line;
//                    }
                }
            } catch (IOException e) {
                log.error("调用算法接口异常", e);
            } finally {
                // 服务器端主动关闭时，客户端手动关闭
                reader.close();
                if (null != inputStream) {
                    inputStream.close();
                }
            }
        } catch (IOException e) {
            log.error("调用算法接口异常", e);
        }

        return sb;
        // 并发订阅
        // ExecutorService executorService = Executors.newFixedThreadPool(100);
        // for (int i = 0; i < 100; i++) {
        //     int finalI = i;
        //     executorService.submit(() -> {
        //         try {
        //             String urlPath = "http://localhost:8089/sse/websocket/subscribe?questionId=kingtao" + finalI;
        //             InputStream inputStream = getSseInputStream(urlPath, DEFAULT_TIME_OUT);
        //             readStream(inputStream, new MsgHandler() {
        //                 @Override
        //                 public void handleMsg(String line) {
        //                     if (line != null && line.contains("data:")) {
        //                         // 注意按约定的消息协议解析消息
        //                         String msg = line.split(":")[1];
        //                         System.out.println(msg);
        //                     }
        //                 }
        //             });
        //         } catch (Exception e) {
        //             e.printStackTrace();
        //         }
        //     });
        // }
        // executorService.shutdown();
    }
*/
    public String getUrlPath() {
        return urlPath;
    }

}
