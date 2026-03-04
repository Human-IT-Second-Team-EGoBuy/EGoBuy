package com.avengers.matefarm.rag.dto.entity;

import com.avengers.matefarm.user.dto.UserEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
@Table(name = "conversations")
public class ConversationEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "conversation_id")
  private Long conversationId;

  @Column(name = "title", nullable = false, length = 255)
  private String title;

  @JdbcTypeCode(SqlTypes.TINYINT)
  @Column(name = "status", nullable = false)
  private Integer status; // 1=ACTIVE, 0=HIDDEN

  @Column(name = "last_message_at")
  private LocalDateTime lastMessageAt;

  /** users.user_id FK */
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", nullable = false)
  private UserEntity user;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;

  @PrePersist
  void prePersist() {
      if (title == null || title.isBlank()) title = "새 대화";
      if (status == null) status = 1;
      
  }
}
