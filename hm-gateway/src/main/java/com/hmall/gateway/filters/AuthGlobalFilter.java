package com.hmall.gateway.filters;

import com.hmall.common.exception.UnauthorizedException;
import com.hmall.common.utils.Constants;
import com.hmall.gateway.config.AuthProperties;
import com.hmall.gateway.config.JwtProperties;
import com.hmall.gateway.utils.JwtTool;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor // 通过这个注解来自动实现构造函数实现AuthProperties的注入
@EnableConfigurationProperties(AuthProperties.class)
public class AuthGlobalFilter implements GlobalFilter, Ordered {

    private final AuthProperties authProperties;

    private final JwtTool jwtTool;

    private final AntPathMatcher antPathMatcher = new AntPathMatcher();

    private final StringRedisTemplate redisTemplate;

    private final JwtProperties jwtProperties;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String requestPath = request.getPath().toString();
        List<String> excludePaths = authProperties.getExcludePaths();
        if (isExclude(excludePaths, requestPath)) {
            return chain.filter(exchange);
        }

        String token = null;
        List<String> headers = request.getHeaders().get("Authorization");
        if (headers != null && !headers.isEmpty()) {
            token = headers.get(0);
        }

        System.out.println("get token = " + token);

        jwtTool.validateTokenBeforeRedis(token);

        String redisKey = jwtTool.getRedisKey(token);
        String userId = redisTemplate.opsForValue().get(redisKey);

        if (userId == null) {
            throw new UnauthorizedException("无效的token");
        }

        redisTemplate.expire(redisKey, jwtProperties.getTokenTTL().toMillis(), TimeUnit.MILLISECONDS);

        ServerWebExchange newExchange = exchange.mutate()
                .request(builder -> builder.header(Constants.USER_INFO_KEY, userId))
                .build();

        return chain.filter(newExchange);
    }

//    @Override
    public Mono<Void> filter_jwt(ServerWebExchange exchange, GatewayFilterChain chain) {
        // 1. 通过request获取用户信息
        ServerHttpRequest request = exchange.getRequest();

        // 2. 不需要拦截就放行 (excludePath)
        String requestPath = request.getPath().toString();

        // 获取放行路径
        List<String> excludePaths = authProperties.getExcludePaths();
        if (isExclude(excludePaths, requestPath)) {
            // 放行
            return chain.filter(exchange);
        }

        // 3. 获取token
        String token = null;
        System.out.println("接收到请求" + request.getHeaders());
        List<String> headers = request.getHeaders().get("Authorization");

        if (headers != null && !headers.isEmpty()) {
            token = headers.get(0);
        }

        System.out.println("获取到用户的jwtToken:" + token);

        // 4. 校验并解析token (校验失败会抛出异常)
        Long userId = null;
        try {
            userId = jwtTool.parseToken(token);
        } catch (UnauthorizedException e) {
            // 拦截，设置响应状态码为401
            ServerHttpResponse response = exchange.getResponse();
            response.setStatusCode(HttpStatus.UNAUTHORIZED);

            // 直接拦截,不继续执行过滤器链
            return response.setComplete();
        }

        System.out.println("解析出用户id:" + userId);

        // 5. 保存用户信息到header里
        String userInfo = userId.toString();
        ServerWebExchange newExchange = exchange.mutate()
                .request(builder -> builder.header(Constants.USER_INFO_KEY, userInfo))
                .build();

        // 6. 放行
        return chain.filter(newExchange);
    }

    /**
     * 判断请求路径是否在被排除路径中
     * @param excludePaths 排除路径列表
     * @param requestPath  请求路径
     * @return 如果在,则不对该请求进行jwt拦截,直接放行
     */
    private boolean isExclude(List<String> excludePaths, String requestPath) {
        for (String excludePath : excludePaths) {
            if (antPathMatcher.match(excludePath, requestPath)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
