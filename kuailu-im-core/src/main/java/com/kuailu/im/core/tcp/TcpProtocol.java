/**
 * 
 */
package com.kuailu.im.core.tcp;

import java.nio.ByteBuffer;

import com.kuailu.im.core.ImChannelContext;
import com.kuailu.im.core.ImPacket;
import com.kuailu.im.core.ImSessionContext;
import com.kuailu.im.core.exception.ImException;
import com.kuailu.im.core.protocol.AbstractProtocol;
import com.kuailu.im.core.protocol.IProtocolConverter;
import com.kuailu.im.core.utils.ImKit;

/**
 * @desc Tcp协议校验器
 * @author linjd
 * @date 2018-05-01
 */
public class TcpProtocol extends AbstractProtocol {

	public TcpProtocol(IProtocolConverter converter){
		super(converter);
	}

	@Override
	public String name() {
		return Protocol.TCP;
	}

	@Override
	protected void init(ImChannelContext imChannelContext) {
		imChannelContext.setSessionContext(new TcpSessionContext(imChannelContext));
		ImKit.initImClientNode(imChannelContext);
	}

	@Override
	public boolean validateProtocol(ImSessionContext imSessionContext) throws ImException {
		if(imSessionContext instanceof TcpSessionContext){
			return true;
		}
		return false;
	}

	@Override
	public boolean validateProtocol(ByteBuffer buffer, ImChannelContext imChannelContext) throws ImException {
		//获取第一个字节协议版本号,TCP协议;
		if(buffer.get() == Protocol.VERSION){
			return true;
		}
		return false;
	}

	@Override
	public boolean validateProtocol(ImPacket imPacket) throws ImException {
		if(imPacket instanceof TcpPacket){
			return true;
		}
		return false;
	}

}
