package com.avengers.matefarm.notice.dto.response;

import com.avengers.matefarm.notice.dto.NoticeEntity;
import com.avengers.matefarm.notice.enums.FileExist;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Builder    // from 정적 메서드 사용을 위해 추가
@Getter
@AllArgsConstructor
public class NoticeResponseDTO {

    @Column(name = "notice_id")
    private Long noticeId;

    @JsonProperty("notice_title")
    private String noticeTitle;

    @JsonProperty("notice_content")
    private String noticeContent;

    @JsonProperty("file_tf")
    private FileExist filesTf;

    @JsonProperty("writer_id")
    private Long writerId;

    @JsonProperty("writer_name")
    private String writerName;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    public static NoticeResponseDTO from(NoticeEntity entity) {
        return NoticeResponseDTO.builder()
                .noticeId(entity.getNoticeId())
                .noticeTitle(entity.getNoticeTitle())
                .noticeContent(entity.getNoticeContent())   // 누락되어 추가
                .filesTf(entity.getFilesTf())               // 누락되어 추가
                .writerId(entity.getWriterId().getUserId()) // Jpa가 UserEntity타입의 객체를 반환하므로 여기에서 닉네임을 추출
                .writerName(entity.getWriterId().getUserName())
                .build();
    }
}
