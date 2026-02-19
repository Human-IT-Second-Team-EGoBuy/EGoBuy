package com.avengers.matefarm.rag.service;

import com.avengers.matefarm.common.exception.CommonException;
import com.avengers.matefarm.common.exception.ErrorCode;
import com.avengers.matefarm.rag.dto.RagResponse;
import com.avengers.matefarm.rag.dto.entity.ConversationEntity;
import com.avengers.matefarm.rag.dto.entity.ConversationMessageEntity;
import com.avengers.matefarm.rag.dto.enums.MessageRole;
import com.avengers.matefarm.rag.dto.response.ChatMessageDto;
import com.avengers.matefarm.rag.dto.response.ConversationDto;
import com.avengers.matefarm.rag.dto.response.SendMessageData;
import com.avengers.matefarm.rag.repository.ConversationMessageRepository;
import com.avengers.matefarm.rag.repository.ConversationRepository;
import com.avengers.matefarm.user.dto.UserEntity;
import com.avengers.matefarm.user.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RagService {

    private final WebClient fastApiWebClient; // Bean으로 주입되어 있다고 가정
    private final ConversationRepository conversationRepository;
    private final ConversationMessageRepository messageRepository;
    private final UserRepository userRepository;

    private final ObjectMapper om = new ObjectMapper();

    /** JWT 로그인 유저(userAuthId) 가져오기 (CommonException 통일) */
    private UserEntity getLoginUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            throw new CommonException(ErrorCode.UNAUTHORIZED_ACCESS);
        }

        String userAuthId = auth.getName();
        if (userAuthId == null || userAuthId.isBlank() || "anonymousUser".equals(userAuthId)) {
            throw new CommonException(ErrorCode.UNAUTHORIZED_ACCESS);
        }

        return userRepository.findByUserAuthId(userAuthId)
                .orElseThrow(() -> new CommonException(ErrorCode.NOT_FOUND_USER));
    }

    @Transactional
    public SendMessageData sendMessage(long conversationId, String content, String clientMessageId) {
        UserEntity user = getLoginUser();

        if (conversationId <= 0) {
            throw new CommonException(ErrorCode.INVALID_CONVERSATION_ID);
        }

        String userText = (content == null) ? "" : content.trim();
        if (userText.isEmpty()) {
            throw new CommonException(ErrorCode.EMPTY_MESSAGE_CONTENT);
        }

        // 핵심: ACTIVE(status=1) 대화만 조회 → 숨김(status=0)은 404처럼 NOT_FOUND
        ConversationEntity conv = conversationRepository
                .findByConversationIdAndUser_UserIdAndStatus(conversationId, user.getUserId(), 1)
                .orElseThrow(() -> new CommonException(ErrorCode.NOT_FOUND_CONVERSATION));

        // 1) USER 메시지 저장
        ConversationMessageEntity savedUserEntity = messageRepository.save(
                ConversationMessageEntity.builder()
                        .conversation(conv)
                        .role(MessageRole.USER)
                        .content(userText)
                        .status(1)
                        .metadata(null)
                        .build()
        );

        ChatMessageDto userMsg = ChatMessageDto.builder()
                .messageId(savedUserEntity.getConversationsMessagesId())
                .role("USER")
                .content(savedUserEntity.getContent())
                .createdAt(savedUserEntity.getCreatedAt() != null ? savedUserEntity.getCreatedAt() : LocalDateTime.now())
                .status(savedUserEntity.getStatus())
                .metadata(null)
                .clientMessageId(clientMessageId)
                .build();

        // 2) FastAPI 호출
        RagResponse ragRes = callFastApi(userText);

        // 3) ASSISTANT 메시지 저장 (metadata는 DB엔 JSON string)
        Map<String, Object> metadataMap = new HashMap<>();
        metadataMap.put("provider", "fastapi");
        metadataMap.put("intent", ragRes.getIntent());
        metadataMap.put("citations", ragRes.getCitations());

        String metadataJson = null;
        try {
            metadataJson = om.writeValueAsString(metadataMap);
        } catch (Exception ignored) {}

        ConversationMessageEntity savedAssistantEntity = messageRepository.save(
                ConversationMessageEntity.builder()
                        .conversation(conv)
                        .role(MessageRole.ASSISTANT)
                        .content(ragRes.getAnswer())
                        .status(1)
                        .metadata(metadataJson)
                        .build()
        );

        ChatMessageDto assistantMsg = ChatMessageDto.builder()
                .messageId(savedAssistantEntity.getConversationsMessagesId())
                .role("ASSISTANT")
                .content(savedAssistantEntity.getContent())
                .createdAt(savedAssistantEntity.getCreatedAt() != null ? savedAssistantEntity.getCreatedAt() : LocalDateTime.now())
                .status(savedAssistantEntity.getStatus())
                .metadata(metadataMap) // 응답에서는 Map으로
                .clientMessageId(clientMessageId)
                .build();

        // 4) conversation 업데이트: lastMessageAt + title(초기만)
        LocalDateTime now = LocalDateTime.now();
        conv.setLastMessageAt(now);

        if (conv.getTitle() == null || conv.getTitle().isBlank() || "새 대화".equals(conv.getTitle())) {
            conv.setTitle(makeTitleFromContent(userText));
        }

        conversationRepository.save(conv);

        ConversationDto convDto = ConversationDto.builder()
                .conversationId(conv.getConversationId())
                .title(conv.getTitle())
                .status(conv.getStatus())
                .lastMessageAt(conv.getLastMessageAt())
                .messages(List.of()) // send 응답에는 굳이 전체 메시지 안 넣음
                .build();

        return SendMessageData.builder()
                .userMessage(userMsg)
                .assistantMessage(assistantMsg)
                .conversation(convDto)
                .build();
    }

    private RagResponse callFastApi(String userText) {
        Map<String, Object> body = new HashMap<>();
        body.put("message", userText);

        try {
            RagResponse ragRes = fastApiWebClient.post()
                    .uri("/chat_rag")
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(RagResponse.class)
                    .block();

            if (ragRes == null || ragRes.getAnswer() == null) {
                throw new CommonException(ErrorCode.EXTERNAL_API_ERROR);
            }
            return ragRes;

        } catch (WebClientResponseException e) {
            // FastAPI가 4xx/5xx를 준 경우
            throw new CommonException(ErrorCode.EXTERNAL_API_ERROR);
        } catch (CommonException e) {
            throw e;
        } catch (Exception e) {
            // 타임아웃/네트워크 등
            throw new CommonException(ErrorCode.EXTERNAL_API_ERROR);
        }
    }

    private String makeTitleFromContent(String content) {
        String t = content == null ? "" : content.trim();
        if (t.isEmpty()) return "대화";
        return (t.length() <= 20) ? t : t.substring(0, 20);
    }
}
