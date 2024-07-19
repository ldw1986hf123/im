package com.kuailu.im.server.util;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.kuailu.im.server.req.AIChatReqParam;
import org.junit.Test;

public class JsonTest {
    @Test
    public void testConsumer() {
        // 模拟消息
        String message ="{\"cmd\":34,\"content\":\"ldw 的第一个问题\",\"questionId\":\"1231\",\"sender\":\"d9e46d24-dbe7-4d86-88b2-7cf8afa935a1\",\"topicId\":\"topicId11\"}";
        AIChatReqParam binLogMessage = JSONObject.parseObject(message, new TypeReference<AIChatReqParam>() {
        });
        String before = binLogMessage.getContent();
        System.out.println("before：" + before.toString());
    }

}
