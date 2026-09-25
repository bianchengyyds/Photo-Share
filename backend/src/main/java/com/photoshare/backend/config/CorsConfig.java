package com.photoshare.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


// Spring Boot 会自动调用 addCorsMappings 方法，但有一个前提条件——这个方法所在的类必须满足两个条件：
// 1. 类必须实现 WebMvcConfigurer 接口
// 2. 类必须被 @Configuration 注解标注, 即 类被Spring容器管理

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // 对所有路径（/**）生效，即全部接口都应用下面的跨域规则。
                .allowedOriginPatterns("*") // 允许任意来源域名的跨域请求
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // 允许的请求方法
                .allowedHeaders("*") // 允许的请求头
                .allowCredentials(true) // 是否允许携带凭证（Cookie、Authorization 头等）
                .maxAge(3600); // 最大缓存时间
    }
}
