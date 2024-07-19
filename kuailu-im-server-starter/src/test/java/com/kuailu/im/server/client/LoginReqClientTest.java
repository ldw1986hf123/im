package com.kuailu.im.server.client;

import cn.hutool.json.JSONUtil;
import com.kuailu.im.core.packets.Command;
import com.kuailu.im.core.packets.LoginReqBody;
import com.kuailu.im.core.tcp.TcpPacket;
import com.kuailu.im.server.config.ImClientConfig;
import com.kuailu.im.server.listener.HelloImClientListener;
import com.kuailu.im.server.starter.ImClientChannelContext;
import com.kuailu.im.server.starter.JimClient;
import com.kuailu.im.server.starter.JimClientAPI;
import lombok.extern.slf4j.Slf4j;
import org.tio.core.Node;

/**
 * 版本: [1.0]
 */
@Slf4j
public class LoginReqClientTest {

    public static ImClientChannelContext imClientChannelContext = null;

    static String devHost="192.168.101.67";
    static String localHost="127.0.0.1";


    /**
     * 启动程序入口
     */
    public static void main(String[] args) throws Exception {
        //服务器节点
        Node serverNode = new Node(localHost, 6688);
        //构建客户端配置信息
        ImClientConfig imClientConfig = ImClientConfig.newBuilder()
                //客户端业务回调器,不可以为NULL
//                .clientHandler(new HelloImClientHandler())
                //客户端事件监听器，可以为null，但建议自己实现该接口
                .clientListener(new HelloImClientListener())
                //心跳时长不设置，就不发送心跳包
//                .heartbeatTimeout(10000)
                //断链后自动连接的，不想自动连接请设为null
                //.reConnConf(new ReconnConf(5000L))
                .build();
        //生成客户端对象;
        JimClient jimClient = new JimClient(imClientConfig);
        //连接服务端
        imClientChannelContext = jimClient.connect(serverNode);
        //连上后，发条消息玩玩
        login();
    }

    private static void login() {
       /* LoginReqBody loginReqBody = new LoginReqBody();
        loginReqBody.setUserId("3856eca9-c16c-4c75-b0f3-8bdb34afdea7");
        loginReqBody.setSeid("989662d9-a420-4a38-b0f2-af13d446bda4");
        loginReqBody.setClientType("Android");*/

        String s="{\"userId\":\"3856eca9-c16c-4c75-b0f3-8bdb34afdea7\",\"clientType\":\"Android\",\"seid\":\"989662d9-a420-4a38-b0f2-af13d446bda4\",\"cmd\":5}";
        LoginReqBody messageReqBody = JSONUtil.toBean(s, LoginReqBody.class);

        log.info("发送的参数：{}", JSONUtil.toJsonStr(messageReqBody));

        TcpPacket loginPacket = new TcpPacket(Command.COMMAND_LOGIN_REQ, messageReqBody.toByte());

        //先登录;
        JimClientAPI.send(imClientChannelContext, loginPacket);
    }
}
