package com.kuailu.im.server.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.kuailu.im.server.model.entity.SearchRecord;

import java.util.List;

/**
 * <p>
 * 用户搜索记录表 服务类
 * </p>
 *
 * @author liangdl
 * @since 2023-06-14
 */
public interface ISearchRecordService extends IService<SearchRecord> {

    List<String> getRecordByUserId(String userId);

    void save(String userId, String searchKey);

    void clear(String userId);

    List<String> getTop10Records(String userId);

}
