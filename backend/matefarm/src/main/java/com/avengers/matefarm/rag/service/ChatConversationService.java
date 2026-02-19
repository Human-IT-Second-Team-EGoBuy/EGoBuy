package com.avengers.matefarm.rag.service;

import com.avengers.matefarm.common.exception.CommonException;
import com.avengers.matefarm.common.exception.ErrorCode;
import com.avengers.matefarm.rag.dto.entity.ConversationEntity;
import com.avengers.matefarm.rag.dto.entity.ConversationMessageEntity;
import com.avengers.matefarm.rag.dto.response.ChatMessageDto;
import com.avengers.matefarm.rag.dto.response.ConversationDto;
import com.avengers.matefarm.rag.repository.ConversationMessageRepository;
import com.avengers.matefarm.rag.repository.ConversationRepository;
import com.avengers.matefarm.user.dto.UserEntity;
import com.avengers.matefarm.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatConversationService {

    private final ConversationRepository conversationRepository;
    private final ConversationMessageRepository messageRepository;
    private final UserRepository userRepository;

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

    /** 0) 대화 목록: 무조건 ACTIVE(status=1)만 */
    @Transactional(readOnly = true)
    public List<ConversationDto> list() {
        UserEntity user = getLoginUser();

        List<ConversationEntity> list = conversationRepository.findList(user.getUserId());
        if (list == null) list = Collections.emptyList();

        return list.stream()
                .map(c -> ConversationDto.builder()
                        .conversationId(c.getConversationId())
                        .title(c.getTitle())
                        .status(c.getStatus())
                        .lastMessageAt(c.getLastMessageAt())
                        .messages(null) // 목록은 메시지 미포함
                        .build())
                .toList();
    }

    /** 1) 대화 상세 조회: ACTIVE(status=1)만 (숨김은 404처럼 NOT_FOUND) */
    @Transactional(readOnly = true)
    public ConversationDto getConversation(long conversationId, boolean includeMessages) {
        UserEntity user = getLoginUser();

        if (conversationId <= 0) {
            throw new CommonException(ErrorCode.INVALID_CONVERSATION_ID);
        }

        ConversationEntity conv = conversationRepository
                .findByConversationIdAndUser_UserIdAndStatus(conversationId, user.getUserId(), 1)
                .orElseThrow(() -> new CommonException(ErrorCode.NOT_FOUND_CONVERSATION));

        List<ChatMessageDto> messages = Collections.emptyList();

        if (includeMessages) {
            List<ConversationMessageEntity> msgEntities =
                    messageRepository.findByConversation_ConversationIdAndStatusOrderByCreatedAtAsc(conversationId, 1);

            if (msgEntities == null) msgEntities = Collections.emptyList();

            messages = msgEntities.stream()
                    .map(this::toChatMessageDto)
                    .toList();
        }

        return ConversationDto.builder()
                .conversationId(conv.getConversationId())
                .title(conv.getTitle())
                .status(conv.getStatus())
                .lastMessageAt(conv.getLastMessageAt())
                .messages(includeMessages ? messages : null)
                .build();
    }

    /** 2) 새 대화 생성 */
    @Transactional
    public ConversationDto createConversation() {
        UserEntity user = getLoginUser();

        ConversationEntity saved = conversationRepository.save(
                ConversationEntity.builder()
                        .title("새 대화")
                        .status(1)
                        .user(user)
                        .build()
        );

        return ConversationDto.builder()
                .conversationId(saved.getConversationId())
                .title(saved.getTitle())
                .status(saved.getStatus())
                .lastMessageAt(saved.getLastMessageAt())
                .messages(Collections.emptyList())
                .build();
    }

    /** 3) 대화 숨김(soft delete): ACTIVE인 것만 0으로 변경 */
    @Transactional
    public void patchConversationStatus(long conversationId, int nextStatus) {
        UserEntity user = getLoginUser();

        if (conversationId <= 0) {
            throw new CommonException(ErrorCode.INVALID_CONVERSATION_ID);
        }
        if (nextStatus != 0 ) {
            throw new CommonException(ErrorCode.INVALID_STATUS_VALUE);
        }

        // ACTIVE만 대상으로 조회 → 이미 숨김(status=0)이면 NOT_FOUND_CONVERSATION(404)처럼 숨김
        ConversationEntity conv = conversationRepository
                .findByConversationIdAndUser_UserIdAndStatus(conversationId, user.getUserId(), 1)
                .orElseThrow(() -> new CommonException(ErrorCode.NOT_FOUND_CONVERSATION));

        conv.setStatus(0);
        conversationRepository.save(conv);
    }

    /** Entity -> ChatMessageDto */
    private ChatMessageDto toChatMessageDto(ConversationMessageEntity e) {
        return ChatMessageDto.builder()
                .messageId(e.getConversationsMessagesId())
                .role(e.getRole().name())
                .content(e.getContent())
                .createdAt(e.getCreatedAt())
                .status(e.getStatus()) // DB NOT NULL DEFAULT 1
                .metadata(null)
                .clientMessageId(null)
                .build();
    }
}
