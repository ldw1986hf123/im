package com.kuailu.im.server.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.logging.Log;
/**
 * @Description MybatisPlusOutImpl，直接使用控制台输出日志
 * @Author ldw
 * MybatisPlusOutImpl 配置在nacos里面，sql输出都转移到这个类里面输出，
 * 然后通过这个url： http://localhost:6689/actuator/loggers/com.kuailu.im.server.config.MybatisPlusOutImpl
 * 传参
 *{
 *     "configuredLevel": "INFO"
 * }
 *
 * 动态开关日志输出级别，以便实现动态输出sql语句
 * 但是用了acurator 需要考虑安全问题
 *
 **/
@Slf4j
public class MybatisPlusOutImpl implements Log {
    public MybatisPlusOutImpl(String clazz) {
        log.info(clazz);
    }

    public boolean isDebugEnabled() {
        return true;
    }

    public boolean isTraceEnabled() {
        return true;
    }

    public void error(String s, Throwable e) {
        log.info(s);
        e.printStackTrace(System.err);
    }

    public void error(String s) {
        log.info(s);
    }

    public void debug(String s) {
        log.info(s);
    }

    public void trace(String s) {
        log.info(s);
    }

    public void warn(String s) {
        log.info(s);
    }
}