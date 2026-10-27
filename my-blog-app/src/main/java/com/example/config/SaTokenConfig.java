package com.example.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.jwt.StpLogicJwtForSimple;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpLogic;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class SaTokenConfig implements WebMvcConfigurer {

    @Bean
    public StpLogic stpLogicJwt() {
        return new StpLogicJwtForSimple();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new SaInterceptor(handler -> {
            SaRouter.match("/**", r -> {
                String method = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
                        .getRequest().getMethod();
                if ("POST".equalsIgnoreCase(method)
                        || "PUT".equalsIgnoreCase(method)
                        || "DELETE".equalsIgnoreCase(method)) {
                    StpUtil.checkLogin();
                }
            });
        }))
        .addPathPatterns("/**")
        .excludePathPatterns("/auth/login");
    }
}
