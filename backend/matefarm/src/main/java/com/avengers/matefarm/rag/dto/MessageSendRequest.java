package com.avengers.matefarm.rag.dto;

import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class MessageSendRequest {
    private String content; // React Body: { content: "..." }
}
