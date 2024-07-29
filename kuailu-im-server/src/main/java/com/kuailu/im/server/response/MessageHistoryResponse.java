package com.kuailu.im.server.response;

import com.kuailu.im.server.req.ChatReqParam;
import com.kuailu.im.server.req.MessageBody;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 */
@Data
@NoArgsConstructor
public class MessageHistoryResponse implements Serializable {
    private static final long serialVersionUID = -1367597924020299919L;
    /**
     * 用户ID
     */
    private String userId;
    private String groupId;
    //    private Long readMsgTime;
    private String receiver;

    private HistoryChat lastUnReadAtMsg;
    /**
     * 群组消息
     */
    private Map<String, List<ChatReqParam>> groups = new HashMap<String, List<ChatReqParam>>();

    private List<HistoryChat> chatMsgList;

    public MessageHistoryResponse(String userId, String groupId) {
        this.userId = userId;
        this.groupId = groupId;
    }
    public MessageHistoryResponse(String groupId) {
//        this.userId = userId;
        this.groupId = groupId;
    }
    public HistoryChat getHistoryInstance() {
        return new HistoryChat();
    }

    @Data
    public class HistoryChat {
        private String id;
        private String sender;
        private String senderName;
        private String receiver;
        private Integer msgType;
        private Integer chatType;
        private MessageBody messageBody;
        protected Long createdTime;
        protected Long updatedTime;
        private String groupId;
        private Integer status;
        private Integer isRead;
        private List<String> readAtMsgUserIds;  //已读了被@ 的消息的userId
    }
}
