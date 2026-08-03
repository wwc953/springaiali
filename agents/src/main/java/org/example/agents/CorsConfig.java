package org.example.agents;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
//import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
//import org.springframework.web.filter.CorsFilter;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;



@Configuration
public class CorsConfig {
//        @Bean
//    public CorsFilter corsFilter() {
//        CorsConfiguration config = new CorsConfiguration();
//        config.setAllowCredentials(true); // 允许携带凭证[reference:13]
//        config.addAllowedOriginPattern("*"); // 允许所有源（使用 Pattern）[reference:14]
//        config.addAllowedHeader("*"); // 允许所有请求头[reference:15]
//        config.addAllowedMethod("*"); // 允许所有请求方法[reference:16]
//        config.setMaxAge(3600L); // 预检请求缓存时间[reference:17]
//
//        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//        source.registerCorsConfiguration("/**", config); // 对所有路径生效[reference:18]
//        return new CorsFilter(source);
//    }

    //    webflux设置跨域方式
    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.addAllowedOriginPattern("*");
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        config.setAllowCredentials(true);
        // 预检请求的缓存时间
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // 对所有路径应用上述配置
        source.registerCorsConfiguration("/**", config);

        return new CorsWebFilter(source);
    }
}
