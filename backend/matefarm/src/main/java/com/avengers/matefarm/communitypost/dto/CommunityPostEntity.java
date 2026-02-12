package com.avengers.matefarm.communitypost.dto;

import com.avengers.matefarm.communitypost.enums.PostType;
import com.avengers.matefarm.notice.enums.FileExist;
import com.avengers.matefarm.user.dto.UserEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Entity
@Table(name = "community_post")
public class CommunityPostEntity {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "community_post_id")
    private Long communityPostId;

    @Column(name = "community_post_title", nullable = false, length = 50)
    private String communityPostTitle;

    @Column(name = "community_post_content", nullable = false, columnDefinition = "TEXT")
    private String communityPostContent;

    @Enumerated(EnumType.STRING)
    @Column(name = "post_type", nullable = false)
    private PostType postType;

    @Column(name = "liked_count", nullable = false)
    private int likedCount;

    @Column(name = "reported_count", nullable = false)
    private int reportedCount;

    @Column(name = "view_count", nullable = false)
    private int viewCount;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "files_tf", nullable = false)
    private FileExist filesTf;

    // 작성자와의 연관 관계 (N:1)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "writer_id", nullable = false)
    private UserEntity writerId;


    /* 생성 시 기본값 세팅 */
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now().withNano(0);
        this.likedCount = 0;
        this.reportedCount = 0;
        this.viewCount = 0;
    }

    public void increaseLikeCount() {
        this.likedCount++;
    }

    public void decreaseLikeCount() {
        if (this.likedCount > 0) {
            this.likedCount--;
        }
    }
}
