package com.miazhang.cropplanner.common;

import java.util.Map;

/** Error body returned by every failed request. */
public record ApiError(int status, String message, Map<String, String> fieldErrors) {

    public static ApiError of(int status, String message) {
        return new ApiError(status, message, Map.of());
    }
}
