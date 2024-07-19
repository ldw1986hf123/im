package com.kuailu.im.server.config;

import com.kuailu.im.server.annotation.ApiVersion;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.web.servlet.mvc.condition.RequestCondition;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.lang.reflect.Method;

/**
*/
public class ApiHandlerMapping extends RequestMappingHandlerMapping {


    // 对类修饰的注解
    @Override
    protected RequestCondition<?> getCustomTypeCondition(Class<?> handlerType) {
        // 判断是否有@ApiVersion注解
        ApiVersion apiVersion = AnnotationUtils.findAnnotation(handlerType, ApiVersion.class);
        return createCondition(apiVersion);
    }

    // 对方法修饰的注解
    @Override
    protected RequestCondition<?> getCustomMethodCondition(Method method) {
        ApiVersion apiVersion = AnnotationUtils.findAnnotation(method, ApiVersion.class);
        return createCondition(apiVersion);
    }

    // 创建基于@APIVersion的RequestCondition
    private RequestCondition<ApiVersionCondition> createCondition(ApiVersion apiVersion) {
        return apiVersion == null ? null : new ApiVersionCondition(apiVersion.value());
    }

}