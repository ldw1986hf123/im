/**
 * 
 */
package com.kuailu.im.core.packets;

import com.kuailu.im.core.ImStatus;
import com.kuailu.im.core.Status;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * 版本: [1.0]
 * 功能说明: 
 * 作者: WChao 创建时间: 2017年9月12日 下午3:15:28
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class  LoginRespBody extends RespBody {
	
	private static final long serialVersionUID = 1L;
	private String token;
	private UserDto user;

	/*public LoginRespBody(){
		this.setCmd(Command.COMMAND_LOGIN_RESP);
	}*/

	public LoginRespBody(Status status){
		this(status,null);
	}

	public LoginRespBody(Status status , UserDto user){
		this(status, user, null);
	}

	public LoginRespBody(Status status , UserDto user, String token){
//		super(Command.COMMAND_LOGIN_RESP, status);
		this.user = user;
		this.token = token;
	}

	public UserDto getUser() {
		return user;
	}

	public void setUser(UserDto user) {
		this.user = user;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public static LoginRespBody success(){
		 return new LoginRespBody(ImStatus.LOGIN_SUCCESS);

	}

	public static LoginRespBody failed(){
		return new LoginRespBody(ImStatus.C10008);
	}
	public static LoginRespBody failed(ImStatus imStatus){
		return new LoginRespBody(imStatus);
	}
	public static LoginRespBody failed(String msg){
		LoginRespBody loginRespBody = new LoginRespBody(ImStatus.C10008);
		loginRespBody.setMsg(msg);
		return loginRespBody;
	}
}
