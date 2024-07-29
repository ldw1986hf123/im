package com.kuailu.im.server.listener;

import com.alibaba.fastjson.JSONObject;
import com.kuailu.im.core.ImChannelContext;
import com.kuailu.im.core.exception.ImException;
import com.kuailu.im.core.packets.UserDto;
import com.kuailu.im.server.listener.AbstractImUserListener;
import lombok.extern.slf4j.Slf4j;

/**
 * @description:
 * @author: 林坚丁
 * @time: 2022/11/30 18:42
 */
@Slf4j
public class ImKuailuUserListener extends AbstractImUserListener {
    @Override
    public void doAfterBind(ImChannelContext imChannelContext, UserDto user) throws ImException {
        log.info("绑定用户:{}", JSONObject.toJSONString(user));
    }

    @Override
    public void doAfterUnbind(ImChannelContext imChannelContext, UserDto user) throws ImException {
        log.info("解绑用户:{}",JSONObject.toJSONString(user));
    }
}
