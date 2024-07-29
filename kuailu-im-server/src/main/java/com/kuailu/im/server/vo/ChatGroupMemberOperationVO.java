package com.kuailu.im.server.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.io.Serializable;
import java.util.List;

/**
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper=false)
public class ChatGroupMemberOperationVO implements Serializable {
    private static final long serialVersionUID = 2096662810254076060L;

    @NotBlank
    private String groupId;

    @NotBlank
    private String updateUser;


    private String seid;

    private List<String> deptIds;

    private List<ChatGroupMemberVo> members;
}
