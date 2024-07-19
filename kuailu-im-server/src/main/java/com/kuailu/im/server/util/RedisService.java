package com.kuailu.im.server.util;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.checkerframework.checker.units.qual.K;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.DefaultTypedTuple;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.concurrent.TimeUnit;
//import cn.hutool.json.JSONUtil;

/**
 * Redis工具类，使用之前请确保RedisTemplate成功注入
 *
 * @author ludw
 * @version 2019-12-06 09:05:38
 */

@Slf4j
@Component
public class RedisService {

    @Autowired
    RedisTemplate redisTemplate;

    /**
     * 设置有效时间
     *
     * @param key     Redis键
     * @param timeout 超时时间
     * @return true=设置成功；false=设置失败
     */
    public boolean expire(final String key, final long timeout) {
        return expire(key, timeout, TimeUnit.SECONDS);
    }

    /**
     * 设置有效时间
     *
     * @param key     Redis键
     * @param timeout 超时时间
     * @param unit    时间单位
     * @return true=设置成功；false=设置失败
     */
    public boolean expire(final String key, final long timeout, final TimeUnit unit) {

        Boolean ret = false;
        try {
            ret = redisTemplate.expire(key, timeout, unit);
        } catch (Exception e) {
            log.error("redis expire 异常", e);
        }
        return ret;
    }

    public boolean hasKey(final String key) {
        Boolean ret = false;
        try {
            ret = redisTemplate.hasKey(key);
        } catch (Exception e) {
            log.error("redis hasKey 异常", e);
        }

        return ret;
    }

    /**
     * 删除单个key
     *
     * @param key 键
     * @return true=删除成功；false=删除失败
     */
    public boolean delKey(final String key) {
        Boolean ret = redisTemplate.delete(key);
        return ret != null && ret;
    }

    /**
     * 删除多个key
     *
     * @param keys 键集合
     * @return 成功删除的个数
     */
    public long delKeys(final Collection<String> keys) {
        Long ret = redisTemplate.delete(keys);
        return ret == null ? 0 : ret;
    }

    /**
     * 存入普通对象
     *
     * @param key   Redis键
     * @param value 值
     */
   /* public void setValue(final String key, final Object value) {
        String json = JSON.toJSONString(value);
        redisTemplate.opsForValue().set(key, json, 1, TimeUnit.MINUTES);
    }*/


