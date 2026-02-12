package com.avengers.matefarm.rag.dto;

import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class SendMessageData {
    private ChatMessageDto userMessage;
    private ChatMessageDto assistantMessage;
    private ConversationDto conversation;
}
