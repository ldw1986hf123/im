package com.kuailu.im.server.vo;

import com.kuailu.im.server.enums.AIChatTypeEnum;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 *
 */
@Accessors(chain = true)
@Data
public class AIUserStatusVo implements Serializable {
    /**
     * 消息ID
     */
    private Integer userStatus;
    private List<Map<String,String>> recommendCommand;


}
