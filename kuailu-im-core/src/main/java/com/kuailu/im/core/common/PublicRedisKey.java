package com.kuailu.im.core.common;

/**
 * 放到redis的公共变量，为不同的服务之间取值用
 */
public interface PublicRedisKey {
    String IM_UNREAD_MSG_COUNT = "IM_UNREAD_MSG_COUNT_";   // 加上userId 后使用
    String APAAS_UNREAD_MSG_COUNT = "APAAS_UNREAD_MSG_COUNT_";   //加上userId 后使用
    String APAAS_PAD_UNREAD_MSG_COUNT = "APAAS_PAD_UNREAD_MSG_COUNT_";   //加上userId 后使用
}
