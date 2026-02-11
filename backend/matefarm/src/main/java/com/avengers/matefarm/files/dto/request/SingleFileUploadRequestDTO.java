package com.avengers.matefarm.files.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import org.springframework.web.multipart.MultipartFile;

// editor를 통해 게시글을 생성할 시 이미지를 드래그 및 복붙할 때 cloudfront Url를 반환해줄 RequestDTO
@Getter
public class SingleFileUploadRequestDTO {

    @JsonProperty("files")
    MultipartFile files;
}
