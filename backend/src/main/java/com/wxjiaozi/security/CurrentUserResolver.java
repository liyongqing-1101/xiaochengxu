package com.wxjiaozi.security;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
public class CurrentUserResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(CurrentUser.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter,
                                  ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest,
                                  WebDataBinderFactory binderFactory) throws Exception {
        HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
        if (request == null) {
            return null;
        }

        CurrentUser annotation = parameter.getParameterAnnotation(CurrentUser.class);
        if (annotation == null) {
            return null;
        }

        Object result = null;

        if ("openid".equals(annotation.value())) {
            result = request.getAttribute("currentOpenid");
        } else if ("adminId".equals(annotation.value())) {
            result = request.getAttribute("currentAdminId");
        } else {
            // Default: return currentUserId as Long
            result = request.getAttribute("currentUserId");
        }

        if (result == null && !annotation.required()) {
            return null;
        }

        return result;
    }
}
