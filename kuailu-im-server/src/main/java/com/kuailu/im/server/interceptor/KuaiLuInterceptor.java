package com.kuailu.im.server.interceptor;

import com.alibaba.fastjson.JSON;
import com.kuailu.im.core.apass.resp.ApassResult;
import com.kuailu.im.core.apass.resp.status.ApassCode;
import com.kuailu.im.core.exception.PassException;
import com.kuailu.im.server.dto.UserCacheDto;
import com.kuailu.im.server.service.IUserAccountService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;

@Slf4j
@Component
public class KuaiLuInterceptor implements HandlerInterceptor {
    @Autowired
    IUserAccountService userAccountService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String url = request.getRequestURI();
        if (url.contains("apass")) {
            printParameter(request);

            //目前指校验apass接口
            if (!url.contains("login")) {
                //请求的是非登录接口
                String token = request.getHeader("token");
                UserCacheDto userCacheDto = userAccountService.getByToken(token);
                if (null == userCacheDto) {
                    response.setContentType("text/json;charset=utf-8");

                    //获取当前用户信息(登录的时候储存了)
//                    HttpSession session = request.getSession();

                    //将当前用户信息转化为json传回给前端（用的JSON的转化方法）
                    ApassResult apassResult = ApassResult.fail();
                    apassResult.fail(ApassCode.NEED_LOGIN);
                    String s = JSON.toJSONString(apassResult);
                    PrintWriter writer = null;
                    try {
                        writer = response.getWriter();
                        writer.write(s);
                    } catch (IOException e) {
                        log.error("返回json异常", e);
                    }
                    return false;
                }
            }
        }
        return true;
    }

    private void printParameter(HttpServletRequest request) {
        String url = request.getRequestURI();
        String parameter = "";
        //获得表单中所有的文本域的name
        Enumeration<String> parameteNames = request.getParameterNames();
        while (parameteNames.hasMoreElements()) {
            //获得每个文本域的name
            String parameteName = parameteNames.nextElement();
            //根据文本域的name来获取值
            //因为无法判断文本域是否是单值或者双值，所以我们全部使用双值接收
            String[] parameteValues = request.getParameterValues(parameteName);
            for (String parameteValue : parameteValues) {
                parameter = parameter + (parameteName + ":" + parameteValue + " ");
            }
        }
        log.info("url:{},apass接口参数 {} ", url, parameter);
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
    }

    /**
     * 判断本次请求的数据类型是否为json
     *
     * @param request request
     * @return boolean
     */
    private boolean isJson(HttpServletRequest request) {
        if (request.getContentType() != null) {
            return request.getContentType().equals(MediaType.APPLICATION_JSON_VALUE) ||
                    request.getContentType().equals(MediaType.APPLICATION_JSON_UTF8_VALUE);
        }

        return false;
    }

    private static String getPostData(HttpServletRequest request) {
        StringBuffer data = new StringBuffer();
        String line = null;
        BufferedReader reader = null;
        try {
            reader = request.getReader();
            while (null != (line = reader.readLine()))
                data.append(line);
        } catch (IOException e) {
        } finally {
        }
        return data.toString();
    }
}