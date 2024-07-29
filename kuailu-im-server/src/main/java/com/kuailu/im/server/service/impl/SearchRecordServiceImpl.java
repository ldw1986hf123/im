package com.kuailu.im.server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kuailu.im.server.mapper.SearchRecordMapper;
import com.kuailu.im.server.model.entity.ChatUnreadMsg;
import com.kuailu.im.server.model.entity.SearchRecord;
import com.kuailu.im.server.service.ISearchRecordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * 搜索记录表 服务实现类
 * </p>
 *
 * @author liangdl
 * @since 2023-06-14
 */
@Service
@Slf4j
public class SearchRecordServiceImpl extends ServiceImpl<SearchRecordMapper, SearchRecord> implements ISearchRecordService {

    @Override
    public List<String> getRecordByUserId(String userId) {
        List<String> searchKeyList = list(new QueryWrapper<SearchRecord>().select("distinct search_key, created_time").orderByDesc("created_time").lambda().eq(SearchRecord::getUserId, userId).last("limit 10")).stream().map(SearchRecord::getSearchKey).collect(Collectors.toList());
        return searchKeyList;
    }

    @Override
    public void save(String userId, String searchKey) {
        SearchRecord searchRecord = new SearchRecord();
        searchRecord.setUserId(userId);
        searchRecord.setSearchKey(searchKey);
        searchRecord.setCreatedTime(new Date());
        searchRecord.setCreatedBy(userId);
        UpdateWrapper<SearchRecord> updateWrapper = new UpdateWrapper<SearchRecord>()
                .eq("user_id", userId)
                .eq("search_key", searchKey);
        saveOrUpdate(searchRecord, updateWrapper);
    }

    @Override
    public void clear(String userId) {
        remove(new QueryWrapper<SearchRecord>().lambda().eq(SearchRecord::getUserId, userId));
    }

    @Override
    public List<String> getTop10Records(String userId) {
        return baseMapper.getTop10Records(userId);
    }

}
