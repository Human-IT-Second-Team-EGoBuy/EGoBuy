package com.avengers.matefarm.like.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PostLikeResponseDTO {

    @JsonProperty("post_id")
    private Long postId;

    @JsonProperty("like_count")
    private Long likeCount; // 게시글의 총 좋아요 개수

    @JsonProperty("is_liked")
    private boolean isLiked; // 사용자가 좋아요를 눌렀는지 여부
}
