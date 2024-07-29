/**
 * 
 */
package com.kuailu.im.server.processor.handshake;


import com.kuailu.im.core.ImChannelContext;
import com.kuailu.im.core.ImPacket;
import com.kuailu.im.core.ImSessionContext;
import com.kuailu.im.core.exception.ImException;
import com.kuailu.im.core.packets.Command;
import com.kuailu.im.core.packets.HandshakeBody;
import com.kuailu.im.core.packets.RespBody;
import com.kuailu.im.core.tcp.TcpSessionContext;
import com.kuailu.im.server.protocol.AbstractProtocolCmdProcessor;
import com.kuailu.im.server.protocol.ProtocolManager;

/**
 * 版本: [1.0]
 * 功能说明: 
 * 作者: WChao 创建时间: 2017年9月11日 下午8:11:34
 */
public class TcpHandshakeProcessor extends AbstractProtocolCmdProcessor implements HandshakeCmdProcessor{

	@Override
	public ImPacket handshake(ImPacket packet, ImChannelContext channelContext) throws ImException {
		RespBody handshakeBody = new RespBody(Command.COMMAND_HANDSHAKE_RESP,new HandshakeBody(Protocol.HANDSHAKE_BYTE));
		ImPacket handshakePacket = ProtocolManager.Converter.respPacket(handshakeBody,channelContext);
		return handshakePacket;
	}

	/**
	 * 握手成功后
	 * @param packet
	 * @param channelContext
	 * @throws ImException
	 * @author Wchao
	 */
	@Override
	public void onAfterHandshake(ImPacket packet, ImChannelContext channelContext)throws ImException {
		
	}

	@Override
	public boolean isProtocol(ImChannelContext channelContext){
		ImSessionContext sessionContext = channelContext.getSessionContext();
		if(sessionContext == null){
			return false;
		}else if(sessionContext instanceof TcpSessionContext){
			return true;
		}
		return false;
	}
	
}
