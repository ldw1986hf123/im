package com.kuailu.im.server.req;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class MessageBody {
    private String content;
    private List<Map<String,Object>> mergeMessageList;
    private String mergedTitle;
    private Integer mergeLevel;
    private String mergeEntityId;
    private String fullName;
    private String pdgThumbViewer;
    private Long fileSize;
    private String suffix;
    private Long duration;
    private String mainFile;
    private String attachFile;
    private String cover;
    private Integer orientation;
    private Integer chatType;
    private Integer original;
    private List<Map<String,Object>> atUsersInfo;  //@所有有人 AtAllUserTag
    private List<String> atUserList;   //@所有有人 AtAllUserTag

    private String atUserIdContent;   // userId 替换了用户名之后的消息内容
    private boolean isAtSelf;
}
