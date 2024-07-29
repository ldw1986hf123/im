package com.kuailu.im.server.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

@Data
@Accessors(chain = true)
@TableName("im_file")
public class File extends BaseEntity {
    private String content;
    private String fullName;
    private String pdgThumbViewer;
    private Long fileSize;
    private String suffix;
    private Long duration;
    private Integer status;
    private String receiver;
    private String sender;
    private Integer orientation;//0竖图 1横图
}
