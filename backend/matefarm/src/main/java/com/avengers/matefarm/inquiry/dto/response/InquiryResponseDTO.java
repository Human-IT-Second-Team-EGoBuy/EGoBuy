package com.avengers.matefarm.inquiry.dto.response;

import com.avengers.matefarm.inquiry.dto.InquiryEntity;
import com.avengers.matefarm.inquiry.enums.InquiryStatus;
import com.avengers.matefarm.inquiry.enums.InquiryType;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InquiryResponseDTO {

    private Long inquiryId;
    private String inquiryTitle;
    private String inquiryContent;
    private InquiryType inquiryType;
    private InquiryStatus inquiryStatus;
    private String writerNickname; // 작성자 식별을 위한 닉네임
    private Long writerId;         // 작성자 고유 번호
    private LocalDateTime createdAt;
    private LocalDateTime answeredAt;

    /* Entity -> from 메소드 */
    public static InquiryResponseDTO from(InquiryEntity entity) {
        return InquiryResponseDTO.builder()
                .inquiryId(entity.getInquiryId())
                .inquiryTitle(entity.getInquiryTitle())
                .inquiryContent(entity.getInquiryContent())
                .inquiryType(entity.getInquiryType())
                .inquiryStatus(entity.getInquiryStatus())
                // 연관된 UserEntity에서 필요한 정보 추출
                .writerId(entity.getWriterId().getUserId())
                .writerNickname(entity.getWriterId().getNickname())
                .createdAt(entity.getCreatedAt())
                .answeredAt(entity.getAnsweredAt())
                .build();
    }
}
