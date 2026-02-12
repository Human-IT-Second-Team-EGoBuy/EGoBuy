package com.avengers.matefarm.rag.service;

import com.avengers.matefarm.rag.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@Service
@RequiredArgsConstructor
public class RagService {

    private final WebClient fastApiWebClient; // 기존 config Bean 사용

    // DB 없으니 임시 messageId 생성기
    private static final AtomicLong ID_GEN = new AtomicLong(5000);

    public SendMessageData sendMessage(long conversationId, String content) {

        // 1) USER 메시지(임시 생성)
        LocalDateTime userTime = LocalDateTime.now();
        ChatMessageDto userMsg = ChatMessageDto.builder()
                .messageId(ID_GEN.incrementAndGet())
                .role("USER")
                .content(content)
                .createdAt(userTime)
                .status(1)
                .metadata(null)
                .build();

        // 2) FastAPI 요청 바디 (최소: message만)
        Map<String, Object> body = new HashMap<>();
        body.put("message", content);

        // history는 없어도 됨(기본 []), 넣고 싶으면 아래 주석 해제
        // body.put("history", List.of(Map.of("role","USER","content",content)));
        // body.put("chatroom_id", conversationId);
        // body.put("crop_id", 0);

        // history/chatroom_id/crop_id는 지금은 안 보내도 됨(전부 optional/default)
        RagResponse ragRes = fastApiWebClient.post()
                .uri("/chat_rag")
                .bodyValue(body)
                .retrieve()
                .bodyToMono(RagResponse.class)
                .block();

        if (ragRes == null) throw new RuntimeException("FastAPI 응답이 비어있음");

        // 3) ASSISTANT 메시지(임시 생성)
        LocalDateTime assistantTime = LocalDateTime.now();

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("provider", "fastapi");
        // model/latencyMs는 FastAPI가 안 주면 일단 생략 또는 null 처리
        // metadata.put("model", "gpt-5.2");
        // metadata.put("latencyMs", 980);
        metadata.put("intent", ragRes.getIntent());
        metadata.put("citations", ragRes.getCitations());

        ChatMessageDto assistantMsg = ChatMessageDto.builder()
                .messageId(ID_GEN.incrementAndGet())
                .role("ASSISTANT")
                .content(ragRes.getAnswer())
                .createdAt(assistantTime)
                .status(1)
                .metadata(metadata)
                .build();

        // 4) conversation(임시 생성)
        String title = makeTitleFromContent(content);
        ConversationDto conv = ConversationDto.builder()
                .conversationId(conversationId)
                .title(title)
                .lastMessageAt(assistantTime)
                .build();

        return SendMessageData.builder()
                .userMessage(userMsg)
                .assistantMessage(assistantMsg)
                .conversation(conv)
                .build();
    }

    private String makeTitleFromContent(String content) {
        String t = content == null ? "" : content.trim();
        if (t.isEmpty()) return "대화";
        return (t.length() <= 20) ? t : t.substring(0, 20);
    }
}
