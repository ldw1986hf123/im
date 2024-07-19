/**
 * 
 */
package com.kuailu.im.core;

public enum ImStatus implements Status{
	
	OK(10000,"ok","成功"),
	C10001(10001,"offline","用户不在线"),
	C10019(10019,"online","用户在线"),
	DATA_FORMAT_ERROR(10002,"send failed","消息发送失败,数据格式不正确,请参考:{'from':来源ID,'to':目标ID,'cmd':'消息命令码','createTime':消息创建时间(Long型),'msgType':Integer消息类型,'content':内容}"),
	C10003(10003,"ok","获取用户信息成功!"),
	C10004(10004,"get user failed !","获取用户信息失败!"),
//	C10005(10005,"ok","获取所有在线用户信息成功!"),
//	C10006(10006,"ok","获取所有离线用户信息成功!"),
	LOGIN_SUCCESS(10007,"ok","登录成功!"),
	C10008(10008,"login failed !","登录失败!"),


	C10009(10009,"ok","鉴权成功!"),
	NEED_LOGIN(10010,"未登录，","权限不足，请先登录"),
	C10011(10011,"join group ok!","加入群组成功!"),
	C10012(10012,"join group failed!","加入群组失败!"),
	C10013(10013,"Protocol version number does not match","协议版本号不匹配!"),
	C10014(10014,"unsupported cmd command","不支持的cmd命令!"),
	C10015(10015,"get user message failed!","获取用户消息失败!"),
	C10016(10016,"get user message ok!","获取离线消息成功!"),
	C10017(10017,"cmd failed!","未知的cmd命令!"),
	C10018(10018,"get user message ok!","获取历史消息成功!"),
	INVALID_VERIFICATION(10020,"Invalid verification!","不合法校验"),
	C10021(10021,"close ok!","关闭成功"),
	CANNOT_FIND_DATA(10023,"find not data!","找不到数据"),

//	LOGIN_EXPIRE(10024,"登录失效!","登录失效!"),

	GROUP_MUST_MORE_THAN_TWO(10025,"会话创建失败","会话创建失败"),


	ERROR(-1,"Unknow exception!","系统繁忙，请稍后再试"),


	FILE_UPLOAD(10026, "文件上传错误","文件上传错误"),
	FILE_MISS_CHUNKS(10027, "文件部分模块上传错误","文件部分模块上传错误"),

	NOT_WHITE_LIST(10028,"no white list","您不在对方的白名单中，不可发送消息。"),

	CAN_NOT_INVITE_GROUP(10029,"no white list","您不在对方的白名单中，不可邀请入群。"),
	;


	
	private int status;
	
	private String description;
	
	private String text;

	ImStatus(int status, String description, String text) {
		this.status = status;
		this.description = description;
		this.text = text;
	}
	
	public int getStatus() {
		return status;
	}
	
	public String getDescription() {
		return description;
	}
	
	public String getText() {
		return text;
	}
	
	@Override
	public int getCode() {
		return this.status;
	}

	@Override
	public String getMsg() {
		return this.getDescription()+" "+this.getText();
	}
}
