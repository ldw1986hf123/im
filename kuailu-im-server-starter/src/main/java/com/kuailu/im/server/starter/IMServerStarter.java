package com.kuailu.im.server.starter;


import com.kuailu.im.core.packets.Command;
import com.kuailu.im.core.utils.PropUtil;
import com.kuailu.im.server.JimServer;
import com.kuailu.im.server.command.CommandManager;
import com.kuailu.im.server.command.handler.ChatReqHandler;
import com.kuailu.im.server.command.handler.HandshakeReqHandler;
import com.kuailu.im.server.command.handler.LoginReqHandler;
import com.kuailu.im.server.config.ImServerConfig;
import com.kuailu.im.server.config.PropertyImServerConfigBuilder;
import com.kuailu.im.server.listener.ImKuailuGroupListener;
import com.kuailu.im.server.listener.ImKuailuUserListener;
import com.kuailu.im.server.processor.chat.ChatReqProcessor;
import com.kuailu.im.server.schduler.PushScheduledTask;
import com.kuailu.im.server.service.*;
import com.kuailu.im.server.starter.command.KuailuWsHandshakeProcessor;
import com.kuailu.im.server.starter.service.LoginServiceProcessor;
import com.kuailu.im.server.util.ApplicationContextHelper;
import com.kuailu.im.server.util.RedisService;
import org.apache.commons.lang3.StringUtils;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.tio.core.ssl.SslConfig;


@SpringBootApplication(scanBasePackages = {"com.kuailu.im.server.**.**"})
@ComponentScan("com.kuailu.im.server.**.**")
@MapperScan(basePackages = "com.kuailu.im.server.mapper")
@EnableScheduling
public class IMServerStarter {

    public static void main(String[] args) throws Exception {
//        ConfigurableApplicationContext context = new SpringApplicationBuilder(IMServerStarter.class).web(WebApplicationType.NONE).run(args);
//
        /***************** 启动springboot  ********************************/
        final ConfigurableApplicationContext context = SpringApplication.run(IMServerStarter.class, args);
        ApplicationContextHelper.init(context);
        ImServerConfig imServerConfig = new PropertyImServerConfigBuilder("config/jim.properties").build();
        //初始化SSL;(开启SSL之前,你要保证你有SSL证书哦...)
        initSsl(imServerConfig);
        //设置群组监听器，非必须，根据需要自己选择性实现;
        imServerConfig.setImGroupListener(new ImKuailuGroupListener());
        //设置绑定用户监听器，非必须，根据需要自己选择性实现;
        imServerConfig.setImUserListener(new ImKuailuUserListener());
        JimServer jimServer = new JimServer(imServerConfig);

        /*****************WS握手业务处理*******************************/
        HandshakeReqHandler handshakeReqHandler = CommandManager.getCommand(Command.COMMAND_HANDSHAKE_REQ, HandshakeReqHandler.class);
        //添加自定义握手处理器;
        handshakeReqHandler.addMultiProtocolProcessor(new KuailuWsHandshakeProcessor());
        /*****************WS握手处理**********************************/

        /*****************登录业务处理*********************************/
        LoginReqHandler loginReqHandler = CommandManager.getCommand(Command.COMMAND_LOGIN_REQ, LoginReqHandler.class);
        //添加登录业务处理器;
        loginReqHandler.setSingleProcessor(new LoginServiceProcessor());
        /*****************登录业务处理**********************************/


        /***************** 发送聊天消息处理器  ********************************/
        ChatReqHandler chatReqHandler = CommandManager.getCommand(Command.COMMAND_CHAT_REQ_2, ChatReqHandler.class);
        IConversationService conversationService = ApplicationContextHelper.get().getBean(IConversationService.class);
        IChatGroupService groupService = ApplicationContextHelper.get().getBean(IChatGroupService.class);
        IChatMsgService iChatMsgService = ApplicationContextHelper.get().getBean(IChatMsgService.class);
        IMergedMsgService mergedMsgService = ApplicationContextHelper.get().getBean(IMergedMsgService.class);
        IChatGroupMemberService groupMemberService = ApplicationContextHelper.get().getBean(IChatGroupMemberService.class);
        IUserAccountService userAccountService = ApplicationContextHelper.get().getBean(IUserAccountService.class);
        RedisService redisService = ApplicationContextHelper.get().getBean(RedisService.class);
        PushScheduledTask pushScheduledTask = ApplicationContextHelper.get().getBean(PushScheduledTask.class);
        IAtMsgService atMsgService = ApplicationContextHelper.get().getBean(IAtMsgService.class);

        chatReqHandler.setSingleProcessor(new ChatReqProcessor(conversationService, groupService, iChatMsgService, mergedMsgService, groupMemberService,
                userAccountService, pushScheduledTask, redisService, atMsgService));
        /***************** 发送聊天消息处理器  ********************************/

        jimServer.start();


    }

    /**
     * 开启SSL之前，你要保证你有SSL证书哦！
     *
     * @param imServerConfig
     * @throws Exception
     */
    private static void initSsl(ImServerConfig imServerConfig) throws Exception {
        //开启SSL
        if (ImServerConfig.ON.equals(imServerConfig.getIsSSL())) {
            String keyStorePath = PropUtil.get("jim.key.store.path");
            String keyStoreFile = keyStorePath;
            String trustStoreFile = keyStorePath;
            String keyStorePwd = PropUtil.get("jim.key.store.pwd");
            if (StringUtils.isNotBlank(keyStoreFile) && StringUtils.isNotBlank(trustStoreFile)) {
                SslConfig sslConfig = SslConfig.forServer(keyStoreFile, trustStoreFile, keyStorePwd);
                imServerConfig.setSslConfig(sslConfig);
            }
        }
    }
}
