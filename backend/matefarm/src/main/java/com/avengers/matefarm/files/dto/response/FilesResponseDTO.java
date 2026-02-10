package com.avengers.matefarm.files.dto.response;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

// ResponseDTO는 하나로 통일하여 사용.
@Builder
@Getter
@Setter
public class FilesResponseDTO {

    /* 게시글 생성 시, 바로 페이지에서 렌더링 될 수 있도록 S3 객체의 접근 경로를 반환해주는 필드.
     * 단, 파일 업로드 및 삭제는 Backend -> S3로 직접 접근하지만 조회의 경우 Cloudfront를 앞단에 두었기 때문에
     * Client -> Cloudfront -> S3 로 접근하므로, 모든 조회 요청을 앞단에서 받는 Cloudfront의 도메인을 제공한다.
     *
     * List<FilesUploadResponseDTO> 구조로 사용할 것. ( 여러 파일 정보를 보내야 하므로 )
    * */

    @JsonProperty("file_id")
    private long fileId;

    @JsonProperty("file_name")
    private String fileOriginalName;

    @JsonProperty("object_key")
    private String objectKey;   // FilesEntity에 objectKey값 넣기 위해 추가

    @JsonProperty("file_size")
    private long fileSize;

    @JsonProperty("cloudfront_url")
    private String cloudFrontUrl;   // CloudFront URL
}
