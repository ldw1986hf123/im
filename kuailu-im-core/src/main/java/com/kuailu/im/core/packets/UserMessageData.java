package com.kuailu.im.core.packets;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.*;

/**

 这个类是应该最终废弃掉的
 */
@Data
@Deprecated
public class UserMessageData implements Serializable {
    private static final long serialVersionUID = -1367597924020299919L;
    /**
     * 用户ID
     */
    private String userId;
    private String groupId;
    private Long readMsgTime;
    /**
     * 群组消息
     */
    private Map<String, List<ChatBody>> groups = new HashMap<String, List<ChatBody>>();

    private List<HistoryChatList> chatMsgList;

    public UserMessageData(String userId) {
        this.userId = userId;
    }

    public UserMessageData(String userId, String groupId) {
        this.userId = userId;
        this.groupId = groupId;
    }

    class HistoryChatList {
        private String id;
        private String sender;
        private String receiver;
        private Integer msgType;
        private Integer chatType;
        private List messageBody;
        protected LocalDateTime createTime;
        private String groupId;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getSender() {
            return sender;
        }

        public void setSender(String sender) {
            this.sender = sender;
        }

        public String getReceiver() {
            return receiver;
        }

        public void setReceiver(String receiver) {
            this.receiver = receiver;
        }

        public Integer getMsgType() {
            return msgType;
        }

        public void setMsgType(Integer msgType) {
            this.msgType = msgType;
        }

        public Integer getChatType() {
            return chatType;
        }

        public void setChatType(Integer chatType) {
            this.chatType = chatType;
        }

        public List getMessageBody() {
            return messageBody;
        }

        public void setMessageBody(List messageBody) {
            this.messageBody = messageBody;
        }

        public LocalDateTime getCreateTime() {
            return createTime;
        }

        public void setCreateTime(LocalDateTime createTime) {
            this.createTime = createTime;
        }

        public String getGroupId() {
            return groupId;
        }

        public void setGroupId(String groupId) {
            this.groupId = groupId;
        }
    }


   /* public  void buildChatList(List<ChatMsg> chatMsgList) {
        List<HistoryChatList> historyChatLists= JSONUtil.toList(JSONUtil.parseArray(chatMsgList),HistoryChatList.class);
        this.setChatMsgList(historyChatLists);
    }*/


}
