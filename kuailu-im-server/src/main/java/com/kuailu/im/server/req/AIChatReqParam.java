/**
 * 
 */
package com.kuailu.im.server.req;

import cn.hutool.json.JSONUtil;
import com.kuailu.im.core.packets.ChatType;
import com.kuailu.im.core.packets.Command;
import com.kuailu.im.core.packets.Message;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;


@Data
public class AIChatReqParam extends Message {
	private static final long serialVersionUID = 5731474214655476286L;
	String content;
	String sender;
	String topicId;
	String flag;//中止生成的时候传“pause”


}
