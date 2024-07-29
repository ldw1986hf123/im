/**
 * 
 */
package com.kuailu.im.core.http;

import java.nio.ByteBuffer;
import com.kuailu.im.core.ImChannelContext;
import com.kuailu.im.core.ImPacket;
import com.kuailu.im.core.ImSessionContext;
import com.kuailu.im.core.exception.ImException;
import com.kuailu.im.core.http.session.HttpSession;
import com.kuailu.im.core.protocol.AbstractProtocol;
import com.kuailu.im.core.protocol.IProtocolConverter;
import com.kuailu.im.core.utils.ImKit;

/**
 *
 * @desc Http协议校验器
 * @author linjd
 * @date 2018-05-01
 */
public class HttpProtocol extends AbstractProtocol {

	@Override
	public String name() {
		return Protocol.HTTP;
	}

	public HttpProtocol(IProtocolConverter protocolConverter){
		super(protocolConverter);
	}

	@Override
	protected void init(ImChannelContext imChannelContext) {
		imChannelContext.setSessionContext(new HttpSession(imChannelContext));
		ImKit.initImClientNode(imChannelContext);
	}

	@Override
	public boolean validateProtocol(ImSessionContext imSessionContext) throws ImException {
		if(imSessionContext instanceof HttpSession) {
			return true;
		}
		return false;
	}

	@Override
	public boolean validateProtocol(ByteBuffer buffer, ImChannelContext imChannelContext) throws ImException {
		HttpRequest request = HttpRequestDecoder.decode(buffer, imChannelContext,false);
		if(request.getHeaders().get(Http.RequestHeaderKey.Sec_WebSocket_Key) == null)
		{
			return true;
		}
		return false;
	}

	@Override
	public boolean validateProtocol(ImPacket imPacket) throws ImException {
		if(imPacket instanceof HttpPacket){
			return true;
		}
		return false;
	}

}
