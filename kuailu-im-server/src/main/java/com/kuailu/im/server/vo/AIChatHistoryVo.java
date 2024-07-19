package com.kuailu.im.server.vo;

import cn.hutool.json.JSONObject;
import com.kuailu.im.server.enums.AIChatTypeEnum;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 *
 */
@Accessors(chain = true)
@Data
public class AIChatHistoryVo implements Serializable {
    /**
     * 消息ID
     */
    private String messageId;
    private List content;
    private String questionMsgId;
    private Integer type= AIChatTypeEnum.ANSWER.getCode();
    private String topicId;
    private Integer answerStatus;   //只有type是answer的时候，这个字段才有效
    private Long createdTime;
    private Boolean topicClosed;        //上个话题是否已经关闭
    private Integer operation;
    private List<AIChatHistoryVo> answerList;
    List<String> recommendCommand=new ArrayList<>();//推荐指令

}
