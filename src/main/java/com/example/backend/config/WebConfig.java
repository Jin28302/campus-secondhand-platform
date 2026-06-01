package com.example.backend.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

// Web配置 - 注册拦截器、配置文件上传的静态资源映射
@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final AuthInterceptor authInterceptor;

    @Value("${file.upload-path}")
    private String uploadPath;

    // 注册认证拦截器 - 拦截所有请求，排除验证码、注册、登录、上传和错误页面
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/captcha",      // 验证码接口无需认证
                        "/register",     // 注册接口无需认证
                        "/login",        // 登录接口无需认证
                        "/upload/**",    // 上传接口无需认证
                        "/error"         // 错误页面无需认证
                );
    }

    // 配置静态资源映射 - 将/upload/**路径映射到本地文件上传目录
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/upload/**")
                .addResourceLocations("file:" + uploadPath);
    }
}
