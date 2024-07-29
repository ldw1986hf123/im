package com.kuailu.im.server.handler;

import com.kuailu.im.core.ImChannelContext;
import com.kuailu.im.core.ImConst;
import com.kuailu.im.core.ImPacket;
import com.kuailu.im.core.config.ImConfig;
import com.kuailu.im.core.exception.ImDecodeException;
import com.kuailu.im.core.packets.Command;
import com.kuailu.im.core.tcp.TcpPacket;
import com.kuailu.im.core.tcp.TcpServerDecoder;
import com.kuailu.im.core.tcp.TcpServerEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;

/**
 *
 * 版本: [1.0]
 * 功能说明:
 * 作者: WChao 创建时间: 2017年8月30日 下午1:10:28
 */
public class HelloImClientHandler implements ImClientHandler, ImConst
{
	private static Logger logger = LoggerFactory.getLogger(HelloImClientHandler.class);

	/**
	 * 处理消息
	 */
	@Override
	public void handler(ImPacket imPacket, ImChannelContext channelContext){
		TcpPacket helloPacket = (TcpPacket)imPacket;
		byte[] body = helloPacket.getBody();
		if (body != null)
		{
			try {
				String str = new String(body, ImConst.CHARSET);
				logger.info("demo客户端收到消息:{}", str);
			}catch (Exception e){
				logger.error(e.getMessage(), e);
			}
		}

		return;
	}

	/**
	 * 编码：把业务消息包编码为可以发送的ByteBuffer
	 * 总的消息结构：消息头 + 消息体
	 * 消息头结构：    4个字节，存储消息体的长度
	 * 消息体结构：   对象的json串的byte[]
	 */
	@Override
	public ByteBuffer encode(ImPacket imPacket, ImConfig imConfig, ImChannelContext imChannelContext)
	{
		TcpPacket tcpPacket = (TcpPacket)imPacket;
		return TcpServerEncoder.encode(tcpPacket, imConfig, imChannelContext);
	}

	@Override
	public TcpPacket decode(ByteBuffer buffer, int limit, int position, int readableLength, ImChannelContext imChannelContext) throws ImDecodeException {
		TcpPacket tcpPacket = TcpServerDecoder.decode(buffer, imChannelContext);
		return tcpPacket;
	}


	/**
	 * 此方法如果返回null，框架层面则不会发心跳；如果返回非null，框架层面会定时发本方法返回的消息包
	 */
	@Override
	public TcpPacket heartbeatPacket(ImChannelContext imChannelContext)
	{
		return null;
	}

}
