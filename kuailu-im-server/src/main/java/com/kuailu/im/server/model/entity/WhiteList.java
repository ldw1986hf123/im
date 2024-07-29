package com.kuailu.im.server.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 * 白名单表
 * </p>
 *
 * @author liangdl
 * @since 2023-05-25
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("im_white_list")
public class WhiteList implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @TableField("user_id")
    private String userId;

    @TableField("created_by")
    private String createdBy;

    @TableField("created_time")
    private Date createdTime;

    @TableField("updated_by")
    private String updatedBy;

    @TableField("updated_time")
    private Date updatedTime;

}
