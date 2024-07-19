package com.kuailu.im.server.vo;

import lombok.Data;

import java.util.List;

/**
 * @description:
 * @author: 林坚丁
 * @time: 2022/12/6 11:10
 */
@Data
public class UserInfoDetail {

    private String id;
    private String userId;

    private String userNo;

    private String userName;


    private String mobile;

    private String email;
    /**
     * 部门长名称
     */
    private String departmentLongName;
    /**
     * 部门长Id
     */
    private List<String> departmentLongId;
    /**
     * 职位名称
     */
    private String positionName;
    /**
     * 公司名称
     */
    private String companyName;
    /**
     * 是否管理员
     */
    private int admin;

    /** 员工状况 1-在线,2-离线,3-请假,4-外出,5-出差 */
    private Integer staffStatus;
    private String staffStatusName;
    private String statusDesc;
}
