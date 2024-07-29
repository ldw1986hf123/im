package com.kuailu.im.server.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @description:
 * @author: liangdl
 * @time: 2023-07-06
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper=false)
//@Builder
public class SearchRecordClearVo implements Serializable {

    private static final long serialVersionUID = 7041132621966527207L;

    private  String userId;
}
