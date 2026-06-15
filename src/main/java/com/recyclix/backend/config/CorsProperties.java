package com.recyclix.backend.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@Data
@ConfigurationProperties(prefix = "recyclix.cors")
public class CorsProperties {

    private String pathPattern = "/**";

    private List<String> allowedOrigins = List.of("http://localhost:3000", "http://localhost:5173");


    private List<String> allowedOriginPatterns = List.of();

    private List<String> allowedMethods = List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS");
    private List<String> allowedHeaders = List.of("*");
    private List<String> exposedHeaders = List.of("Authorization", "Content-Disposition");

    private boolean allowCredentials = true;
    private long maxAge = 3600;

    //-------------------
}