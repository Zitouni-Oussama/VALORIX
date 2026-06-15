package com.recyclix.backend.dto.support;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AnonymousTicketRequestDTO {

    @Email
    @Size(max = 255)
    private String email;

    @Size(max = 100)
    private String fullName;

    @Size(max = 20)
    private String phone;

    private String roleType;
}