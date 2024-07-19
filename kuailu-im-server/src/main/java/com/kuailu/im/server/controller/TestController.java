package com.kuailu.im.server.controller;

import com.kuailu.im.core.http.AjaxResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 */
@RestController
@Slf4j
@RequestMapping(value = "/test")
public class TestController {

    @Autowired
    private RedisTemplate redisTemplate;

    @GetMapping("/details")
    public AjaxResult details(@RequestParam(value = "groupId") String groupId) {
        AjaxResult result=AjaxResult.fail();
        Object o = redisTemplate.opsForValue().get(groupId);
        log.trace("log trace level test");
        log.debug("log debug level test");
        log.info("log info level test");
        log.warn("log warn level test");
        log.error("log error level test");
        return result.success(o);
    }


}
