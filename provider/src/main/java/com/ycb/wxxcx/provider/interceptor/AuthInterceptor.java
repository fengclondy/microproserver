package com.ycb.wxxcx.provider.interceptor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/**
 * Created by zhuhui on 17-7-27.
 */
@Configuration
public class AuthInterceptor extends WebMvcConfigurerAdapter {
    @Bean
    AuthHandlerInterceptor authHandlerInterceptor() {
        return new AuthHandlerInterceptor();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authHandlerInterceptor())
                .addPathPatterns("/shop/getShopList")
                .excludePathPatterns("/user/login");
        // super.addInterceptors(registry);
    }
}
