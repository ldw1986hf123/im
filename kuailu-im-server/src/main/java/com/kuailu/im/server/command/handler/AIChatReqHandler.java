package com.kuailu.im.server.command.handler;

import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.kuailu.im.core.ImChannelContext;
import com.kuailu.im.core.ImPacket;
import com.kuailu.im.core.ImStatus;
import com.kuailu.im.core.exception.ImException;
import com.kuailu.im.core.packets.Command;
import com.kuailu.im.core.packets.RespBody;
import com.kuailu.im.server.JimServerAPI;
import com.kuailu.im.server.command.AbstractCmdHandler;
import com.kuailu.im.server.constant.RedisCacheKey;
import com.kuailu.im.server.enums.AIAnswerTagEnum;
import com.kuailu.im.server.protocol.ProtocolManager;
import com.kuailu.im.server.req.AIChatReqParam;
import com.kuailu.im.server.response.AIChatResponse;
import com.kuailu.im.server.util.UUIDUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 小鹭AI助手发生消息处理
 */
@Slf4j
public class AIChatReqHandler extends AbstractCmdHandler {

    //5秒算法接口没有返回，就超时
    private static Integer DEFAULT_TIME_OUT = 5 * 1000;


    /**
     * [
     * {
     * "data": "sql",
     * "content": "2024-01-29 18:02:20 question一二三四五六七八九十 的答案4"
     * },
     * {
     * "data": "txt",
     * "content": "2024-01-29 18:02:20 question一二三四五六七八九十 的答案4"
     * },
     * {
     * "data": "table",
     * "content": "数组"
     * }
     * ]
     *
     * @param packet
     * @param channelContext
     * @return
     * @throws ImException
     */
    @Override
    public ImPacket handler(ImPacket packet, ImChannelContext channelContext) throws ImException {
        String currentUserId = channelContext.getUserId();
        AIChatReqParam aiChatReqParam = JSON.parseObject(packet.getBody(), AIChatReqParam.class);
        String topicId = aiChatReqParam.getTopicId();
        String questionMsgId = aiChatReqParam.getId();
        String content = aiChatReqParam.getContent();
        String messageId= UUIDUtil.getUUID();
        aiChatReqParam.setSender(currentUserId);

        aiChatMsgService.saveQuestion(aiChatReqParam);

        //调用AI算法接口生成答案,返回给前端
        AIChatResponse aiChatResponse = null;
        //调用算法接口，生成答案

        String unicode = strToURlCode(content);
        String urlPath = sseClient.getUrlPath();
        // 单订阅
        List<Map> answerContent = new ArrayList();
        InputStream inputStream = null;
        BufferedReader reader = null;
        //因为保存到数据库，所以要破解一个完整的内容
        StringBuilder totalSummary=new StringBuilder();
        try {
            log.info("调用算法接口，生成答案. 算法调用地址： {}", urlPath + "execute_sql_stream?question=" + unicode);
            inputStream = getSseInputStream(urlPath + "execute_sql_stream?question=" + unicode, DEFAULT_TIME_OUT);
            reader = new BufferedReader(new InputStreamReader(inputStream));
            String line = "";
            while ((line = reader.readLine()) != null) {
                //只需要关注三种数据  [sql]  [table] [text]    [summary]
                log.info("算法返回内容 {}", line);
                Map<String, String> dataMap = new HashMap();
                if (line.contains(AIAnswerTagEnum.DONE.getTag())) {
                    List<Map<String, String>> contentList = new ArrayList();
                    aiChatResponse = new AIChatResponse.TextResponseBuilder(questionMsgId, topicId,messageId).build();
                    RespBody resPacket = new RespBody(command().getNumber(), aiChatResponse);
                    aiChatResponse.getRecommendCommand().add("相似指令1");
                    aiChatResponse.getRecommendCommand().add("相似指令2");
                    aiChatResponse.getRecommendCommand().add("相似指令3");
                    dataMap.put("data", AIAnswerTagEnum.DONE.getTag());
                    dataMap.put("content", "");
                    contentList.add(dataMap);
                    aiChatResponse.setContent(contentList);
                    ImPacket responsePacket = ProtocolManager.Converter.respPacket(resPacket, channelContext);
                    JimServerAPI.send(channelContext, responsePacket);
                    break;
                } else if (line.contains(AIAnswerTagEnum.SQL.getTag()) || line.contains(AIAnswerTagEnum.TABLE.getTag())) {
                    List<Map<String, String>> contentList = new ArrayList();
                    aiChatResponse = new AIChatResponse.TextResponseBuilder(questionMsgId, topicId,messageId).build();
                    RespBody resPacket = new RespBody(command().getNumber(), aiChatResponse);
                    AIAnswerTagEnum aiAnswerTagEnum = getTagByStr(line);
                    dataMap.put("data", aiAnswerTagEnum.getTag().replace("data:", ""));
                    dataMap.put("content", line.replace(aiAnswerTagEnum.getTag(), ""));
                    contentList.add(dataMap);
                    aiChatResponse.setContent(contentList);

                    ImPacket responsePacket = ProtocolManager.Converter.respPacket(resPacket, channelContext);
                    JimServerAPI.send(channelContext, responsePacket);

                    //组装保存到数据库的内容
                    answerContent.add(dataMap);

                } else if (line.contains(AIAnswerTagEnum.SUMMARY.getTag())) {
                    List<Map<String, String>> contentList = new ArrayList();
                    String summaryContent=line.replace(AIAnswerTagEnum.SUMMARY.getTag(), "");

                    aiChatResponse = new AIChatResponse.TextResponseBuilder(questionMsgId, topicId,messageId).build();
                    RespBody resPacket = new RespBody(command().getNumber(), aiChatResponse);
                    AIAnswerTagEnum aiAnswerTagEnum = getTagByStr(line);
                    dataMap.put("data", aiAnswerTagEnum.getFrontTag());
                    dataMap.put("content",summaryContent );
                    contentList.add(dataMap);
                    aiChatResponse.setContent(contentList);
                    ImPacket responsePacket = ProtocolManager.Converter.respPacket(resPacket, channelContext);
                    JimServerAPI.send(channelContext, responsePacket);
                    //组装保存到数据库的内容
                    totalSummary.append(summaryContent);
                }
            }
        } catch (IOException e) {
            log.error("调用算法接口异常", e);
            RespBody resPacket = new RespBody(command(), ImStatus.ERROR);
            return ProtocolManager.Converter.respPacket(resPacket, channelContext);
        } finally {
            // 服务器端主动关闭时，客户端手动关闭
            log.info("服务器端主动关闭时，客户端手动关闭");
            try {
                if (null != reader) {
                    reader.close();
                }
            } catch (IOException e) {
                log.error("关闭流异常", e);
            }
            if (null != inputStream) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    log.error("关闭流异常", e);
                }
            }
        }

        Map<String,String >totalDataMap=new HashMap();
        totalDataMap.put("data",  AIAnswerTagEnum.SUMMARY.getFrontTag());
        totalDataMap.put("content",totalSummary.toString());
        answerContent.add(totalDataMap);
        if (!aiChatMsgService.hasAnswer(questionMsgId)) {
            aiChatMsgService.saveAnswer(aiChatResponse.getMessageId(), JSONUtil.toJsonStr(answerContent), questionMsgId, topicId, currentUserId);
        } else {
            answerExtendService.saveAnswer(aiChatResponse.getMessageId(),  JSONUtil.toJsonStr(answerContent), questionMsgId, topicId, currentUserId);
        }

        //每次放消息都延迟一下会话,10分钟过期
        String key = RedisCacheKey.AI_CHAT_TOPIC + topicId;
        redisService.setValue(key, currentUserId, 10, TimeUnit.MINUTES);
        return null;
    }

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

    @Override
    public Command command() {
        return Command.AI_CHAT_REQ;
    }


    private String strToUnicode(String string) {
        StringBuffer unicode = new StringBuffer();
        for (int i = 0; i < string.length(); i++) {
            // 取出每一个字符
            char c = string.charAt(i);
            // 转换为unicode
            unicode.append("\\u" + Integer.toHexString(c));
        }
        return unicode.toString();
    }


    private String strToURlCode(String string) {
        String encodedUrl = null;
        try {
            encodedUrl = URLEncoder.encode(string, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            log.error("Encoded error  ", e);
        }
        log.info("Encoded URL: " + encodedUrl);

        return encodedUrl;
    }


    private AIAnswerTagEnum getTagByStr(String tagParam) {
        for (AIAnswerTagEnum answerTagEnum : AIAnswerTagEnum.values()) {
            if (tagParam.contains(answerTagEnum.getTag())) {
                return answerTagEnum;
            }
        }
        return null;
    }


}
