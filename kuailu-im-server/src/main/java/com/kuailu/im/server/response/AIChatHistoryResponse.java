package com.kuailu.im.server.response;

import com.kuailu.im.server.model.entity.AIChatMsg;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 *
 */
@Data
public class AIChatHistoryResponse implements Serializable {
    /**
     * 用户ID
     */
    private List<AIChatHistoryDto> messageList;
    private String groupId;
    private String receiver;


}
