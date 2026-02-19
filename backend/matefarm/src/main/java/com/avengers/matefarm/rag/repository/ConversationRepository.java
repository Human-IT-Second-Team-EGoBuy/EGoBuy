package com.avengers.matefarm.rag.repository;

import com.avengers.matefarm.rag.dto.entity.ConversationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ConversationRepository extends JpaRepository<ConversationEntity, Long> {

    /** 내 대화 목록: ACTIVE(status=1)만 */
    @Query("""
        select c from ConversationEntity c
        where c.user.userId = :userId
          and c.status = 1
        order by c.lastMessageAt desc nulls last, c.conversationId desc
    """)
    List<ConversationEntity> findList(Long userId);

    /** 내 대화 단건 조회: ACTIVE(status=1)만 (숨김은 404처럼 숨김용) */
    Optional<ConversationEntity> findByConversationIdAndUser_UserIdAndStatus(Long conversationId, Long userId, Integer status);

}
