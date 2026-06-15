package com.recyclix.backend.security;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "spring.app.jwt")
public class SecurityProperties {
    private String secret;
    private long ttlSeconds = 86400; // 1 jour
    private String issuer = "recyclix";
}