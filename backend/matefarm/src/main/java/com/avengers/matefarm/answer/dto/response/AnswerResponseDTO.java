package com.avengers.matefarm.answer.dto.response;

import com.avengers.matefarm.answer.dto.AnswerEntity;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class AnswerResponseDTO {

    @JsonProperty("answer_id")
    private Long answerId;

    @JsonProperty("answer_content")
    private String answerContent;

    @JsonProperty("admin_nickname")
    private String adminNickname; // 답변한 관리자

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("updated_at")
    private LocalDateTime updatedAt; // 수정되었을 경우

    /* from 메소드 */
    public static AnswerResponseDTO from(AnswerEntity entity) {
        return AnswerResponseDTO.builder()
                .answerId(entity.getAnswerId())
                .answerContent(entity.getAnswerContent())
                .adminNickname(entity.getAnswerUserId().getNickname()) // 관리자 닉네임
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
