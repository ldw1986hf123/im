package com.kuailu.im.server.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kuailu.im.server.mapper.WhiteListMapper;
import com.kuailu.im.server.model.entity.WhiteList;
import com.kuailu.im.server.service.IWhiteListService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 白名单表 服务实现类
 * </p>
 *
 * @author liangdl
 * @since 2023-05-25
 */
@Service
@Slf4j
public class WhiteListServiceImpl extends ServiceImpl<WhiteListMapper, WhiteList> implements IWhiteListService {

}
