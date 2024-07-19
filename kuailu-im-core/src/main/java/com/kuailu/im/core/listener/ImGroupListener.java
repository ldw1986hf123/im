package com.kuailu.im.core.listener;

import com.kuailu.im.core.ImChannelContext;
import com.kuailu.im.core.exception.ImException;
import com.kuailu.im.core.packets.GroupDto;

/**
 * @ClassName ImGroupListener
 * @Description TODO
 * @author linjd
 * @Date 2020/1/12 14:17
 * @Version 1.0
 **/
public interface ImGroupListener {
    /**
     * 绑定群组后回调该方法
     * @param imChannelContext IM通道上下文
     * @param group 绑定群组对象
     * @throws ImException
     * @author linjd
     */
    void onAfterBind(ImChannelContext imChannelContext, GroupDto group) throws ImException;

    /**
     * 解绑群组后回调该方法
     * @param imChannelContext IM通道上下文
     * @param group 绑定群组对象
     * @throws ImException
     * @author linjd
     */
    void onAfterUnbind(ImChannelContext imChannelContext, GroupDto group) throws ImException;

}
