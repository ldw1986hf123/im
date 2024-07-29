package com.kuailu.im.server.apass.controller;

import cn.hutool.http.server.HttpServerRequest;
import com.kuailu.im.server.dto.UserCacheDto;
import com.kuailu.im.server.service.IUserAccountService;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Enumeration;

@Controller
public class ApassBaseController {

    @Autowired
    IUserAccountService userAccountService;

    protected UserCacheDto getCurrentUser(HttpServletRequest request) {
        String token = request.getHeader("token");
        if (StringUtils.isEmpty(token)){

        }
        return userAccountService.getByToken(token);
    }


}
