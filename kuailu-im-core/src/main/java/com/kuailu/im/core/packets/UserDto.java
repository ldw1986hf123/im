/**
 * 
 */
package com.kuailu.im.core.packets;

import cn.hutool.core.bean.BeanUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 功能说明: 
 * @author : 林坚丁 创建时间: 2022/11/30 18:38
 */

@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class UserDto extends Message implements Serializable{
	
	private static final long serialVersionUID = 1L;

	/**
	 * 用户id;
	 */
	private String userId;
	/**
	 * 用户工号;
	 */
	private String userNo;
	/**
	 * user nick
	 */
	private String userName;
	/**
	 * 用户头像
	 */
	private String avatar;
	/**
	 * 在线状态(online、offline)
	 */
	private String status = UserStatusType.OFFLINE.getStatus();

	/**
	 * 用户所属终端;(ws、tcp、http、android、ios等)
	 */
	private String terminal;
	/**
	 * 群组列表;
	 */
	private List<GroupDto> groups=new ArrayList<>();

	public UserDto clone(){
		UserDto cloneUser = UserDto.builder().build();
		BeanUtil.copyProperties(this, cloneUser,"groups");
		return cloneUser;
	}
}
