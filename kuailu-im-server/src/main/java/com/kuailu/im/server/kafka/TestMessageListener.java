package com.kuailu.im.server.kafka;

import com.alibaba.fastjson.JSONObject;
import com.kuailu.im.server.mq.PushMessage;
import com.kuailu.im.server.schduler.MsgHelperScheduledTask;
import com.kuailu.im.server.service.IChatGroupService;
import com.kuailu.im.server.service.IChatMsgService;
import com.kuailu.im.server.service.IConversationService;
import com.kuailu.im.server.util.RedisService;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class TestMessageListener {

    @Autowired
    RedisService redisService;

    @Autowired
    IChatGroupService chatGroupService;

    @Autowired
    IChatMsgService chatMsgService;

    @Autowired
    IConversationService conversationService;


    @Autowired
    MsgHelperScheduledTask msgHelperScheduledTask;

    /**
     * 最好能让kafka一个个顺序进行消费，避免带来并发问题
     * （1）可以通过配置 max.poll.records 属性来控制每次拉取的消息数量。默认情况下，它是 500 条。如果你只想拉取一条消息，可以将其设置为 1。
     * spring.kafka.consumer.properties.max.poll.records: 1
     * 这样配置后，每次 poll() 方法调用将只拉取一条消息。
     * 请注意，逐个拉取消息可能会导致较高的延迟，因此你需要权衡消费效率和实时性之间的需求。
     * （2）
     *
     * @param consumerRecords
     * @param ack
     */
    @KafkaListener(
            topics = "kafka-test-topic",
            groupId = "kafka-test-groupId")
    public void batchConsumer(List<ConsumerRecord<String, String>> consumerRecords, Acknowledgment ack) {
        try {

            for (ConsumerRecord<String, String> consumerRecord : consumerRecords) {
                String jsonObject = consumerRecord.value();
                PushMessage pushMessage = JSONObject.toJavaObject(JSONObject.parseObject(jsonObject), PushMessage.class);
                log.info("messageId:{}", pushMessage.getMessageId());

            }
        } catch (Exception e) {
            log.error("消费apass kafka消息异常", e);
        } finally {
            ack.acknowledge();
        }
    }

}
