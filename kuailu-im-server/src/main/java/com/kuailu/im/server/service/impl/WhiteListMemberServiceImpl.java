package com.kuailu.im.server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kuailu.im.server.enums.YesOrNoEnum;
import com.kuailu.im.server.mapper.WhiteListMemberMapper;
import com.kuailu.im.server.model.entity.WhiteList;
import com.kuailu.im.server.model.entity.WhiteListMember;
import com.kuailu.im.server.service.IWhiteListMemberService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 白名单成员表 服务实现类
 * </p>
 *
 * @author liangdl
 * @since 2023-05-25
 */
@Service
@Slf4j
public class WhiteListMemberServiceImpl extends ServiceImpl<WhiteListMemberMapper, WhiteListMember> implements IWhiteListMemberService {

    @Autowired
    WhiteListServiceImpl whiteListService;

    @Override
    public int inWhiteList(String mUserId, String userId) {
        LambdaQueryWrapper queryWrapperWhiteList = new QueryWrapper<WhiteList>().lambda().eq(WhiteList::getUserId, mUserId);
        if (whiteListService.count(queryWrapperWhiteList) > 0) {
            LambdaQueryWrapper queryWrapperWhiteMemberList = new QueryWrapper<WhiteListMember>().lambda()
                    .eq(WhiteListMember::getUserId, userId)
                    .eq(WhiteListMember::getMUserId, mUserId);
            if (count(queryWrapperWhiteMemberList) < 0) {
                return YesOrNoEnum.NO.getCode();
            }
        }
        return YesOrNoEnum.YES.getCode();
    }
}
