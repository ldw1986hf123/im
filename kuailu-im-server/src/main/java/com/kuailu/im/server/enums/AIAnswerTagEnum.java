package com.kuailu.im.server.enums;

public enum AIAnswerTagEnum {
//     [sql]  [table] [text]    [summary]

    SQL("data:[sql]", ""),
    TABLE("data:[table]", ""),

    SUMMARY("data:[summary]", "[text]"),

    DONE("[DONE]", ""),

    ;
    private String frontTag;
    private String tag;

    AIAnswerTagEnum(String tag, String frontTag) {
        this.tag = tag;
        this.frontTag = frontTag;
    }

    public String getFrontTag() {
        return frontTag;
    }

    public String getTag() {
        return tag;
    }


}
