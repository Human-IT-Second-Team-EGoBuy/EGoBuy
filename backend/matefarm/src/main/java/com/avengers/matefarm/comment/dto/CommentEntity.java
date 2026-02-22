package com.avengers.matefarm.comment.dto;

import com.avengers.matefarm.comment.enums.DeleteYN;
import com.avengers.matefarm.communitypost.dto.CommunityPostEntity;
import com.avengers.matefarm.user.dto.UserEntity;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "comment")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comment_id")
    private Long commentId;

    @Column(name = "comment_content", length = 255)
    private String commentContent;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "delete_yn")
    private DeleteYN deleteYn; // 'Y', 'N'

    // 작성자와의 관계 (N:1)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "writer_id", nullable = false)
    private UserEntity writerId;

    // 게시글과의 관계 (N:1)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private CommunityPostEntity postId;

    // 추가 : 대댓글의 경우 부모 댓글의 Id를 가짐 | 주체(대댓글) -> 대상(부모)
    @ManyToOne(fetch = FetchType.LAZY)
    @JsonProperty("parent_id")
    private CommentEntity parentId;

    // 추가 : 댓글과의 관계 1:N  | 주체(댓글) -> 대상(대댓글)
    @Builder.Default    // builder로 생성될 때 여기서 설정한 초기화 식이 제대로 먹히지 않아 null 값이 생기는 것을 방지하기 위해 추가
    @OneToMany(mappedBy = "parentId", cascade = CascadeType.ALL)
    private List<CommentEntity> children = new ArrayList<>();




    /* @Setter 사용하지 않고 특정 필드에만 접근하도록 사용 */

    // 댓글 수정
    public void updateComment(String newComment) {
        this.commentContent = newComment;
        this.updatedAt = LocalDateTime.now().withNano(0);
    }

    // 댓글 삭제
    public void deleteComment() {
        this.deleteYn = DeleteYN.Y;
        this.updatedAt = LocalDateTime.now().withNano(0);
    }
}
