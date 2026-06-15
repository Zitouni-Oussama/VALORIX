package com.recyclix.backend.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.*;
import org.springframework.web.filter.CorsFilter;

import java.util.List;

@Configuration
@EnableConfigurationProperties(CorsProperties.class)
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter(CorsProperties props) {
        CorsConfiguration config = new CorsConfiguration();

        // origins
        if (props.getAllowedOrigins() != null && !props.getAllowedOrigins().isEmpty()) {
            config.setAllowedOrigins(props.getAllowedOrigins());
        }

        // origin patterns (utile si tu veux wildcard genre http://localhost:*)
        if (props.getAllowedOriginPatterns() != null && !props.getAllowedOriginPatterns().isEmpty()) {
            config.setAllowedOriginPatterns(props.getAllowedOriginPatterns());
        }

        config.setAllowedMethods(props.getAllowedMethods());
        config.setAllowedHeaders(props.getAllowedHeaders());
        config.setExposedHeaders(props.getExposedHeaders());
        config.setAllowCredentials(props.isAllowCredentials());
        config.setMaxAge(props.getMaxAge());

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration(props.getPathPattern(), config);

        return new CorsFilter(source);
    }
}