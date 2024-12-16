package com.hmall.common.interceptors;


import cn.hutool.core.util.StrUtil;
import com.hmall.common.utils.Constants;
import com.hmall.common.utils.UserContext;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * preHandle: 取出用户信息,保存至ThreadLocal
 * afterCompletion: 清理ThreadLocal
 * 注意: 本拦截器不需要真正拦截任何请求,只负责取出用户id并将用户信息存入ThreadLocal
 */
public class UserInfoInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 1. 获取登陆用户信息
        String userId = request.getHeader(Constants.USER_INFO_KEY);

        if (!StrUtil.isNotBlank(userId)) {
            return true;
        }
        // 2. 如果有，存入ThreadLocal
        UserContext.setUser(Long.valueOf(userId));

        // 3. 放行
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 清理用户信息
        UserContext.removeUser();
    }
}
