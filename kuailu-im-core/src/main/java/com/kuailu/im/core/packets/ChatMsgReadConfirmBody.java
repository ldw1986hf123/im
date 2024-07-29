package com.kuailu.im.core.packets;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatMsgReadConfirmBody extends Message {

    private static final long serialVersionUID = -7715994643986927377L;
    @Deprecated
    private String userId;

    @Deprecated
    private String otherUserId;


    private String groupId;
    private String msgId;
    private Long readMsgTime;

}
