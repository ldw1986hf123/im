package com.kuailu.im.server.config;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.servlet.mvc.condition.RequestCondition;

import javax.servlet.http.HttpServletRequest;

/**
 *
 */
@Data
public class ApiVersionCondition implements RequestCondition<ApiVersionCondition> {
    // header中的key
    private final static String HEADER_VERSION = "apiversion";
    private final static String HEADER_PLATFORM = "apiplatform";

    // api的版本
    private String defaultVersion = "1.3.0";

    // api的平台
    private String apiplatform;

    public ApiVersionCondition(String apiVersion) {
        this.defaultVersion = apiVersion;
    }

    // 将不同的筛选条件合并
    @Override
    public ApiVersionCondition combine(ApiVersionCondition apiVersionCondition) {
        // 采用最后定义优先原则，则方法上的定义覆盖类上面的定义
        return new ApiVersionCondition(apiVersionCondition.getDefaultVersion());
    }

    // 根据request查找匹配到的筛选条件
    @Override
    public ApiVersionCondition getMatchingCondition(HttpServletRequest request) {
        /**
         * 正则匹配请求header参数中是否有版本号
         * 版本号应该以v开头，如：v1、v2，这个可以通过修改正则自定义
         */
        String apiversion = request.getHeader(HEADER_VERSION);
        if (StringUtils.isEmpty(apiversion)) {
            return new ApiVersionCondition(defaultVersion);
        }
        return this;
    }

    // 不同筛选条件比较，用于排序
    @Override
    public int compareTo(ApiVersionCondition apiVersionCondition, HttpServletRequest httpServletRequest) {
        return 0;
    }

}