package com.kuailu.im.core.packets;

/**
 * <pre>
 * *
 * 聊天类型
 * </pre>
 */
public enum ChatType {

    CHAT_TYPE_PRIVATE(0),

    CHAT_TYPE_PUBLIC(1),

    CHAT_TYPE_MSG_HELPER(2,"消息助手"),

    FILE_HELPER(3, "文件共享助手"),

    XIAOLU_AI(4, "小鹭AI助手"),
    ;

    public int getValue() {
        return value;
    }

    public String getName() {
        return name;
    }

    public final int getNumber() {
        return value;
    }

    public final String getName(ChatType chatType) {
        return chatType.name;
    }

  /*  public static ChatType valueOf(int value) {
        return forNumber(value);
    }

    public static ChatType forNumber(int value) {
        switch (value) {
            case 0:
                return CHAT_TYPE_PRIVATE;
            case 1:
                return CHAT_TYPE_PUBLIC;
            case 2:
                return CHAT_TYPE_MSG_HELPER;
            default:
                return null;
        }
    }*/

    private final int value;
    private String name;

    ChatType(int value) {
        this.value = value;
    }

    ChatType(int value, String name) {
        this.value = value;
        this.name = name;
    }
}

