package com.avengers.matefarm.insectpestinfo.common;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
        String code,
        String message,
        T data
) {
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(ApiCode.SU.name(), null, data);
    }

    public static <T> ApiResponse<T> notFound(String message) {
        return new ApiResponse<>(ApiCode.NF.name(), message, null);
    }

    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(ApiCode.ER.name(), message, null);
    }
}