package com.hmall.api.config;

import com.hmall.common.utils.Constants;
import com.hmall.common.utils.UserContext;
import feign.Logger;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Bean;

/**
 * 要使配置生效，要修改所有SpringBootApplication启动类的注解的
 * defaultConfiguration字段
 * 这里就将日志级别配置和请求拦截器配置都写在一起了，就不用修改启动累注解代码了
 */
public class DefaultFeignConfig {
    @Bean
    public Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL;
    }

    @Bean
    public RequestInterceptor userInfoRequestInterceptor(){
        return new RequestInterceptor() {
            @Override
            public void apply(RequestTemplate template) {
                // 获取登录用户
                Long userId = UserContext.getUser();

                if(userId == null) {
                    // 如果为空则直接跳过
                    return;
                }

                // 如果不为空则放入请求头中，传递给下游微服务
                template.header(Constants.USER_INFO_KEY, userId.toString());
            }
        };
    }
}
