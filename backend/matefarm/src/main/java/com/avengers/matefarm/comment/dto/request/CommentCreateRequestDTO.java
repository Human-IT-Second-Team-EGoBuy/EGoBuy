package com.avengers.matefarm.comment.dto.request;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class CommentCreateRequestDTO {

    @JsonProperty("post_id")
    private Long postId;

    @JsonProperty("comment")
    private String comment;

    @JsonProperty("writer_id")
    private Long writerId;

    @JsonProperty("parent_id") // 추가. 일반 댓글이면 null, 대댓글이면 부모 ID
    private Long parentId;
}
