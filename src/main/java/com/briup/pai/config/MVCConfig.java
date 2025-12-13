package com.briup.pai.config;

import com.briup.pai.interceptor.LoginInterceptor;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author 86151
 * @program: Plant_Ai_Identify_Study
 * @description
 * @create 2025/12/13 17:33
 **/
@Configuration
public class MVCConfig implements WebMvcConfigurer {

    @Resource
    private LoginInterceptor loginInterceptor;

    /**
     * 拦截器配置
     * @param registry
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 需要放行的路径
        String[] excludePaths = {
                "/login/withUsername",
                "/login/withTelephone",
                "/login/sendMessageCode",
                "/doc.html",
                "/swagger-resources/**",
                "/webjars/**",
                "/v3/api-docs/**",
                "/icons/icon_zh_48.png",
                "/favicon.ico"
        };
        registry.addInterceptor(loginInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(excludePaths);
    }
}