/**
 * 
 */
package com.kuailu.im.server.config;


import com.kuailu.im.core.http.HttpConfig;
import com.kuailu.im.core.utils.PropUtil;
import com.kuailu.im.core.ws.WsConfig;

/**
 */
public class PropertyImServerConfigBuilder extends ImServerConfigBuilder<ImServerConfig,PropertyImServerConfigBuilder> {
	
	public PropertyImServerConfigBuilder(String file) {
		PropUtil.use(file);
	}
	
	@Override
	public PropertyImServerConfigBuilder configHttp(HttpConfig httpConfig)throws Exception{
		httpConfig.setBindPort(PropUtil.getInt("jim.port"));
		//设置web访问路径;html/css/js等的根目录，支持classpath:，也支持绝对路径
		httpConfig.setPageRoot(PropUtil.get("jim.http.page"));
		//不缓存资源;
		httpConfig.setMaxLiveTimeOfStaticRes(PropUtil.getInt("jim.http.max.live.time"));
		//设置j-im mvc扫描目录;
		httpConfig.setScanPackages(PropUtil.get("jim.http.scan.packages").split(","));
		return this;
	}

	@Override
	public PropertyImServerConfigBuilder configWs(WsConfig wsConfig)throws Exception{

		return this;
	}

	@Override
	protected PropertyImServerConfigBuilder getThis() {
		return this;
	}

	@Override
	public ImServerConfig build()throws Exception {
		super.build();
		return ImServerConfig.newBuilder()
				.bindIp(PropUtil.get("jim.bind.ip"))
				.bindPort(PropUtil.getInt("jim.port"))
				.heartbeatTimeout(PropUtil.getLong("jim.heartbeat.timeout"))
				.isStore(PropUtil.get("jim.store"))
				.httConfig(this.httpConfig)
				.wsConfig(this.wsConfig)
				.serverListener(this.serverListener)
				.kuailuApiUrl(PropUtil.get("kuailu.api.url"))
				.isCluster(PropUtil.get("jim.cluster")).build();
	}
}
