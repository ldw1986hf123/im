package com.kuailu.im.server.mapper;

import com.kuailu.im.core.packets.ChatGroupUserIdDto;
import com.kuailu.im.core.packets.PrivateGroupDto;
import com.kuailu.im.server.model.entity.ChatGroupMember;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 */
public interface ChatGroupMemberMapper extends BaseMapper<ChatGroupMember> {
    List<ChatGroupUserIdDto> getUserChatGroup(@Param("userId") String userId);

    PrivateGroupDto getPrivateGroup(@Param("userId") String userId, @Param("memberId") String memberId);

    void saveGroupMemberBatch(@Param("groupMemberList")   List<ChatGroupMember> groupMemberList);

}
