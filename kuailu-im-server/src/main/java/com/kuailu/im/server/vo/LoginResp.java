package com.kuailu.im.server.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * @author liqing01
 * @version 1.0.0
 * @Description TODO
 * @createTime 2022年06月14日 18:46:00
 */
@Data
public class LoginResp implements Serializable{
	
	/**  sid(session id)  */
    private String seid;
    
    /**
     * 是否第一次登录,0-否/1-是
     */
    /**  是否第一次登录,0-否/1-是  */
    private Integer firstLogin;

    /**  用户id  */
    private String userId;
    
    /**  创建时间  */
    private long createTime;
}
