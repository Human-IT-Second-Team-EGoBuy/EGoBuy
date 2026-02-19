package com.avengers.matefarm.rag.dto.request;

import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class MessageSendRequest {
    private String content; // React Body: { content: "..." }
    private String clientMessageId;
}
