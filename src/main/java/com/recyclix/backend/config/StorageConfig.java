package com.recyclix.backend.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Path;
import java.util.Set;

@Configuration
@EnableConfigurationProperties(StorageProperties.class)
public class StorageConfig {

    @Bean
    public Path uploadsRoot(StorageProperties props) {
        return Path.of(props.getRootDir()).toAbsolutePath().normalize();
    }
}

@Data
@ConfigurationProperties(prefix = "recyclix.storage")
class StorageProperties {
    
    private String rootDir = "uploads";
    private String imagesDir = "images";
    private Set<String> allowedExtensions = Set.of("png", "jpg", "jpeg", "webp");

    /**
     * Taille max par fichier (en bytes)
     * Ex: 5MB = 5 * 1024 * 1024
     */
    private long maxFileSize = 5L * 1024 * 1024;
}