package com.kuailu.im.server.util;
import com.kuailu.im.core.http.HttpConfig;
import com.kuailu.im.core.http.HttpRequest;
import com.kuailu.im.server.config.ImServerConfig;

import org.tio.core.ChannelContext;

/**
 * @author linjd
 * 2017年8月18日 下午5:47:00
 */
public class HttpServerUtils {
	/**
	 *
	 * @param request
	 * @return
	 * @author WChao
	 */
	public static HttpConfig getHttpConfig(HttpRequest request) {
		ImServerConfig imServerConfig = (ImServerConfig)request.getImChannelContext().getImConfig();
		return imServerConfig.getHttpConfig();
	}

	/**
	 * @param args
	 * @author WChao
	 */
	public static void main(String[] args) {

	}

	/**
	 *
	 * @author WChao
	 */
	public HttpServerUtils() {
	}
}
