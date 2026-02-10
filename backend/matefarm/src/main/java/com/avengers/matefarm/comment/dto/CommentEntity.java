package com.avengers.matefarm.comment.dto;

import com.avengers.matefarm.comment.enums.DeleteYN;
import com.avengers.matefarm.communitypost.dto.CommunityPostEntity;
import com.avengers.matefarm.user.dto.UserEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

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
    private UserEntity writer;

    // 게시글과의 관계 (N:1)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private CommunityPostEntity postId;

    // 대댓글과의 관계 (1:N)
    // @OneToMany(mappedBy = "comment", cascade = CascadeType.ALL, orphanRemoval = true)
    // private List<ReplyEntity> replies = new ArrayList<>();

    /* from 메소드 */
    public static CommentEntity from(String content, UserEntity writer, CommunityPostEntity post) {
        return CommentEntity.builder()
                .commentContent(content)
                .writer(writer)
                .postId(post)
                .deleteYn(DeleteYN.N)
                .createdAt(LocalDateTime.now().withNano(0))
                .build();
    }


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
