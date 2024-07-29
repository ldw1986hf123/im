package com.kuailu.im.core.packets;


/**
 * 是以枚举的顺序作为值的，不是以value作为值！！
 * todo 待优化掉，应该以确定的int值作为值
 */
public enum Command {
    /**
     * <code>COMMAND_UNKNOW = 0;</code>
     */
    COMMAND_UNKNOW(0),
    /**
     * <pre>
     * 握手请求，含http的websocket握手请求
     * </pre>
     *
     * <code>COMMAND_HANDSHAKE_REQ = 1;</code>
     */
    COMMAND_HANDSHAKE_REQ(1),
    /**
     * <pre>
     * 握手响应，含http的websocket握手响应
     * </pre>
     *
     * <code>COMMAND_HANDSHAKE_RESP = 2;</code>
     */
    COMMAND_HANDSHAKE_RESP(2),
    /**
     * <pre>
     * 鉴权请求
     * </pre>
     *
     * <code>COMMAND_AUTH_REQ = 3;</code>
     */
    COMMAND_AUTH_REQ(3),
    /**
     * <pre>
     * 鉴权响应
     * </pre>
     *
     * <code>COMMAND_AUTH_RESP = 4;</code>
     */
    COMMAND_AUTH_RESP(4),
    /**
     * <pre>
     * 登录请求
     * </pre>
     *
     * <code>COMMAND_LOGIN_REQ = 5;</code>
     */
    COMMAND_LOGIN_REQ(5),
    /**
     * <pre>
     *登录响应
     * </pre>
     *
     * <code>COMMAND_LOGIN_RESP = 6;</code>
     */
  COMMAND_LOGIN_RESP(6),
    /**
     * <pre>
     * 申请进入群组
     * </pre>
     *
     * <code>COMMAND_JOIN_GROUP_REQ = 7;</code>
     */
    COMMAND_JOIN_GROUP_REQ(7),
    /**
     * <pre>
     * 申请进入群组响应
     * </pre>
     *
     * <code>COMMAND_JOIN_GROUP_RESP = 8;</code>
     */
    COMMAND_JOIN_GROUP_RESP(8),
    /**
     * <pre>
     * 进入群组通知
     * </pre>
     *
     * <code>COMMAND_JOIN_GROUP_NOTIFY_RESP = 9;</code>
     */
    COMMAND_JOIN_GROUP_NOTIFY_RESP(9),
    /**
     * <pre>
     * 退出群组通知
     * </pre>
     *
     * <code>COMMAND_EXIT_GROUP_NOTIFY_RESP = 10;</code>
     */
    COMMAND_EXIT_GROUP_NOTIFY_RESP(10),
    /**
     * <pre>
     * 聊天请求
     * </pre>
     *
     * <code>COMMAND_CHAT_REQ = 11;</code>
     */
    COMMAND_CHAT_REQ(11),
    /**
     * <pre>
     * 聊天响应
     * </pre>
     *
     * <code>COMMAND_CHAT_RESP = 12;</code>
     */
    COMMAND_CHAT_RESP(12),
    /**
     * <pre>
     * 心跳请求
     * </pre>
     *
     * <code>COMMAND_HEARTBEAT_REQ = 13;</code>
     */
    COMMAND_HEARTBEAT_REQ(13),
    /**
     * <pre>
     * 退出登录
     * </pre>
     *
     * <code>COMMAND_CLOSE_REQ = 14;</code>
     */
    COMMAND_CLOSE_REQ(14),
    /**
     * <pre>
     * 发出撤消消息指令(管理员可以撤消所有人的消息，自己可以撤消自己的消息)
     * </pre>
     *
     * <code>COMMAND_CANCEL_MSG_REQ = 15;</code>
     */
    COMMAND_CANCEL_MSG_REQ(15),
    /**
     * <pre>
     * 收到撤消消息指令
     * </pre>
     *
     * <code>COMMAND_CANCEL_MSG_RESP = 16;</code>
     */
    COMMAND_CANCEL_MSG_RESP(16),
    /**
     * <pre>
     * 获取用户信息;
     * </pre>
     *
     * <code>COMMAND_GET_USER_REQ = 17;</code>
     */
    CONVERSATION_LIST(17),
    /**
     * <pre>
     * 获取用户信息响应;
     * </pre>
     *
     * <code>COMMAND_GET_USER_RESP = 18;</code>
     */
    COMMAND_GET_USER_RESP(18),
    /**
     * <pre>
     * 获取聊天消息;
     * </pre>
     *
     * <code>COMMAND_GET_MESSAGE_REQ = 19;</code>
     */
    COMMAND_GET_MESSAGE_HISTORY(19),
    /**
     * <pre>
     * 获取聊天消息响应;
     * </pre>
     *
     * <code>COMMAND_GET_MESSAGE_RESP = 20;</code>
     */
    COMMAND_GET_MESSAGE_RESP(20),

    /**
     * <pre>
     * 获取用户会话列表;
     * </pre>
     *
     * <code>COMMAND_GET_MESSAGE_RESP = 21;</code>
     */
    COMMAND_GET_CONVERSATION_REQ(21),
    /**
     * <pre>
     * 获取用户会话列表，并返回每个会话最新一条消息;
     * </pre>
     *
     * <code>COMMAND_GET_MESSAGE_RESP = 22;</code>
     */
    COMMAND_GET_CONVERSATION_RESP(22),

    /**
     * <pre>
     * 消息已读回执;
     * </pre>
     *
     * <code>COMMAND_GET_MESSAGE_RESP = 23;</code>
     */
    COMMAND_MSG_READ_CONFIRM_REQ(23),
    /**
     * <pre>
     * 获取用户会话列表，并返回每个会话最新一条消息;
     * </pre>
     *
     * <code>COMMAND_GET_MESSAGE_RESP = 24;</code>
     */
    COMMAND_MSG_READ_RESP(24),
    /**
     * <pre>
     * 聊天响应-自己发送消息的响应
     * </pre>
     *
     * <code>COMMAND_CHAT_SELF_RESP = 25;</code>
     */
    COMMAND_CHAT_SELF_RESP(25),
    /**
     * <pre>
     * 聊天响应-自己发送消息的响应
     * </pre>
     *
     * <code>COMMAND_CHAT_SELF_RESP = 26;</code>
     */
    COMMAND_CHAT_REQ_2(26),


    /**
     * <pre>
     * 登录请求
     * </pre>
     *
     * <code>COMMAND_LOGIN_REQ = 5;</code>
     */
    COMMAND_LOCAL_LOGIN_REQ(27),


    /**
     * <pre>
     * 用户撤回消息之后，推送给消息的接收方
     * </pre>
     *
     * <code>COMMAND_LOGIN_REQ = 5;</code>
     */
    COMMAND_PUSH_REVOKE_MESSAGE(28),
    COMMAND_WS_CONNECTED(29),

    COMMAND_GET_MESSAGE_CONTEXT(30),

    COMMAND_MESSAGE_NAVIGATE(31),

    //实时语音转文字
    RT_ASR(32),

    IAT_ASR(33),

    AI_CHAT_REQ(34)
    ;

    public final int getNumber() {
        return value;
    }

    public static Command valueOf(int value) {
        return forNumber(value);
    }

    public static Command forNumber(int value) {
        for (Command command : Command.values()) {
            if (command.getNumber() == value) {
                return command;
            }
        }
        return null;
    }

 /* public static Command addAndGet(String name , int value){
	  return DynamicEnumUtil.addEnum(Command.class, name,new Class[]{int.class}, new Object[]{value});
  }*/

    private final int value;

    private Command(int value) {
        this.value = value;
    }
}

