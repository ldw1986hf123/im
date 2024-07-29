package com.kuailu.im.server.vo;

import lombok.Builder;
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
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper=false)
//@Builder
public class ChatGroupVo implements Serializable {
    private static final long serialVersionUID = 7041132621966527207L;

    private  String seid;

    private  String groupId;

    @Size(max = 2000)
    private String groupName;

    @Size(max = 500)
    private String avatar;

    @NotNull
    private int chatType;

    @NotBlank
    @Size(max = 128)
    private String owner;

    @Min(2)
    private int maxMembers;

    @Size(max = 128)
    private String description;

    @Size(max = 2000)
    private String ext;

    private List<String> deptIds;

    private List<ChatGroupMemberVo> members;


    private String updateUser;


}
