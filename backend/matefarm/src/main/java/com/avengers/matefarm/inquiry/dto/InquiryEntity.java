package com.avengers.matefarm.inquiry.dto;


import com.avengers.matefarm.inquiry.enums.InquiryStatus;
import com.avengers.matefarm.inquiry.enums.InquiryType;
import com.avengers.matefarm.user.dto.UserEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "inquiry")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InquiryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "inquiry_id")
    private Long inquiryId;

    @Column(name = "inquiry_title", length = 50, nullable = false)
    private String inquiryTitle;

    @Column(name = "inquiry_content", columnDefinition = "TEXT", nullable = false)
    private String inquiryContent;

    @Enumerated(EnumType.STRING)
    @Column(name = "inquiry_type")
    private InquiryType inquiryType;

    @Enumerated(EnumType.STRING)
    @Column(name = "inquiry_status", nullable = false)
    private InquiryStatus inquiryStatus;

    // 유저와의 연관관계 (N:1)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "writer_id", nullable = false)
    private UserEntity writerId;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "answered_at")
    private LocalDateTime answeredAt;

    /* Entity객체 상태 변경용 메소드 */
    public void ChangeStatusToProcessing() {
        this.inquiryStatus = InquiryStatus.PROCESSING;
    }

    /* Entity객체 상태 변경용 메소드 */
    public void ChangeStatusToCompleted() {
        this.inquiryStatus = InquiryStatus.COMPLETED;
    }

    /* 영속성 컨텍스트가 관리하는 엔티티의 스냅샷을 업데이트 하기 위한 메소드 */
    public void updateInquiry(String inquiryTitle, String inquiryContent, InquiryType inquiryType) {
        this.inquiryTitle = inquiryTitle;
        this.inquiryContent = inquiryContent;
        this.inquiryType = inquiryType;
    }
}


