package com.recyclix.backend.util;

import lombok.experimental.UtilityClass;

@UtilityClass
public class Constants {

    // Pagination
    public static final int DEFAULT_PAGE = 0;
    public static final int DEFAULT_SIZE = 20;
    public static final int MAX_SIZE = 100;

    // Money
    public static final int MONEY_SCALE = 3;

    // Dates
    public static final String DATE_PATTERN = "yyyy-MM-dd";
    public static final String DATETIME_PATTERN = "yyyy-MM-dd'T'HH:mm:ss";

    // Headers
    public static final String HEADER_AUTHORIZATION = "Authorization";

    // Upload
    public static final long MAX_UPLOAD_BYTES = 5L * 1024 * 1024; // 5MB
}