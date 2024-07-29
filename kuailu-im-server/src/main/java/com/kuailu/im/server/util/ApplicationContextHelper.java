package com.kuailu.im.server.util;

import org.springframework.context.ConfigurableApplicationContext;

/**
 * @description:
 */
public class ApplicationContextHelper {
    private static ConfigurableApplicationContext context;

    private ApplicationContextHelper() {
        // Never instance
    }

    public static void init(ConfigurableApplicationContext applicationContext) {
        context = applicationContext;
    }

    public static ConfigurableApplicationContext get() {
        return context;
    }

}
