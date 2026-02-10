package com.avengers.matefarm.notice.dto.request;

import com.avengers.matefarm.notice.enums.FileExist;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class NoticeUploadRequestDTO {

//    @Column(name = "notice_id")
//    private Long noticeId;

    @JsonProperty("notice_title")
    private String noticeTitle;

    @JsonProperty("notice_content")
    private String noticeContent;

    @JsonProperty("file_tf")
    private FileExist fileExist;

    @JsonProperty("writer_id")
    private Long writerId;

    private List<MultipartFile> files;
}
