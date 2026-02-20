package com.avengers.matefarm.rag.repository;

import com.avengers.matefarm.rag.dto.entity.ConversationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface ConversationRepository extends JpaRepository<ConversationEntity, Long> {

    /** 내 대화 목록: ACTIVE(status=1)만 + 페이징 */
    Page<ConversationEntity> findByUser_UserIdAndStatus(Long userId, Integer status, Pageable pageable);

    /** 내 대화 단건 조회: ACTIVE(status=1)만 (숨김은 404처럼 숨김용) */
    Optional<ConversationEntity> findByConversationIdAndUser_UserIdAndStatus(Long conversationId, Long userId, Integer status);

}
