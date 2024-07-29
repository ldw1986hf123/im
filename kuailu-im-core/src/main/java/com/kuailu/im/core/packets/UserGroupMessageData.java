package com.kuailu.im.core.packets;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * @description:
 * @author: 林坚丁
 * @time: 2023/2/20 21:46
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Deprecated
public class UserGroupMessageData implements Serializable {
    private static final long serialVersionUID = -3026152041586841066L;
    private String userId;
    private String groupId;
//    private String lastMsgId;
    private Long readMsgTime;
//    private List<ChatBody> chatMsgList;

//   private List<ChatGroupMessageDataList> messageDataLists=new ArrayList<>();
//
//    @Data
//    @Builder
//    @AllArgsConstructor
//    @NoArgsConstructor
//    public class ChatGroupMessageDataList implements Serializable {
//
//        private static final long serialVersionUID = -6442954720005579617L;
//        private String groupId;
//        private List<ChatBody> messageList;
//    }
}
