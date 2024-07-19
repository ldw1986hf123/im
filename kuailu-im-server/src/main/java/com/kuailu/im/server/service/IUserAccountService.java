package com.kuailu.im.server.service;

import com.kuailu.im.core.ImChannelContext;
import com.kuailu.im.core.exception.ImException;
import com.kuailu.im.server.dto.UserCacheDto;
import com.kuailu.im.server.model.entity.ChatGroupMember;
import com.kuailu.im.server.model.entity.UserAccount;
import com.baomidou.mybatisplus.extension.service.IService;
import com.kuailu.im.server.vo.ChatGroupMemberVo;

import java.util.Date;
import java.util.List;

/**
 * <p>
 */
public interface IUserAccountService extends IService<UserAccount> {

    List<ChatGroupMember> getUserInfoByDeptIds(String seid, List<String> deptIds);

    /**
     * 获取用户信息，只在登录的时候调用该方法
     *
     * @param seid
     * @param userId
     * @return
     * @throws Exception
     */
    UserAccount getByUserIdLogin(String seid, String userId) throws Exception;

    Boolean isOnLine(String userId);


    UserCacheDto cacheUserInLogin(UserAccount userAccount);


    /**
     * 返回token
     *
     * @param userAccount
     * @return
     */
    UserCacheDto cacheApassUserInLogin(UserAccount userAccount);

    UserCacheDto getByToken(String token);

    void cleanUserGroupCache(List<String> userIds);

    void initMsgHelper(String receiver);

    /**
     * 获取web端登录用户信息
     *
     * @param userId
     * @return
     */
    UserCacheDto getApassOnlineUser(String userId);

    void logout(String currentUserId);

    //    void updateByLogin(String userId,String userNo,String userName,Integer staffStatus,String avatarUrl,String seid);
    String getAvatarUrl(String userId);

    String getAvatarUrl(String userId, Integer chatType);

    String getAvatarUrl(String userId, String seid);


    String getAvatarUrl(String userId, Integer chatType, String seid);

    void updateByLogin(UserAccount loginUserAccount);

    UserAccount getByUserId(String userId);
}
