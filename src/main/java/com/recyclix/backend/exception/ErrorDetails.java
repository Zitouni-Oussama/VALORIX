package com.recyclix.backend.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorDetails {

    private String field;          // ex: "email"
    private String message;        // ex: "ne peut pas être nul"
    private Object rejectedValue;  // ex: "abc"
}