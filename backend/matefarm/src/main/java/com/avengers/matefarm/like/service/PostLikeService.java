package com.avengers.matefarm.like.service;

import com.avengers.matefarm.like.dto.response.PostLikeResponseDTO;

public interface PostLikeService {
    PostLikeResponseDTO postLike(Long userId, Long postId);
}
