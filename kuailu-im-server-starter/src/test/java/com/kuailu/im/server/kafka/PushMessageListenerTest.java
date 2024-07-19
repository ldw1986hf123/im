package com.kuailu.im.server.kafka;


import com.alibaba.fastjson.JSONObject;
import com.kuailu.im.server.mq.PushMessage;
import com.kuailu.im.server.starter.BaseJunitTest;
import com.kuailu.im.server.util.UUIDUtil;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;

 public class PushMessageListenerTest extends BaseJunitTest {

    @Autowired
    KafkaTemplate kafkaTemplate;

    @Test
    public void send() {
        PushMessage pushMessage = new PushMessage();
        pushMessage.setMessageId("123");
        pushMessage.setUserId("dsadad");


        kafkaTemplate.send("apaas-service", UUIDUtil.getUUID(), JSONObject.toJSONString(pushMessage));
    }
}