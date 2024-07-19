package com.kuailu.im.core.dto;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class FileDto {
    private String content;
    private String fullName;
    private String pdgThumbViewer;
    private Long fileSize;
    private String suffix;
    private Long duration;
    private Integer status;
    private String receiver;
}
