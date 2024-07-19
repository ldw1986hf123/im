/**
 * 
 */
package com.kuailu.im.server.processor.login;

import com.kuailu.im.core.ImChannelContext;
import com.kuailu.im.core.packets.LoginReqBody;
import com.kuailu.im.core.packets.LoginRespBody;
import com.kuailu.im.core.packets.UserDto;
import com.kuailu.im.server.processor.SingleProtocolCmdProcessor;
/**
 *
 * @author WChao
 */
@Deprecated
public interface LoginCmdProcessor extends SingleProtocolCmdProcessor {
	/**
	 * 执行登录操作接口方法
	 * @param loginReqBody
	 * @param imChannelContext
	 * @return
	 */
//	 LoginRespBody doLogin(LoginReqBody loginReqBody, ImChannelContext imChannelContext);
	/**
	 * 获取用户信息接口方法
	 * @param loginReqBody
	 * @return
	 */
//	UserDto getUser(LoginReqBody loginReqBody );
	/**
	 * 登录成功(指的是J-IM会在用户校验完登陆逻辑后进行J-IM内部绑定)回调方法
	 * @param imChannelContext
	 */
	 void onSuccess(UserDto user,String seid, ImChannelContext imChannelContext);

	/**
	 * 登陆失败回调方法
	 * @param imChannelContext
	 */
	 void onFailed(ImChannelContext imChannelContext,String userId,String seid);
}