    /**
     * 存入普通对象
     *
     * @param key Redis键
     */
    public void setList(final String key, final List list) {
        try {
            if (CollectionUtils.isEmpty(list)) {
                return;
            }
            redisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(list), 1, TimeUnit.HOURS);
        } catch (Exception e) {
            log.error("setList 异常", e);
        }
    }


    public <T> List getList(final String key, Class<T> clazz) {
        List list = new ArrayList();
        try {

            String value = (String) redisTemplate.opsForValue().get(key);
            list = JSONUtil.toList(JSONUtil.toJsonStr(value), clazz);

        } catch (Exception e) {
            log.error("getList 异常", e);
        }
        return list;
    }


    /**
     * 存入普通对象
     *
     * @param key     键
     * @param value   值
     * @param timeout 有效期，单位秒
     */
    public void setValue(final String key, final Object value, final long timeout) {
        redisTemplate.opsForValue().set(key, JSON.toJSONString(value), timeout, TimeUnit.SECONDS);
    }

    /**
     * 设置不过期的值，只有在设置与其他服务共享的 redis值的时候用，其他情况不用
     *
     * @param key
     * @param value
     */
    public void setValue(final String key, final Object value) {
        redisTemplate.opsForValue().set(key, value);
    }


    public void setValue(final String key, final Object value, final long timeout, TimeUnit timeUnit) {
        try {
            redisTemplate.opsForValue().set(key, JSON.toJSONString(value), timeout, timeUnit);
        } catch (Exception e) {
            log.error("redis setValue 异常", e);
        }
    }

    public <T> List<T> reverseRange(String key, int start, int end, Class<T> clazz) {
        List<T> list = Arrays.asList();
        try {
            Set<String> ranking = redisTemplate.opsForZSet().reverseRange(key, start, end);
            list = JSON.parseArray(JSON.toJSONString(ranking), clazz);
        } catch (Exception e) {
            log.error("redis reverseRange 异常", e);
        }
        return list;
    }

    public <T> List<T> reverseRange(String key, Class<T> clazz) {
        List<T> list = Arrays.asList();
        try {
            Set<String> ranking = redisTemplate.opsForZSet().reverseRange(key, 0, -1);
            list = JSON.parseArray(JSON.toJSONString(ranking), clazz);
        } catch (Exception e) {
            log.error("redis reverseRange 异常", e);
        }
        return list;
    }

    public <T> List<T> range(String key, Class<T> clazz) {
        List<T> list = Arrays.asList();
        try {
            Set<String> ranking = redisTemplate.opsForZSet().range(key, 0, -1);
            list = JSON.parseArray(JSON.toJSONString(ranking), clazz);
        } catch (Exception e) {
            log.error("redis reverseRange 异常", e);
        }
        return list;
    }

    public <T> List<T> range(String key, int start, int end, Class<T> clazz) {
        List<T> list = Arrays.asList();
        try {
            Set<String> ranking = redisTemplate.opsForZSet().range(key, start, end);
            list = JSONUtil.toList(JSONUtil.toJsonStr(ranking), clazz);
        } catch (Exception e) {
            log.error("redis range 异常", e);
        }
        return list;
    }

    public <T> T getValue(final String key, Class<T> clazz) {
        T jsonValue = null;
        try {
            Object object = redisTemplate.opsForValue().get(key);
            if (null != object) {
                jsonValue = JSON.parseObject(object.toString(), clazz);
            }
        } catch (Exception e) {
            log.error("redis getValue 异常", e);
        }
        return jsonValue;
    }

    public void addZSet(String key, Object value, Double score, int duration) {
        if (null == value) {
            log.error("空对象，不可以放到缓存");
            return;
        }
        try {
            redisTemplate.opsForZSet().add(key, value, score);
        } catch (Exception e) {
            log.error("redis addZset 异常", e);
        }
        redisTemplate.expire(key, duration, TimeUnit.HOURS);
    }

    //todo 放到缓存里的createdTime会不对，待处理
    public void addZSet(String key, Object value, Double score, int duration, TimeUnit timeUnit) {
        if (null == value) {
            log.error("空对象，不可以放到缓存");
            return;
        }
        try {
            redisTemplate.opsForZSet().add(key, value, score);
        } catch (Exception e) {
            log.error("redis addZset 异常", e);
        }
        redisTemplate.expire(key, duration, timeUnit);
    }

    public void addZSet(String key, List list) {
        if (CollectionUtils.isEmpty(list)) {
            log.error("空对象，不可以放到缓存");
            return;
        }
        try {
            for (Object item : list) {
                redisTemplate.opsForZSet().add(key, item, 0);
            }
            redisTemplate.expire(key, 1, TimeUnit.HOURS);
        } catch (Exception e) {
            log.error("redis addZset 异常", e);
        }
    }

    public void addZset(String key, Object value, Double score, Long timeout, TimeUnit timeUnit) {
        if (null == value) {
            return;
        }
        redisTemplate.opsForZSet().add(key, JSON.toJSON(value), score);
        redisTemplate.expire(key, timeout, timeUnit);
    }


    public Long removeZSets(String key, List list) {
        Long removedSize = 0l;
        if (CollectionUtils.isEmpty(list)) {
            return removedSize;
        }
        try {
            removedSize = redisTemplate.opsForZSet().remove(key, list.toArray());
        } catch (Exception e) {
            log.error("redis removeZsets 异常", e);
        }
        return removedSize;
    }


    public long getZSetSize(String key) {
        return redisTemplate.opsForZSet().size(key);//等同于zCard(key);
    }


    public void putHash(String key, String hashKey, Object value) {
        redisTemplate.opsForHash().put(key, hashKey, value);
        redisTemplate.expire(key, 1, TimeUnit.HOURS);

    }

    public void putHash(String key, String hashKey, Object value, int duration, TimeUnit timeUnit) {
        redisTemplate.opsForHash().put(key, hashKey, value);
        redisTemplate.expire(key, duration, timeUnit);

    }

    public void putHashAll(String key, Map map) {
        redisTemplate.opsForHash().putAll(key, map);
        redisTemplate.expire(key, 1, TimeUnit.HOURS);
    }

    public void putHashAll(String key, Map map, int duration, TimeUnit timeUnit) {
        redisTemplate.opsForHash().putAll(key, map);
        redisTemplate.expire(key, duration, timeUnit);
    }

    public <T> T getHashValue(String key, Class<T> clazz) {
        T jsonValue = null;
        if (hasKey(key)) {
            Map<Object, Object> allFieldsAndValues = redisTemplate.opsForHash().entries(key);
            jsonValue = BeanUtil.toBean(allFieldsAndValues, clazz);
        }
        return jsonValue;
    }

    public <T> T getHashValue(String key, String hashKey, Class<T> clazz) {
        T jsonValue = null;
        if (null != redisTemplate.opsForHash().get(key, hashKey)) {
            Object hashValue = redisTemplate.opsForHash().get(key, hashKey);
            if (null != hashValue) {
                jsonValue = (T) hashValue;
            }
        }
        return jsonValue;
    }

    public Boolean hasHashKey(String key, String hashKey) {
        Boolean hasHashKey = redisTemplate.opsForHash().hasKey(key, hashKey);
        return hasHashKey;
    }

    public void deleteHashKey(String key, String hashKey) {
        try {
            redisTemplate.opsForHash().delete(key, hashKey);
        } catch (Exception e) {
            log.error("deleteHashKey 异常", e);
        }
    }


    public void lPush(String key, Object value) {
        redisTemplate.opsForList().leftPush(key, value);
        redisTemplate.expire(key, 1, TimeUnit.MINUTES);
    }

    public <T> T rightPop(String key, Class<T> clazz) {
        T result = null;
        try {
            Object object = redisTemplate.opsForList().rightPop(key);
            if (null != object) {
                result = (T) object;
            }
        } catch (Exception e) {
            log.error("redis rightPop 异常", e);
        }
        return result;
    }

    public void increment(String key) {
        try {
            redisTemplate.opsForValue().increment(key);
        } catch (Exception e) {
            log.error("redis increment 异常", e);
        }
    }

    public void decrement(String key) {
        try {
            redisTemplate.opsForValue().decrement(key);
        } catch (Exception e) {
            log.error("redis increment 异常", e);
        }
    }

    public void convertAndSend(String channel, Object object) {
        try {
            redisTemplate.convertAndSend(channel, JSON.toJSONString(object));
        } catch (Exception e) {
            log.error("redis convertAndSend 异常", e);
        }
    }


}