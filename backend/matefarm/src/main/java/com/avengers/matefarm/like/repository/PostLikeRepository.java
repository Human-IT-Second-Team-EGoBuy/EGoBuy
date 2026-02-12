package com.avengers.matefarm.like.repository;

import com.avengers.matefarm.communitypost.dto.CommunityPostEntity;
import com.avengers.matefarm.like.dto.PostLikeEntity;
import com.avengers.matefarm.user.dto.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PostLikeRepository extends JpaRepository<PostLikeEntity, Long> {

    // 좋아요 조회
    Optional<PostLikeEntity> findByPostIdAndUserId(CommunityPostEntity postId, UserEntity userId);

    // 좋아요 갯수 반환
    long countByPostId(CommunityPostEntity postEntity);
}
