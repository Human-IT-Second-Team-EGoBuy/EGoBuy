package com.avengers.matefarm.rag.repository;

import com.avengers.matefarm.rag.dto.entity.ConversationMessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ConversationMessageRepository extends JpaRepository<ConversationMessageEntity, Long> {

    /** 대화방 메시지 전체(숨김 포함) */
    List<ConversationMessageEntity> findByConversation_ConversationIdOrderByCreatedAtAsc(Long conversationId);

    /** 대화방 메시지(status=1만) */
    List<ConversationMessageEntity> findByConversation_ConversationIdAndStatusOrderByCreatedAtAsc(
            Long conversationId, Integer status
    );
}
