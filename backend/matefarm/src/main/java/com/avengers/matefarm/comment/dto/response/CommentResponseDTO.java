package com.avengers.matefarm.comment.dto.response;


import com.avengers.matefarm.comment.dto.CommentEntity;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

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

    @JsonProperty("re_comment")
    private List<CommentResponseDTO> reComment;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    // Entity -> ResponseDTO 변환 메서드
    public static CommentResponseDTO from(CommentEntity entity) {
        return CommentResponseDTO.builder()
                .commentId(entity.getCommentId())
                .commentContent(entity.getCommentContent())
                .createdAt(entity.getCreatedAt())
                .nickname(entity.getWriterId().getNickname()) // UserEntity에서 가져옴
                .writerId(entity.getWriterId().getUserId())
                .postId(entity.getPostId().getCommunityPostId()) // PostEntity에서 가져옴
                .reComment(entity.getChildren() != null ?       // Entity를 반환받고, 부모 객체에 List로 담긴 자식 객체를 List로 반환.
                        entity.getChildren().stream()
                                .map(CommentResponseDTO::from)
                                .collect(Collectors.toList()) : null)
                .build();
    }
}

