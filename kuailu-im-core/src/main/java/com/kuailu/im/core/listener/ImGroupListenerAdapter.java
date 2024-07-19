package com.kuailu.im.core.listener;

import com.kuailu.im.core.ImChannelContext;
import com.kuailu.im.core.ImConst;
import com.kuailu.im.core.packets.GroupDto;
import org.tio.core.ChannelContext;
import org.tio.core.intf.GroupListener;

/**
 * @ClassName ImGroupListenerAdapter
 * @Description TODO
 * @author linjd
 * @Date 2020/1/12 14:19
 * @Version 1.0
 **/
public class ImGroupListenerAdapter implements GroupListener, ImConst {

    private ImGroupListener imGroupListener;

    public ImGroupListenerAdapter(ImGroupListener imGroupListener){
        this.imGroupListener = imGroupListener;
    }

    @Override
    public void onAfterBind(ChannelContext channelContext, String group) throws Exception {
        ImChannelContext imChannelContext = (ImChannelContext)channelContext.get(Key.IM_CHANNEL_CONTEXT_KEY);
        GroupDto groupDto=new GroupDto();
        groupDto.setGroupId(group);
        imGroupListener.onAfterBind(imChannelContext, groupDto);
    }

    @Override
    public void onAfterUnbind(ChannelContext channelContext, String group) throws Exception {
        ImChannelContext imChannelContext = (ImChannelContext)channelContext.get(Key.IM_CHANNEL_CONTEXT_KEY);
        GroupDto groupDto=new GroupDto();
        groupDto.setGroupId(group);
        imGroupListener.onAfterUnbind(imChannelContext, groupDto);
    }
}
