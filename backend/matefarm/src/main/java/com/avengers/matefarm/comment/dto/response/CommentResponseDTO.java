package com.avengers.matefarm.comment.dto.response;


import com.avengers.matefarm.comment.dto.CommentEntity;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Builder
@Getter
@Setter
public class CommentResponseDTO {

    @JsonProperty("post_id")
    private Long postId;

    @JsonProperty("comment_id")
    private Long commentId;

    @JsonProperty("comment_content")
    private String commentContent;

    @JsonProperty("writer_id")
    private Long writerId;

    @JsonProperty("nickname")
    private String nickname;

//    @JsonProperty("recomment")
//    private List<RecommentResponseDTO> recomment;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    // Entity -> ResponseDTO 변환 메서드
    public static CommentResponseDTO from(CommentEntity entity) {
        return CommentResponseDTO.builder()
                .commentId(entity.getCommentId())
                .commentContent(entity.getCommentContent())
                .createdAt(entity.getCreatedAt())
                .nickname(entity.getWriter().getNickname()) // UserEntity에서 가져옴
                .writerId(entity.getWriter().getUserId())
                .postId(entity.getPostId().getCommunityPostId()) // PostEntity에서 가져옴
                .build();
    }
}

