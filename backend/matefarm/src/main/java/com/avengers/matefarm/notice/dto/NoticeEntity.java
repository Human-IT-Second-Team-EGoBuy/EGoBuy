package com.avengers.matefarm.notice.dto;


import com.avengers.matefarm.notice.enums.FileExist;
import com.avengers.matefarm.user.dto.UserEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Builder
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Table(name = "notice")
public class NoticeEntity {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notice_id")
    private Long noticeId;

    @Column(name = "notice_title", nullable = false, length = 50)
    private String noticeTitle;

    @Column(name = "notice_content", nullable = false, columnDefinition = "TEXT")
    private String noticeContent;

    @Enumerated(EnumType.STRING)
    @Column(name = "files_tf", nullable = false, length = 1)
    private FileExist filesTf; // 파일 첨부 여부 (Y/N)

    // 작성자와의 연관 관계 (N:1) | 주체(UserEntity를 소유하는 객체) -> 대상(UserEntity)
    // DDL의 writer_id BIGINT와 연결됨
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "writer_id", nullable = false)
    private UserEntity writerId;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;


}

