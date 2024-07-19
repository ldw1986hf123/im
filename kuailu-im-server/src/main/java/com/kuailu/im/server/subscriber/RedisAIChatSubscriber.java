//package com.kuailu.im.server.subscriber;
//
//import com.alibaba.fastjson.JSON;
//import com.kuailu.im.core.utils.JsonKit;
//import com.kuailu.im.server.model.entity.AIChatMsg;
//import com.kuailu.im.server.req.AIChatReqParam;
//import com.kuailu.im.server.service.IAIChatMsgService;
//import com.kuailu.im.server.service.IUserAccountService;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.data.redis.connection.Message;
//import org.springframework.data.redis.connection.MessageListener;
//import org.springframework.stereotype.Component;
//
//@Slf4j
//@Component
//public class RedisAIChatSubscriber implements MessageListener {
//
//    @Autowired
//    IAIChatMsgService iaiChatMsgService;
//
//    @Autowired
//    IUserAccountService userAccountService;
//
//    @Override
//    public void onMessage(Message message, byte[] pattern) {
//        String channel = new String(message.getChannel());
//        String value = new String(message.getBody());
//        System.out.println("我是监听者小A,我监听到的消息是 " + message.toString());
//        AIChatReqParam aiChatReqParam = JSON.parseObject(message.toString(), AIChatReqParam.class);
//        iaiChatMsgService.saveQuestion(aiChatReqParam );
//
//    }
//}
