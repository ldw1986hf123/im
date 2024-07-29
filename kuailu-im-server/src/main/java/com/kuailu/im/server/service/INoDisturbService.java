package com.kuailu.im.server.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.kuailu.im.server.model.entity.NoDisturb;

import java.util.List;

/**
 * <p>
 */
public interface INoDisturbService extends IService<NoDisturb> {

    NoDisturb getOne(String userId, String conversationId);


    List<String> getNoDisturbUserId(String conversationId);

}
