package com.kuailu.im.server.util;

import java.util.UUID;

/**
 */
public class UUIDUtil {
    public static String getUUID() {
        return UUID.randomUUID().toString().replaceAll("\\-", "");
    }
}
