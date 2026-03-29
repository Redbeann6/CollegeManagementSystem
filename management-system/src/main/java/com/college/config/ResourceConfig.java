package com.college.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

@Configuration
public class ResourceConfig implements WebMvcConfigurer {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 使用与ProfileController一致的路径构造方式
        String uploadDir = System.getProperty("user.dir") + "/uploads/avatars/";
        registry.addResourceHandler("/static/avatar/**")
                .addResourceLocations("file:" + uploadDir);
        // 添加专门处理头像的资源处理器，这样前端可以直接访问/avatar路径
        registry.addResourceHandler("/avatar/**")
                .addResourceLocations("file:" + uploadDir);
        // 添加/api/avatar/**路径的资源处理器，适配前端axios的baseURL
        registry.addResourceHandler("/api/avatar/**")
                .addResourceLocations("file:" + uploadDir);
    }
}