package org.example.agents;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class CorsConfig {
    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true); // 允许携带凭证[reference:13]
        config.addAllowedOriginPattern("*"); // 允许所有源（使用 Pattern）[reference:14]
        config.addAllowedHeader("*"); // 允许所有请求头[reference:15]
        config.addAllowedMethod("*"); // 允许所有请求方法[reference:16]
        config.setMaxAge(3600L); // 预检请求缓存时间[reference:17]

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config); // 对所有路径生效[reference:18]
        return new CorsFilter(source);
    }
}
