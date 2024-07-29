package com.kuailu.im.server.service.impl;


import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class SSEClient {
    // timeout
    private static Integer DEFAULT_TIME_OUT = 2*60*1000;

    /** 获取SSE输入流 */
    public static InputStream getSseInputStream(String urlPath, int timeoutMill) throws IOException {
        HttpURLConnection urlConnection = getHttpURLConnection(urlPath, timeoutMill);
        InputStream inputStream = urlConnection.getInputStream();
        return new BufferedInputStream(inputStream);
    }

    /** 读流数据 */
    public static void readStream(InputStream is, MsgHandler msgHandler) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        try {
            String line = "";
            while ((line = reader.readLine()) != null) {
                msgHandler.handleMsg(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // 服务器端主动关闭时，客户端手动关闭
            reader.close();
            is.close();
        }
    }

    private static HttpURLConnection getHttpURLConnection(String urlPath, int timeoutMill) throws IOException {
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

    /** 消息处理接口 */
    interface MsgHandler {
        void handleMsg(String line);
    }

    public static void main(String[] args) throws Exception {
        // 单订阅
        String urlPath = "http://192.168.204.198:60001/execute_sql_stream?question=%E5%93%AA%E4%B8%AA%E9%83%A8%E9%97%A8%E6%80%BB%E5%8A%A0%E7%8F%AD%E6%97%B6%E9%97%B4%E6%9C%80%E5%A4%9A";
        InputStream inputStream = getSseInputStream(urlPath, DEFAULT_TIME_OUT);
        readStream(inputStream, new MsgHandler() {
            @Override
            public void handleMsg(String line) {
                System.out.println(line);
                if (line != null && line.contains("data:")) {
                    // 注意按约定的消息协议解析消息
                    String msg = line.split(":")[1];
                    System.out.println(msg);
                }
            }
        });

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
}
