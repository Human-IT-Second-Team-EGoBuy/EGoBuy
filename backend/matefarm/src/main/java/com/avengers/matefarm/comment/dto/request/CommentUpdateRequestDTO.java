package com.avengers.matefarm.comment.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class CommentUpdateRequestDTO {

    @JsonProperty("new_comment")
    private String newComment;

//    @JsonProperty("comment_id") PathVariable로 받음
//    private Long commentId;

    @JsonProperty("writer_id")
    private int writerId;
}
