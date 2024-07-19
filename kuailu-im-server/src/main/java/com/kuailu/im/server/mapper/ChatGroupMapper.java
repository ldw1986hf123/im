package com.kuailu.im.server.mapper;

import com.kuailu.im.server.model.entity.ChatGroup;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;


public interface ChatGroupMapper extends BaseMapper<ChatGroup> {

    List<Map<String, Object>> getGroupByName(@Param("userId") String userId, @Param("searchKey") String searchKey);

    List<Map<String, Object>> getGroupLikeName(@Param("userId") String userId, @Param("searchKey") String searchKey);

    List<Map<String, Object>> getGroupInclUserName(@Param("userId") String userId, @Param("searchKey") String searchKey);

}
