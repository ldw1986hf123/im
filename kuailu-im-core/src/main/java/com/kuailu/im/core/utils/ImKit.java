/**
 * 
 */
package com.kuailu.im.core.utils;

import com.kuailu.im.core.ImChannelContext;
import com.kuailu.im.core.ImSessionContext;
import com.kuailu.im.core.packets.ImClientNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Objects;


public class ImKit {
	
	private static Logger logger = LoggerFactory.getLogger(ImKit.class);

	/**
	 * 设置Client对象到ImSessionContext中
	 * @param channelContext 通道上下文
	 * @return 客户端Node信息
	 * @author: WChao
	 */
	public static ImClientNode initImClientNode(ImChannelContext channelContext) {
		ImSessionContext imSessionContext = channelContext.getSessionContext();
		ImClientNode imClientNode = imSessionContext.getImClientNode();
		if(Objects.nonNull(imClientNode)){
			return imClientNode;
		}
//		imClientNode = ImClientNode.newBuilder().id(channelContext.getId()).ip(channelContext.getClientNode().getIp())
//		.port(channelContext.getClientNode().getPort()).build();
		imClientNode =new ImClientNode();
		imClientNode.setId(channelContext.getId());
		imClientNode.setIp(channelContext.getClientNode().getIp());
		imClientNode.setPort(channelContext.getClientNode().getPort());
		imSessionContext.setImClientNode(imClientNode);
		return imClientNode;
	}

}
