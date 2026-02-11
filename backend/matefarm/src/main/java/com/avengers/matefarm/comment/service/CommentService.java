package com.avengers.matefarm.comment.service;

import com.avengers.matefarm.comment.dto.request.CommentCreateRequestDTO;
import com.avengers.matefarm.comment.dto.request.CommentUpdateRequestDTO;
import com.avengers.matefarm.comment.dto.response.CommentResponseDTO;

import java.util.List;

public interface CommentService {
    CommentResponseDTO createComment(CommentCreateRequestDTO requestDTO);

    List<CommentResponseDTO> getComments(Long postId);

    CommentResponseDTO updateComment(Long commentId, CommentUpdateRequestDTO requestDTO, Long userId);

    void deleteComment(Long commentId, Long userId);
}
