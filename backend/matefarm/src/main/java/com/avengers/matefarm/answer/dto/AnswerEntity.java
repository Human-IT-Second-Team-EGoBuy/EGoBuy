package com.avengers.matefarm.answer.dto;

import com.avengers.matefarm.answer.enums.DeleteStatus;
import com.avengers.matefarm.inquiry.dto.InquiryEntity;
import com.avengers.matefarm.user.dto.UserEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "answer")
public class AnswerEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "answer_id")
    private Long answerId;

    @Column(name = "answer_content", nullable = false, columnDefinition = "TEXT")
    private String answerContent;

    @Enumerated(EnumType.STRING)
    @Column(name = "is_deleted", nullable = false)
    private DeleteStatus isDeleted; // Y, N 관리를 위한 Enum

    // 문의와의 관계 (1:1)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inquiry_id", nullable = false)
    private InquiryEntity inquiryId;

    // 답변자와의 관계 (N:1)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "answer_user_id", nullable = false)
    private UserEntity answerUserId; // 답변을 작성한 관리자

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    /* 답변 수정 */
    public void updateAnswer(String content) {
        this.answerContent = content;
        this.updatedAt = LocalDateTime.now().withNano(0);
    }

    /* 답변 삭제 (Soft Delete) */
    public void deleteAnswer() {
        this.isDeleted = DeleteStatus.Y;
        this.deletedAt = LocalDateTime.now().withNano(0);
    }
}
