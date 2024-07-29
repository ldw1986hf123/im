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
import com.kuailu.im.server.service.impl.InitializeData;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 专门提供给apass服务调用
 */
@RestController
@RequestMapping(value = "/apass/fix")
@Slf4j
public class ApasssFixController extends ApassBaseController {

    @Autowired
    InitializeData initializeData;

    @PostMapping("/initConversation")
    public ApassResult initConversation() {
        ApassResult ajaxResult = ApassResult.fail();
        initializeData.initConversation();
        ajaxResult.success();
        return ajaxResult;
    }

    @PostMapping("/initFileHelper")
    public ApassResult initFileHelper() {
        ApassResult ajaxResult = ApassResult.fail();
        initializeData.initFileHelper();
        ajaxResult.success();
        return ajaxResult;
    }

    @PostMapping("/initTipMsg")
    public ApassResult initTipMsg() {
        ApassResult ajaxResult = ApassResult.fail();
        initializeData.initTipMsg();
        ajaxResult.success();
        return ajaxResult;
    }
}
