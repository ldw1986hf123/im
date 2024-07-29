package com.kuailu.im.server.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

@Data
public class BaseDto {
    private Long id;

    private String createdBy="";

    private Date createdTime ;

    private String updatedBy="";

    private Date updatedTime  ;


}
