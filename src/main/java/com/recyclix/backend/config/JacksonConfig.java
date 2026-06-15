package com.recyclix.backend.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.TimeZone;

@Configuration
public class JacksonConfig {

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = JsonMapper.builder()
                .addModule(new JavaTimeModule())
                .build();

        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        // Dates ISO (pas timestamps)
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // Tolérance aux champs inconnus
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

        // Timezone cohérente
        mapper.setTimeZone(TimeZone.getTimeZone("Africa/Algiers"));

        return mapper;
    }
}