package com.recyclix.backend.security;

import com.recyclix.backend.exception.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AccessDeniedHandlerImpl implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void handle(HttpServletRequest req, HttpServletResponse res, AccessDeniedException ex) {
        try {
            ErrorResponse body = ErrorResponse.builder()
                    .status(403)
                    .error("Forbidden")
                    .message("Accès refusé")
                    .path(req.getRequestURI())
                    .errorCode("FORBIDDEN")
                    .build();

            res.setStatus(403);
            res.setContentType(MediaType.APPLICATION_JSON_VALUE);
            res.getWriter().write(objectMapper.writeValueAsString(body));
        } catch (Exception ignored) {}
    }
}