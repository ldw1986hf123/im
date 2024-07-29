/**
 *
 */
package com.kuailu.im.core.packets;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class LoginReqBody extends Message {

    private static final long serialVersionUID = -10113316720288444L;
    /**
     * 用户Id
     */
    private String userId;

    /**
     * 用户sid
     */

    private String seid;

    /**
     * web,app,h5;
     */
    private String clientType;

    /**
     * 用户工号
     */
    private String userNo;
    /**
     * 密码
     */
    private String password;
    /**
     * 登陆token
     */
    private String token;
    /**
     * 员工名称
     */
    private String userName;
    /**
     * 设备id
     */
    private String deviceId;

}
