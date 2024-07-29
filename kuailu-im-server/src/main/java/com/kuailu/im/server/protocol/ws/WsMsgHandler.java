package com.kuailu.im.server.protocol.ws;

import com.kuailu.im.core.ImChannelContext;
import com.kuailu.im.core.ImConst;
import com.kuailu.im.core.ImPacket;
import com.kuailu.im.server.req.ChatReqParam;
import com.kuailu.im.core.ws.*;
import com.kuailu.im.server.util.ChatKit;

import com.kuailu.im.server.JimServerAPI;
import com.kuailu.im.server.protocol.ProtocolManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
/**
 */
@Deprecated
public class WsMsgHandler implements IWsMsgHandler {
	private static Logger log = LoggerFactory.getLogger(WsMsgHandler.class);

	private WsConfig wsServerConfig = null;

	/**
	 * 
	 * @param text
	 * @param imChannelContext
	 * @return 可以是WsResponsePacket、String、null
	 * @author: linjd
	 */
	@Override
	public Object onText(WsRequestPacket wsRequestPacket, String text, ImChannelContext imChannelContext) throws Exception {
		ChatReqParam chatBody = ChatKit.toChatBody(wsRequestPacket.getBody(), imChannelContext);
		String toId = chatBody.getReceiver();
		if(ChatKit.isOnline(toId)){
			JimServerAPI.sendToUser(toId, wsRequestPacket);
			ImPacket sendSuccessPacket = ProtocolManager.Packet.success(imChannelContext);
			text = new String(sendSuccessPacket.getBody(), ImConst.Http.CHARSET_NAME);
		}else{
			ImPacket offlineRespPacket = ProtocolManager.Packet.offline(imChannelContext);
			text = new String(offlineRespPacket.getBody(), ImConst.Http.CHARSET_NAME);
		}
		return text;
	}

	/**
	 * 
	 * @param webSocketPacket
	 * @param bytes
	 * @param imChannelContext
	 * @return 可以是WsResponsePacket、byte[]、ByteBuffer、null
	 * @author: linjd
	 */
	@Override
	public Object onBytes(WsRequestPacket webSocketPacket, byte[] bytes, ImChannelContext imChannelContext) throws Exception {
		String text = new String(bytes, "utf-8");
		log.info("收到byte消息:{},{}", bytes, text);
		ByteBuffer buffer = ByteBuffer.allocate(bytes.length);
		buffer.put(bytes);
		return buffer;
	}

	/** 
	 * @param imChannelContext
	 * @return
	 * @throws Exception
	 * @author: linjd
	 */
	@Override
	public WsResponsePacket handler(ImPacket imPacket, ImChannelContext imChannelContext)throws Exception {
		WsRequestPacket wsRequest = (WsRequestPacket)imPacket;
		return h(wsRequest, wsRequest.getBody(), wsRequest.getWsOpcode(), imChannelContext);
	}
	
	public WsResponsePacket h(WsRequestPacket wsRequest, byte[] bytes, Opcode opcode, ImChannelContext imChannelContext) throws Exception {
		WsResponsePacket wsResponse = null;
		if (opcode == Opcode.TEXT) {
			if (bytes == null || bytes.length == 0) {
				JimServerAPI.remove(imChannelContext, "错误的webSocket包，body为空");
				return null;
			}
			String text = new String(bytes, wsServerConfig.getCharset());
			Object retObj = this.onText(wsRequest, text, imChannelContext);
			String methodName = "onText";
			wsResponse = processRetObj(retObj, methodName, imChannelContext);
			return wsResponse;
		} else if (opcode == Opcode.BINARY) {
			if (bytes == null || bytes.length == 0) {
				JimServerAPI.remove(imChannelContext, "错误的webSocket包，body为空");
				return null;
			}
			Object retObj = this.onBytes(wsRequest, bytes, imChannelContext);
			String methodName = "onBytes";
			wsResponse = processRetObj(retObj, methodName, imChannelContext);
			return wsResponse;
		} else if (opcode == Opcode.PING || opcode == Opcode.PONG) {
			log.error("收到" + opcode);
			return null;
		} else if (opcode == Opcode.CLOSE) {
			Object retObj = this.onClose(wsRequest, bytes, imChannelContext);
			String methodName = "onClose";
			wsResponse = processRetObj(retObj, methodName, imChannelContext);
			return wsResponse;
		} else {
			JimServerAPI.remove(imChannelContext, "错误的webSocket包，错误的Opcode");
			return null;
		}
	}

	private WsResponsePacket processRetObj(Object obj, String methodName, ImChannelContext imChannelContext) throws Exception {
		WsResponsePacket wsResponse;
		if (obj == null) {
			return null;
		} else {
			if (obj instanceof String) {
				String str = (String) obj;
				wsResponse = new WsResponsePacket();
				wsResponse.setBody(str.getBytes(wsServerConfig.getCharset()));
				wsResponse.setWsOpcode(Opcode.TEXT);
				return wsResponse;
			} else if (obj instanceof byte[]) {
				wsResponse = new WsResponsePacket();
				wsResponse.setBody((byte[]) obj);
				wsResponse.setWsOpcode(Opcode.BINARY);
				return wsResponse;
			} else if (obj instanceof WsResponsePacket) {
				return (WsResponsePacket) obj;
			} else if (obj instanceof ByteBuffer) {
				wsResponse = new WsResponsePacket();
				byte[] bs = ((ByteBuffer) obj).array();
				wsResponse.setBody(bs);
				wsResponse.setWsOpcode(Opcode.BINARY);
				return wsResponse;
			} else {
				log.error("{} {}.{}()方法，只允许返回byte[]、ByteBuffer、WebSocketResponsePacket或null，但是程序返回了{}", imChannelContext, this.getClass().getName(), methodName, obj.getClass().getName());
				return null;
			}
		}
		
	}
	@Override
	public Object onClose(WsRequestPacket webSocketPacket, byte[] bytes, ImChannelContext imChannelContext) throws Exception {
		JimServerAPI.remove(imChannelContext, "receive close flag");
		return null;
	}

	/**
	 * 
	 * @author: linjd
	 */
	public WsMsgHandler(WsConfig wsServerConfig, String[] scanPackages) {
		this.setWsServerConfig(wsServerConfig);
		//this.routes = new Routes(scanPackages);
	}
	public WsMsgHandler() {
		this(WsConfig.newBuilder().build(), null);
	}

	/**
	 * @return the wsServerConfig
	 */
	public WsConfig getWsServerConfig() {
		return wsServerConfig;
	}

	/**
	 * @param wsServerConfig the wsServerConfig to set
	 */
	public void setWsServerConfig(WsConfig wsServerConfig) {
		this.wsServerConfig = wsServerConfig;
	}

}
