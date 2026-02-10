package com.avengers.matefarm.files.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

// editor를 통해 게시글을 생성할 시 이미지를 드래그 및 복붙할 때 cloudfront Url를 반환해줄 ResponseDTO
@Getter
@Setter
@Builder
public class SingleFileUploadResponseDTO {

    @JsonProperty("cloudfront_url")
    private String cloudfrontUrl;
}
