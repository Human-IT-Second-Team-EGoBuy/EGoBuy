package com.avengers.matefarm.comment.repository;


import com.avengers.matefarm.comment.dto.CommentEntity;
import com.avengers.matefarm.comment.dto.response.CommentResponseDTO;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface CommentRepository extends JpaRepository<CommentEntity, Long> {

    // PostId를 기준으로 모든 Comment를 List로 반환하는 메소드
    List<CommentResponseDTO> findAllByPostId(Long postId);
}
