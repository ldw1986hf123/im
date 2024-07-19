package com.kuailu.im.server.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import java.io.Serializable;


@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper=false)
public class MsgNotifyVo implements Serializable {

    private static final long serialVersionUID = 7041132621966527207L;

    private String seid;

    private String userId;

    private String content;

    private String pushData;

}
