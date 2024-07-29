package com.kuailu.im.server.listener;

import com.kuailu.im.core.ImChannelContext;
import com.kuailu.im.core.exception.ImException;
import com.kuailu.im.core.listener.ImGroupListener;
import com.kuailu.im.core.message.MessageHelper;
import com.kuailu.im.core.packets.GroupDto;
import com.kuailu.im.server.config.ImServerConfig;


import java.util.Objects;

/**
 * @author linjd
 * @Desc
 * @date 2022-08-03 00:17
 */
public abstract class AbstractImGroupListener implements ImGroupListener {

    public abstract void doAfterBind(ImChannelContext imChannelContext, GroupDto group) throws ImException;

    public abstract void doAfterUnbind(ImChannelContext imChannelContext, GroupDto group) throws ImException;

    @Override
    public void onAfterBind(ImChannelContext imChannelContext, GroupDto group) throws ImException {
        ImServerConfig imServerConfig =  (ImServerConfig)imChannelContext.getImConfig();
        MessageHelper messageHelper = imServerConfig.getMessageHelper();
        //是否开启持久化
       /* if(isStore(imServerConfig)){
            messageHelper.getBindListener().onAfterGroupBind(imChannelContext, group);
        }*/
        doAfterBind(imChannelContext, group);
    }

    @Override
    public void onAfterUnbind(ImChannelContext imChannelContext, GroupDto group) throws ImException {
        ImServerConfig imServerConfig =  (ImServerConfig)imChannelContext.getImConfig();
        MessageHelper messageHelper = imServerConfig.getMessageHelper();
        //是否开启持久化
        if(isStore(imServerConfig)){
            messageHelper.getBindListener().onAfterGroupUnbind(imChannelContext, group);
        }
        doAfterUnbind(imChannelContext, group);
    }

    /**
     * 是否开启持久化;
     * @return
     */
    public boolean isStore(ImServerConfig imServerConfig){
        MessageHelper messageHelper = imServerConfig.getMessageHelper();
        if(imServerConfig.ON.equals(imServerConfig.getIsStore()) && Objects.nonNull(messageHelper)){
            return true;
        }
        return false;
    }

}
