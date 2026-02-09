package com.avengers.matefarm.notice.dto;

import com.avengers.matefarm.notice.enums.FileExist;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;

import java.time.LocalDateTime;

// Request, ResponseDTO 의 종류에 따라 쓸 원본 DTO
public class NoticeDTO {

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

    @JsonProperty("created_at")
    private LocalDateTime createdAt;
}
