package com.avengers.matefarm.files.policy;

// S3에 업로드 가능한 파일의 타입을 정의


import lombok.Getter;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
public enum AllowedMimeType {

    // 이미지
    IMAGE_PNG("image/png"),
    IMAGE_JPEG("image/jpeg"),
    IMAGE_WEBP("image/webp"),

    // PDF
    PDF("application/pdf"),

    // PowerPoint
    PPT("application/vnd.ms-powerpoint"),
    PPTX("application/vnd.openxmlformats-officedocument.presentationml.presentation"),

    // Excel
    XLS("application/vnd.ms-excel"),
    XLSX("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"),

    // Word
    DOC("application/msword"),
    DOCX("application/vnd.openxmlformats-officedocument.wordprocessingml.document"),

    // HWP
    HWP("application/x-hwp"),
    HWP_ALT("application/haansofthwp");



    private final String mimeType;

    AllowedMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public static Set<String> getAllowedTypes() {
        return Arrays.stream(values())
                .map(AllowedMimeType::getMimeType)
                .collect(Collectors.toSet());
    }

    // 특정 타입이 허용되는지 확인
    public static boolean isAllowed(String mimeType) {

        return getAllowedTypes().contains(mimeType);
    }
}
