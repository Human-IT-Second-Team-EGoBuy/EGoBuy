package com.avengers.matefarm.rag.dto.response;

import lombok.*;
import java.time.LocalDateTime;
import java.util.Map;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class ChatMessageDto {
    private long messageId;
    private String role;          // "USER" | "ASSISTANT"
    private String content;
    private LocalDateTime createdAt;
    private int status;           // 1 임시
    private Map<String, Object> metadata; // assistant만 사용
    private String clientMessageId;
}
