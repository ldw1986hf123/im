package com.kuailu.im.server.apass.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONUtil;
import com.google.common.collect.Lists;
import com.kuailu.im.core.apass.resp.ApassResult;
import com.kuailu.im.core.apass.resp.LoginVo;
import com.kuailu.im.core.param.ApassLoginParam;
import com.kuailu.im.server.constant.CLIENT_TYPE;
import com.kuailu.im.server.dto.UserCacheDto;
import com.kuailu.im.server.model.entity.Conversation;
import com.kuailu.im.server.model.entity.UserAccount;
import com.kuailu.im.server.service.IConversationService;
import com.kuailu.im.server.service.IUserAccountService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 专门提供给apass服务调用
 */
@RestController
@RequestMapping(value = "/apass/login")
@Slf4j
public class ApasssController extends ApassBaseController {

    @Autowired
    IConversationService conversationService;

    @Autowired
    IUserAccountService userAccountService;

    @PostMapping("/login")
    public ApassResult login(@RequestBody ApassLoginParam apassLoginParam) {
        ApassResult ajaxResult = ApassResult.fail();
        String userId = apassLoginParam.getUserId();
        String seid = apassLoginParam.getSeid();
        LoginVo loginVo = new LoginVo();
        UserCacheDto userCacheDto = null;
        Conversation fileHelperConversation = null;

        if (!validateParam(ajaxResult, apassLoginParam)) {
            return ajaxResult;
        }

        UserAccount userAccount = null;
        try {
            userAccount = userAccountService.getByUserIdLogin(seid, userId);
            userAccount.setClientType(CLIENT_TYPE.WEB);
            userAccount.setSeid(seid);
            userAccountService.updateByLogin(userAccount);
            //是否已经登录过
            userCacheDto = userAccountService.getApassOnlineUser(userAccount.getUserId());
            if (null != userCacheDto) {
                log.info("用户：{} userId:{}  已经登录过",userCacheDto.getUserName(),userId);
                //延长过期时间
            } else {
                userCacheDto = userAccountService.cacheApassUserInLogin(userAccount);
            }
            fileHelperConversation = conversationService.getFileHelperByUserId(userId);
        } catch (Exception e) {
            log.error("apass登录异常.apassLoginParam:{}", JSONUtil.toJsonStr(apassLoginParam), e);
        }
        loginVo = BeanUtil.copyProperties(fileHelperConversation, LoginVo.class);
        loginVo.setToken(userCacheDto.getToken());
        userAccountService.cleanUserGroupCache(Lists.newArrayList(userId));
        conversationService.cleanUserConversationIdCache(Lists.newArrayList(userId));

        ajaxResult.success(loginVo);
        return ajaxResult;
    }

    @PostMapping("/logout")
    public ApassResult logout(@RequestParam(value = "groupId") String groupId) {
        ApassResult result = ApassResult.fail();
        result.success();
        return result;
    }

    private Boolean validateParam(ApassResult ajaxResult, ApassLoginParam apassLoginParam) {
        Boolean validate = false;
        if (StringUtils.isEmpty(apassLoginParam.getUserId())) {
            ajaxResult.fail("userId 不能为空");
        } else if (StringUtils.isEmpty(apassLoginParam.getSeid())) {
            ajaxResult.fail("seid 不能为空");
        } else {
            validate = true;
        }
        return validate;
    }

}
