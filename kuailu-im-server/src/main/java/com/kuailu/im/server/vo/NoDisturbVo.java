package com.kuailu.im.server.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.List;

/**
 * @description:
 * @author: liangdl
 * @time: 2023-05-17
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper=false)
//@Builder
public class NoDisturbVo implements Serializable {

    private static final long serialVersionUID = 7041132621966527207L;

    private  String userId;

    private  String conversationId;
}
