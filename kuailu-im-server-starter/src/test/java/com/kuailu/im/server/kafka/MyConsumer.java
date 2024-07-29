package com.kuailu.im.server.kafka;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.time.Duration;
import java.util.Arrays;
import java.util.Properties;

import static java.lang.Integer.parseInt;
@Slf4j
public class MyConsumer {
    private final static String TOPIC_NAME = "apaas-service";
    private final static String CONSUMER_GROUP_NAME = "group-apaas-service";

    public static void main(String[] args) {
        Properties properties = new Properties();
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "192.168.101.67:9092");
        /**
         * 1.消费分组名
         */
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, CONSUMER_GROUP_NAME);
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, "0");

        /**
         * 1.2设置序列化
         */
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());

        /**
         * 2.创建一个消费者的客户端
         */
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(properties);

        /**
         * 3.消费者订阅主题列表
         */
        int offset = 41;
        int partition =0;
        TopicPartition topicPartition = new TopicPartition("apaas-service", partition);
        log.info("Skipping {}-{} offset {}", "\"apaas-service", partition, offset);
        consumer.seek(topicPartition, offset + 1L);
    }
}