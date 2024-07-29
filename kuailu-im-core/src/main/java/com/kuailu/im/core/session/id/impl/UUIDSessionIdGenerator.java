package com.kuailu.im.core.session.id.impl;

import com.kuailu.im.core.http.HttpConfig;
import com.kuailu.im.core.session.id.ISessionIdGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

/**
 * @author linjd
 * 2017年8月15日 上午10:53:39
 */
public class UUIDSessionIdGenerator implements ISessionIdGenerator {
	private static Logger log = LoggerFactory.getLogger(UUIDSessionIdGenerator.class);

	public final static UUIDSessionIdGenerator instance = new UUIDSessionIdGenerator();

	/**
	 * @param args
	 * @author linjd
	 */
	public static void main(String[] args) {
		UUIDSessionIdGenerator uuidSessionIdGenerator = new UUIDSessionIdGenerator();
		String xx = uuidSessionIdGenerator.sessionId(null);
		log.info(xx);

	}

	/**
	 *
	 * @author linjd
	 */
	private UUIDSessionIdGenerator() {
	}

	/**
	 * @return
	 * @author linjd
	 */
	@Override
	public String sessionId(HttpConfig httpConfig) {
		return UUID.randomUUID().toString().replace("-", "");
	}
}
