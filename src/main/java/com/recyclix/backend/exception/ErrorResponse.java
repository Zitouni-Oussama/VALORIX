package com.recyclix.backend.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    @Builder.Default
    private Instant timestamp = Instant.now();

    private int status;            // 400, 401, ...
    private String error;          // "Bad Request"
    private String message;        // message clair
    private String path;           // URI demandée
    private String errorCode;      // optionnel: ex "ACCOUNT_NOT_FOUND"
    private List<ErrorDetails> details;
}