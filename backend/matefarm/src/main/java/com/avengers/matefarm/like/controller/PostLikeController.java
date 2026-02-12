package com.avengers.matefarm.like.controller;

import com.avengers.matefarm.common.ResponseDTO;
import com.avengers.matefarm.like.dto.response.PostLikeResponseDTO;
import com.avengers.matefarm.like.service.PostLikeService;
import com.avengers.matefarm.security.JwtUtil;
import org.springframework.web.bind.annotation.*;

@RestController("LikeController")
@RequestMapping("/api/likes")
public class PostLikeController {

    private final PostLikeService PostLikeService;
    private final JwtUtil jwtUtil;

    public PostLikeController(PostLikeService PostLikeService,
                              JwtUtil jwtUtil) {

        this.PostLikeService = PostLikeService;
        this.jwtUtil = jwtUtil;
    }

    /* 게시글 좋아요 생성/삭제 */
    @PostMapping("/{postId}")
    public ResponseDTO<PostLikeResponseDTO> postLike(
            @PathVariable("postId") Long postId,
            @RequestHeader("Authorization") String bearerToken) {

        // 토큰에서 userId 추출
        String token = bearerToken.substring(7);
        Long userId = jwtUtil.getUserIdFromToken(token);

        PostLikeResponseDTO responseDTO =
                PostLikeService.
                        postLike(postId,userId);
        return ResponseDTO.ok(responseDTO);
    }
}
