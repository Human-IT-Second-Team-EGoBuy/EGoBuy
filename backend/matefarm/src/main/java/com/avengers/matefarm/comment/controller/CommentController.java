package com.avengers.matefarm.comment.controller;

import com.avengers.matefarm.comment.dto.request.CommentCreateRequestDTO;
import com.avengers.matefarm.comment.dto.request.CommentUpdateRequestDTO;
import com.avengers.matefarm.comment.dto.response.CommentResponseDTO;
import com.avengers.matefarm.comment.service.CommentService;
import com.avengers.matefarm.common.ResponseDTO;
import com.avengers.matefarm.security.JwtUtil;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController("CommentController")
@RequestMapping("/api/comment")

public class CommentController {

    private final CommentService commentService;
    private final JwtUtil jwtUtil;

    public CommentController(CommentService commentService,
                             JwtUtil jwtUtil) {

        this.commentService = commentService;
        this.jwtUtil = jwtUtil;
    }

    /* 댓글 생성 메소드 */
    @PostMapping("/comments")
    public ResponseDTO<CommentResponseDTO> createComment(
            @RequestBody CommentCreateRequestDTO requestDTO
            ) {

        CommentResponseDTO responseDTO =
                commentService.
                        createComment(requestDTO);

        return ResponseDTO.ok(responseDTO);

    }

    /* 댓글 수정 */
    @PatchMapping("/{commentId}")
    public ResponseDTO<CommentResponseDTO> updateComment(
            @PathVariable("commentId") Long commentId,
            @RequestBody CommentUpdateRequestDTO requestDTO,
            @RequestHeader("authorization") String bearerToken
    ) {

        String token = bearerToken.substring(7); // "Bearer " 제거
        Long userId = jwtUtil.getUserIdFromToken(token);    // Claims에서 UserId 추출

        CommentResponseDTO responseDTO =
                commentService.
                        updateComment(
                                commentId,
                                requestDTO,
                                userId);

        return ResponseDTO.ok(responseDTO);

    }
    /* 댓글 삭제 */
    @DeleteMapping("/{commentId}")
    public ResponseDTO<String> deleteComment(
            @PathVariable("commentId") Long commentId,
            @RequestHeader("Authorization") String bearerToken

    ) {
        String token = bearerToken.substring(7); // "Bearer " 제거
        Long userId = jwtUtil.getUserIdFromToken(token);    // Claims에서 UserId 추출

        commentService.deleteComment(commentId, userId);

        return ResponseDTO.ok("댓글이 삭제되었습니다.");
    }

    /* 게시글 조회 시 댓글 리스트를 모두 조회하는 메소드 */
    @GetMapping("/{postId}")
    public ResponseDTO<List<CommentResponseDTO>> getComments(
            @PathVariable Long postId
    ) {

        List<CommentResponseDTO> responseDTO =
                commentService.getComments(postId);

        return ResponseDTO.ok(responseDTO);

    }
}
