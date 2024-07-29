package com.kuailu.im.server.processor.group;


import com.kuailu.im.core.ImChannelContext;
import com.kuailu.im.core.packets.GroupDto;
import com.kuailu.im.core.packets.JoinGroupRespBody;
import com.kuailu.im.server.processor.SingleProtocolCmdProcessor;
/**
 * @author ensheng
 */
public interface GroupCmdProcessor extends SingleProtocolCmdProcessor {
    /**
     * 加入群组处理
     * @param joinGroup
     * @param imChannelContext
     * @return
     */
    JoinGroupRespBody join(GroupDto joinGroup, ImChannelContext imChannelContext);
}
