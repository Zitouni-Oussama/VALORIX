package com.recyclix.backend.security;

import com.recyclix.backend.exception.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthenticationEntryPointImpl implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest req, HttpServletResponse res, AuthenticationException ex) {
        try {
            ErrorResponse body = ErrorResponse.builder()
                    .status(401)
                    .error("Unauthorized")
                    .message("Non authentifié")
                    .path(req.getRequestURI())
                    .errorCode("UNAUTHORIZED")
                    .build();

            res.setStatus(401);
            res.setContentType(MediaType.APPLICATION_JSON_VALUE);
            res.getWriter().write(objectMapper.writeValueAsString(body));
        } catch (Exception ignored) {}
    }
}