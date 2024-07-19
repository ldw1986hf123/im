package com.kuailu.im.server.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kuailu.im.server.model.entity.SearchRecord;

import java.util.List;

/**
 * <p>
 */
public interface SearchRecordMapper extends BaseMapper<SearchRecord> {

    List<String> getTop10Records(String userId);

}
