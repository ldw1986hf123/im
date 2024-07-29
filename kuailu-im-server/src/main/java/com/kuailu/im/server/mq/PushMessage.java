package com.kuailu.im.server.mq;

import lombok.Data;

import java.util.Date;

@Data
public class PushMessage {
    // 消息的类型，普通消息通知栏；voip要透传。
    public PushMessageType pushMessageType;
    // 推送类型，android推送分为小米/华为/魅族等。ios分别为开发和发布。
//    public MessagePushType messagePushType;
    // 发送者
    public String sender;
    // 发送者名
    public String senderName;
    //0单聊，1群聊
    public int convType;
    // 接受设备
    public String target;
    // 接受设备名
    public String targetName;
    public String userId;
    //  public int line;
    //  public int cntType;
    //  public long serverTime;
    // 消息内容
    public String pushContent;
    public String pushData;


    //推给app的未读数
    public int unReceivedMsgNumber;
    // 使用实现使用@符号来提示某人或者提示一批人或者所有人 1 提示某个人 2 表示提示所有人
    public int mentionedType;
    public String packageName;
    public String deviceToken;
    public String voipDeviceToken;
    public boolean isHiddenDetail;
    public String deviceLanguage;
    public String messageId;
    private Integer timeToLiveInSecond;
    private Date messageTime;
    private String pushBizNumber;
    //推给pad的未读数
    private Integer unReceivedMsgNumberOfPad;
    private String fromType="im";
}
