package com.recyclix.backend.security;

import lombok.experimental.UtilityClass;

@UtilityClass
public class SecurityConstants {
    public static final String AUTH_HEADER = "Authorization";
    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String CLAIM_SUBJECT = "sub";
    public static final String CLAIM_ROLE = "role";
    public static final String CLAIM_ACCOUNT_ID = "accountId";
}