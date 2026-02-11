package com.avengers.matefarm.rag.dto;

import lombok.*;
import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class ConversationDto {
    private long conversationId;
    private String title;
    private LocalDateTime lastMessageAt;
}
