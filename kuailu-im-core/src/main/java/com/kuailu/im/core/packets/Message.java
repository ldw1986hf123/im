/**
 * 
 */
package com.kuailu.im.core.packets;

import com.alibaba.fastjson.JSONObject;
import com.kuailu.im.core.utils.JsonKit;
import lombok.Data;

import java.io.Serializable;

/**
 */
@Data
public class Message implements Serializable{
	
	private static final long serialVersionUID = -6375331164604259933L;
	/**
	 * 消息创建时间
	 * new Date().getTime()
	 */
	protected Long createdTime;
	/**
	 * 消息id，全局唯一
	 * UUIDSessionIdGenerator.instance.sessionId(null)
	 */
	protected String id ;
	/**
	 * 消息cmd命令码
	 */
	protected Integer cmd ;
	/**
	 * 扩展参数字段
	 */
	protected JSONObject extras;

	private String version;


	public byte[] toByte(){
		return JsonKit.toJsonBytes(this);
	}
	

}
