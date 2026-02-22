package com.avengers.matefarm.comment.repository;


import com.avengers.matefarm.comment.dto.CommentEntity;
import com.avengers.matefarm.communitypost.dto.CommunityPostEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface CommentRepository extends JpaRepository<CommentEntity, Long> {

    // PostId를 기준으로 모든 Comment를 List로 반환하는 메소드 | CommentEntity에 PostId는 CommunityPostEntity 타입의 객체이므로 Long이 아닌 객체의 타입을 명시
    List<CommentEntity> findAllByPostIdAndParentIdIsNull(CommunityPostEntity postId);
}
