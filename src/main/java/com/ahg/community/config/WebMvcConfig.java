package com.ahg.community.config;


import com.ahg.community.controller.interceptor.AlphaInterceptor;
import com.ahg.community.controller.interceptor.LoginRequiredInterceptor;
import com.ahg.community.controller.interceptor.LoginTicketInterceptor;
import com.ahg.community.controller.interceptor.MessageInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {//实现拦截器的配置类需要实现一个接口，而不止是装载Bean

    @Autowired
    private AlphaInterceptor alphaInterceptor;

    @Autowired
    private LoginTicketInterceptor loginTicketInterceptor;

    @Autowired
    private MessageInterceptor messageInterceptor;

    @Autowired
    private LoginRequiredInterceptor loginRequiredInterceptor;
    //
    public void addInterceptors(InterceptorRegistry registry){//registry中添加拦截器（拦截所有请求）
        registry.addInterceptor(alphaInterceptor)
                .excludePathPatterns("/**/*.css", "/**/*.js", "/**/*.png","/**/*.jpg","/**/*.jpeg")
                .addPathPatterns("/register", "/login");


        registry.addInterceptor(loginTicketInterceptor)
                .excludePathPatterns("/**/*.css", "/**/*.js", "/**/*.png","/**/*.jpg","/**/*.jpeg");

        registry.addInterceptor(loginRequiredInterceptor)
                .excludePathPatterns("/**/*.css", "/**/*.js", "/**/*.png","/**/*.jpg","/**/*.jpeg");

        registry.addInterceptor(messageInterceptor)
                .excludePathPatterns("/**/*.css", "/**/*.js", "/**/*.png","/**/*.jpg","/**/*.jpeg");


    }


}
