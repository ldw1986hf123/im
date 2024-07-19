package com.kuailu.im.server.util;

import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSONObject;
import com.kuailu.im.server.mq.PushMessage;
import com.kuailu.im.server.starter.BaseJunitTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CountDownLatch;

@Slf4j
public class TestBatch extends BaseJunitTest {
    private static int threadCount = 30;

    private final static CountDownLatch COUNT_DOWN_LATCH = new CountDownLatch(threadCount); //为保证30个线程同时并发运行

    @Autowired
    KafkaTemplate kafkaTemplate;

    @Test
    public void main() {
        String jsonObject = "{\"convType\":0,\"fromType\":\"apaas\",\"mentionedType\":0,\"messageId\":\"f8cb5db0a1df4af1ab1184e62885e519\",\"pushContent\":\"您有一项待办：《FZ2023100003 KMCPL 2023-10 2023-10 汇总付款申请[Summary Of Payment Application] RMB 306965.83 2023-10-17 11:44:37》，请您及时处理\",\"pushData\":{\"chatType\":2,\"concatTitle\":\"【流程待办】您有一项待办：《FZ2023100003 KMCPL 2023-10 2023-10 汇总付款申请[Summary Of Payment Application] RMB 306965.83 2023-10-17 11:44:37》，请您及时处理\",\"content\":\"<html><br><!DOCTYPE html><html><head><meta name=\\\"viewport\\\" content=\\\"width=device-width,initial-scale=1.0 ,maximum-scale=1, minimum-scale=1.0,user-scalable=no\\\" /></head><body><p>曾明怡.ZengMingYi，您好<br/>        许彬彬.XuBinBin刚刚制单了许彬彬.XuBinBin的付款汇总申请《FZ2023100003 KMCPL 2023-10 2023-10 汇总付款申请[Summary Of Payment Application] RMB 306965.83 2023-10-17 11:44:37》，现到达结算经理环节，请点此<a target=\\\"_blank\\\" href=\\\"https://oi.bwoil.com/portal/#/smartForm?needTransfer=need&processID=c58fbcde121c4df2845693470e688359&processDefNo=Process_fin_fukuanshenqing_huizong&processInstId=7ed9645a-6c9f-11ee-9368-0242ba9e91e8&taskInstId=7f986e9d-6c9f-11ee-adaf-02422538bde3&taskStatus=1\\\">处理</a></p><p><br/></p><p>--以上消息由智能办公系统自动发送<br/></p></body></html><br></html>\",\"id\":\"f8cb5db0a1df4af1ab1184e62885e519\",\"linkList\":[{\"linkName\":\"处理\",\"linkUrl\":\"https://oi.bwoil.com/portal/#/smartForm?needTransfer=noneed&processID=c58fbcde121c4df2845693470e688359&processDefNo=Process_fin_fukuanshenqing_huizong&processInstId=7ed9645a-6c9f-11ee-9368-0242ba9e91e8&taskInstId=7f986e9d-6c9f-11ee-adaf-02422538bde3&taskStatus=1\"}],\"msgClass\":\"approveMsg\",\"receiverId\":\"7806af91-615c-42fd-b136-34e1db7ce9d3\",\"scene\":\"processToDo\",\"title\":\"您有一项待办：《FZ2023100003 KMCPL 2023-10 2023-10 汇总付款申请[Summary Of Payment Application] RMB 306965.83 2023-10-17 11:44:37》，请您及时处理\"},\"pushMessageType\":\"NORMAL\",\"senderName\":\"快鹭智能办公\",\"unReceivedMsgNumber\":4,\"unReceivedMsgNumberOfPad\":4,\"userId\":\"7806af91-615c-42fd-b136-34e1db7ce9d3\"}";
        PushMessage pushMessage = JSONObject.toJavaObject(JSONObject.parseObject(jsonObject), PushMessage.class);
        for (int i = 0; i < threadCount; i++) {//循环开30个线程
            int finalI = i;
            new Thread(new Runnable() {
                public void run() {
                /*    COUNT_DOWN_LATCH.countDown();//每次减一
                    try {
                        COUNT_DOWN_LATCH.await(); //此处等待状态，为了让30个线程同时进行
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }*/
                    log.info("发送的时间finaI " + finalI);
                    pushMessage.setMessageId(finalI + "");
                    ListenableFuture<SendResult<String, String>> future = kafkaTemplate.send("kafka-test-topic",  JSONObject.toJSONString(pushMessage));
                    future.addCallback(new ListenableFutureCallback<SendResult<String, String>>() {
                        @Override
                        public void onSuccess(SendResult<String, String> result) {
                            log.info("成功发送消息：{}，offset=[{}]", pushMessage, result.getRecordMetadata().offset());
                        }

                        @Override
                        public void onFailure(Throwable ex) {
                            log.error("消息：{} 发送失败，原因：{}", pushMessage, ex.getMessage());
                        }
                    });
                }
            }).start();

        }
    }


    @Test
    public void sendSingle() {
        for (int i = 0; i < threadCount; i++) {//循环开30个线程
            PushMessage pushMessage = new PushMessage();
            pushMessage.setMessageId(i + "");
            ListenableFuture<SendResult<String, String>> future = kafkaTemplate.send("kafka-test-topic",  JSONObject.toJSONString(pushMessage));
            future.addCallback(new ListenableFutureCallback<SendResult<String, String>>() {
                @Override
                public void onSuccess(SendResult<String, String> result) {
                    log.info("成功发送消息：{}，offset=[{}]", pushMessage, result.getRecordMetadata().offset());
                }

                @Override
                public void onFailure(Throwable ex) {
                    log.error("消息：{} 发送失败，原因：{}", pushMessage, ex.getMessage());
                }
            });
          /*  ListenableFuture<SendResult<String, String>> future = kafkaTemplate.send("test",  JSONObject.toJSONString(pushMessage));
            future.addCallback(new ListenableFutureCallback<SendResult<String, String>>() {
                @Override
                public void onSuccess(SendResult<String, String> result) {
                    log.info("成功发送消息：{}，offset=[{}]", pushMessage, result.getRecordMetadata().offset());
                }

                @Override
                public void onFailure(Throwable ex) {
                    log.error("消息：{} 发送失败，原因：{}", pushMessage, ex.getMessage());
                }
            });*/


        }
    }

}