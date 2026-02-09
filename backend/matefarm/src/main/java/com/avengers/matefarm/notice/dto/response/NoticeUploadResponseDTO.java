package com.avengers.matefarm.notice.dto.response;

import com.avengers.matefarm.files.dto.response.FilesResponseDTO;
import com.avengers.matefarm.notice.enums.FileExist;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
public class NoticeUploadResponseDTO {

    @JsonProperty("notice_id")
    private long noticeId;

    @JsonProperty("notice_title")
    private String noticeTitle;

    @JsonProperty("notice_content")
    private String noticeContent;

    @JsonProperty("file_exist")     // 조회 시 사용할 파일 여부 필드 추가
    private FileExist fileExist;

    @JsonProperty("files")
    private List<FilesResponseDTO> files;   // 파일에 관련된 정보를 List로 반환하는 객체

}
