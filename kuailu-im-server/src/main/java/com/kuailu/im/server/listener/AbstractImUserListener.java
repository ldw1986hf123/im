package com.kuailu.im.server.listener;

import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONObject;
import com.kuailu.im.core.ImChannelContext;
import com.kuailu.im.core.exception.ImException;
import com.kuailu.im.core.listener.ImUserListener;
import com.kuailu.im.core.message.MessageHelper;
import com.kuailu.im.core.packets.UserDto;
import com.kuailu.im.server.config.ImServerConfig;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

/**
 * @author 林坚丁
 * @Desc 绑定/解绑 用户监听器抽象类
 * @date 2022-05-02 13:43
 */
@Slf4j
public abstract class AbstractImUserListener implements ImUserListener {

    public abstract void doAfterBind(ImChannelContext imChannelContext, UserDto user) throws ImException;

    public abstract void doAfterUnbind(ImChannelContext imChannelContext, UserDto user) throws ImException;

    @Override
    public void onAfterBind(ImChannelContext imChannelContext, UserDto user) throws ImException {
        ImServerConfig imServerConfig = (ImServerConfig) imChannelContext.getImConfig();
        MessageHelper messageHelper = imServerConfig.getMessageHelper();
        //是否开启持久化
       /* if (isStore(imServerConfig)) {
            messageHelper.getBindListener().onAfterUserBind(imChannelContext, user);
        }*/
        log.info("用户:{}，id:{}，绑定", user.getUserName(), user.getUserId());
//        doAfterBind(imChannelContext, user);
    }

    @Override
    public void onAfterUnbind(ImChannelContext imChannelContext, UserDto user) throws ImException {
        ImServerConfig imServerConfig = (ImServerConfig) imChannelContext.getImConfig();
        MessageHelper messageHelper = imServerConfig.getMessageHelper();
        //是否开启持久化
        if (isStore(imServerConfig)) {
            messageHelper.getBindListener().onAfterUserUnbind(imChannelContext, user);
        }
        log.info("用户:{}，id:{}，解绑", user.getUserName(), user.getUserId());
//        doAfterUnbind(imChannelContext, user);
    }

    /**
     * 是否开启持久化;
     *
     * @return
     */
    public boolean isStore(ImServerConfig imServerConfig) {
        MessageHelper messageHelper = imServerConfig.getMessageHelper();
        if (imServerConfig.ON.equals(imServerConfig.getIsStore()) && Objects.nonNull(messageHelper)) {
            return true;
        }
        return false;
    }

}
