package com.kuailu.im.server.util;

import lombok.Data;

import java.util.Date;

@Data
public class StudentEntity {
    /**
     * 姓名
     */
    private String name;
    /**
     * 年龄
     */
    private Integer age;
    /**
     * 生日
     */
    private Date birthday;
}
