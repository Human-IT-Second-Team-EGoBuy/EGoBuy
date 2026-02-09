package com.avengers.matefarm.notice.dto.response;

import com.avengers.matefarm.files.dto.response.FilesResponseDTO;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class NoticeDetailResponseDTO {

    private Long noticeId;
    private String noticeTitle;
    private String noticeContent;
    private String filesTf;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 작성자 정보
    private String writerNickname;
    private Long writerId;

    // 파일  (업로드 결과와 동일)
    private List<FilesResponseDTO> files;

}
