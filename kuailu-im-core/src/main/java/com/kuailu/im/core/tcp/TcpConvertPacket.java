/**
 * 
 */
package com.kuailu.im.core.tcp;

import com.kuailu.im.core.ImChannelContext;
import com.kuailu.im.core.ImPacket;
import com.kuailu.im.core.ImSessionContext;
import com.kuailu.im.core.packets.Command;
import com.kuailu.im.core.protocol.IProtocolConverter;

/**
 * TCP协议消息转化包
 * @author linjd
 *
 */
public class TcpConvertPacket implements IProtocolConverter {

	/**
	 * 转TCP协议响应包;
	 */
	@Override
	public ImPacket RespPacket(byte[] body, Command command, ImChannelContext imChannelContext) {
		ImSessionContext sessionContext = imChannelContext.getSessionContext();
		if(sessionContext instanceof TcpSessionContext){
			TcpPacket tcpPacket = new TcpPacket(command,body);
			TcpServerEncoder.encode(tcpPacket, imChannelContext.getImConfig(), imChannelContext);
			tcpPacket.setCommand(command);
			return tcpPacket;
		}
		return null;
	}

	@Override
	public ImPacket RespPacket(ImPacket imPacket, Command command, ImChannelContext imChannelContext) {

		return this.RespPacket(imPacket.getBody(), command, imChannelContext);
	}

	/**
	 * 转TCP协议请求包;
	 */
	@Override
	public ImPacket ReqPacket(byte[] body, Command command, ImChannelContext channelContext) {
		Object sessionContext = channelContext.getSessionContext();
		if(sessionContext instanceof TcpSessionContext){
			TcpPacket tcpPacket = new TcpPacket(command,body);
			TcpServerEncoder.encode(tcpPacket, channelContext.getImConfig(), channelContext);
			tcpPacket.setCommand(command);
			return tcpPacket;
		}
		return null;
	}

}
