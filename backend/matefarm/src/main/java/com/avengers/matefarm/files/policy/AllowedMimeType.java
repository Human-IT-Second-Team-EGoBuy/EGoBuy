package com.avengers.matefarm.files.policy;

// S3에 업로드 가능한 파일의 타입을 정의


import lombok.Getter;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
public enum AllowedMimeType {

    // 이미지 ( 필요 시 추가할 것 )
    IMAGE_PNG("image/png", true),
    IMAGE_JPEG("image/jpeg", true),
    IMAGE_WEBP("image/webp", true),
    IMAGE_GIF("image/gif", true),   // 움짤

            /* ******************************************************************* */

    // PDF
    PDF("application/pdf", false),

    // PowerPoint
    PPT("application/vnd.ms-powerpoint", false),
    PPTX("application/vnd.openxmlformats-officedocument.presentationml.presentation", false),

    // Excel
    XLS("application/vnd.ms-excel", false),
    XLSX("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", false),

    // Word
    DOC("application/msword", false),
    DOCX("application/vnd.openxmlformats-officedocument.wordprocessingml.document", false),

    // HWP
    HWP("application/x-hwp", false),
    HWP_ALT("application/haansofthwp", false);



    private final String mimeType;
    private final Boolean isImage;

    AllowedMimeType(String mimeType, Boolean isImage) {
        this.mimeType = mimeType;
        this.isImage = isImage;
    }

    /* Type이 중복되는 것을 방지하기 위해 Set을 사용 */

    // mimeType 필드를 통해 추출
    public static Set<String> getAllowedTypes() {
        return Arrays.stream(values())
                .map(AllowedMimeType::getMimeType)
                .collect(Collectors.toSet());
    }

    //  이미지 타입만 추출
    public static Set<String> getImageTypes() {
        return Arrays.stream(values())
                .filter(AllowedMimeType::getIsImage) // 이미지인 것만 필터링
                .map(AllowedMimeType::getMimeType)
                .collect(Collectors.toSet());
    }

    // 특정 타입이 허용되는지 확인
    public static boolean isAllowed(String mimeType) {

        return getAllowedTypes().contains(mimeType);
    }
}
