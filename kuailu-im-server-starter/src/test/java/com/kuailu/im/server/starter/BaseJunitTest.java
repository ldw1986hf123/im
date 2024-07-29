package com.kuailu.im.server.starter;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.kuailu.im.core.packets.Command;
import com.kuailu.im.core.packets.LoginReqBody;
import com.kuailu.im.core.tcp.TcpPacket;
import com.kuailu.im.server.config.ImClientConfig;
import com.kuailu.im.server.handler.HelloImClientHandler;
import com.kuailu.im.server.listener.HelloImClientListener;
import com.kuailu.im.server.model.entity.UserAccount;
import com.kuailu.im.server.service.IUserAccountService;
import lombok.extern.slf4j.Slf4j;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.tio.core.Node;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {IMServerStarter.class})
@Slf4j
public class BaseJunitTest {

    @Autowired
    IUserAccountService userAccountService;

    public static final String sid = "0259b104-1449-4a62-abf6-a22f80e0ce6a";

    public static String devHost = "192.168.101.67";
    public static String localHost = "127.0.0.1";
    public static String testhost = "192.168.102.126";
    public static String UAT4 = "https://koaim.bwoil.com/";


    public static final String WSUrl = "kuailuIm-dev.brightoilonline.com";

    public static String userZhangming = "88497f17-02f1-4fc5-b376-da6a675c76e5";
    public static String userWangzhiChao = "da510f61-f224-4b24-864a-afa2fc5856ad";
    public static String qianJingJing = "f4af0933-e178-4af9-bc50-d1e3d1175696";

    public static String sunQinFeng = "12e56b81-10a3-4683-b78a-0802ecd8ff16";
    public static String keTianshun = "9cf6acff-5bf5-4c7f-87d5-6d89bec2de21";
    public static String mengxiuze = "7e735a4e-604b-4485-84ad-9506222a3702";
    public static String wenyue = "d9e46d24-dbe7-4d86-88b2-7cf8afa935a1";
    public static String pengsisi = "0ab166cf-8fa5-4aa2-a07a-567616c29ff4";
    public static String wangcesi = "415c05ab8773458faf0dea99d83fecfd";
    public static String liangchiqiang = "3856eca9-c16c-4c75-b0f3-8bdb34afdea7";

    public static String lindewei = "b48fabbf-500a-464f-95ed-582a77549d2d";
    public static String wugenyuan = "4b98ee42-6ac9-43d4-b495-1c86863c30c5";
    public static String zhangqinhui  = "00eaf868-e4bd-4a89-b5cf-8b0cbecaf041";






    public static ImClientChannelContext imClientChannelContext = null;

    protected static void init(String host) {
        //服务器节点
        Node serverNode = new Node(host, 6688);
        //构建客户端配置信息
        ImClientConfig imClientConfig = ImClientConfig.newBuilder()
                //客户端业务回调器,不可以为NULL
                .clientHandler(new HelloImClientHandler())
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
        try {
            imClientChannelContext = jimClient.connect(serverNode);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        //连上后，发条消息玩玩
//        login();
    }

    protected static void login(String userId, String seid) {
        LoginReqBody loginReqBody = new LoginReqBody();
        loginReqBody.setUserId(userId);
        loginReqBody.setSeid(seid);
        loginReqBody.setClientType("Android");
        loginReqBody.setCmd(Command.COMMAND_LOGIN_REQ.getNumber());
        byte[] loginBody = loginReqBody.toByte();
        TcpPacket loginPacket = new TcpPacket(Command.COMMAND_LOGIN_REQ, loginBody);
        JimClientAPI.send(imClientChannelContext, loginPacket);
    }

    protected UserAccount getUser(Long userId) {

        UserAccount userAccount = userAccountService.getOne(new QueryWrapper<UserAccount>().lambda().eq(UserAccount::getUserId, userId));
        return userAccount;
    }

    public void printResult(Object result) {
        log.info("结果是：{}", JSON.toJSONString(result));
    }

}