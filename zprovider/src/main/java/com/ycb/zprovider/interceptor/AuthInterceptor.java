package com.ycb.zprovider.interceptor;

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
                .addPathPatterns("/borrow/getMachineInfo")
                .addPathPatterns("/formid/submitFormId")
                .addPathPatterns("/order/getOrderList")
                .addPathPatterns("/order/getOrderStatus")
                .addPathPatterns("/wxpay/payment")
                .addPathPatterns("/refund/getRefundList")
                .addPathPatterns("/refund/doRefund")
                .addPathPatterns("/shop/getShopList")
                .addPathPatterns("/shop/getShopInfo")
                .addPathPatterns("/user/userInfo")
                .excludePathPatterns("/user/login");
        // super.addInterceptors(registry);
    }
}
