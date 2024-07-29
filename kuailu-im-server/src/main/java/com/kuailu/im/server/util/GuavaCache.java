package com.kuailu.im.server.util;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class GuavaCache {
    public Cache<String, Object> cache = CacheBuilder.newBuilder()
            .expireAfterWrite(5, TimeUnit.MINUTES)// 注意和expireAfterAccess的区别
            .maximumSize(100) // 缓存最大容量，超过时会根据缓存策略进行清理
            .build();


    @PostConstruct
    public void init() {
        log.info("GuavaCache 初始化完成{}", cache);
    }

    public Object get(String key) {
        return cache.getIfPresent(key);
    }
    public  <T> T get(String key,Class<T> clazz) {
        return (T) cache.getIfPresent(key);
    }

    public void put(String key, Object value) {
        cache.put(key, value);
    }

    public void invalidate(String key) {
        cache.invalidate(key);
    }

    public void invalidateAll() {
        cache.invalidateAll();
    }
}

