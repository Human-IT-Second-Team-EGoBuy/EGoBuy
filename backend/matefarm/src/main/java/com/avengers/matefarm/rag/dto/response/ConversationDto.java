package com.avengers.matefarm.rag.dto.response;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class ConversationDto {
    private Long conversationId;
    private String title;
    private Integer status;
    private LocalDateTime lastMessageAt;
    private List<ChatMessageDto> messages;
}
