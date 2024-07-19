package com.kuailu.im.server.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.kuailu.im.server.model.entity.WhiteListMember;

/**
 * <p>
 */
public interface IWhiteListMemberService extends IService<WhiteListMember> {

    int inWhiteList(String mUserId,String userId);
}
