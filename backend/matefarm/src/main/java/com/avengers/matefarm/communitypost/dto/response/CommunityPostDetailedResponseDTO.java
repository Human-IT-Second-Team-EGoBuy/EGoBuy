package com.avengers.matefarm.communitypost.dto.response;


import com.avengers.matefarm.communitypost.dto.CommunityPostEntity;
import com.avengers.matefarm.notice.enums.FileExist;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@JsonPropertyOrder({            // 반환되는 Json 데이터의 순서를 보장하기 위해 사용
        "community_post_id",
        "community_post_title",
        "community_post_content",
        "nickname",
        "view_count",
        "liked_count",
        "reported_count",
        "created_at",
        "file_tf"
})
public class CommunityPostDetailedResponseDTO {

    @JsonProperty("community_post_id")
    private Long communityPostId;

    @JsonProperty("community_post_title")
    private String communityPostTitle;

    @JsonProperty("community_post_content")
    private String communityPostContent;

    @JsonProperty("liked_count")
    private int likedCount;

    @JsonProperty("reported_count")
    private int reportedCount;

    @JsonProperty("view_count")
    private int viewCount;

    @JsonProperty("file_tf")
    private boolean fileTf;

    @JsonProperty("nickname")
    private String nickname;        // 게시글 작성자

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    /* from 메소드 */
    public static CommunityPostDetailedResponseDTO from(CommunityPostEntity entity) {
        return CommunityPostDetailedResponseDTO.builder()
                .communityPostId(entity.getCommunityPostId())
                .communityPostTitle(entity.getCommunityPostTitle())
                .communityPostContent(entity.getCommunityPostContent())
                .likedCount(entity.getLikedCount())
                .reportedCount(entity.getReportedCount())
                .viewCount(entity.getViewCount())
                .fileTf(entity.getFilesTf() == FileExist.Y) // 파일 여부
                .nickname(entity.getWriterId().getNickname())   // entity에서 닉네임 추출
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
