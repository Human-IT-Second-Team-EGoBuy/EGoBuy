package com.avengers.matefarm.comment.service;

import com.avengers.matefarm.comment.dto.CommentEntity;
import com.avengers.matefarm.comment.dto.request.CommentCreateRequestDTO;
import com.avengers.matefarm.comment.dto.request.CommentUpdateRequestDTO;
import com.avengers.matefarm.comment.dto.response.CommentResponseDTO;
import com.avengers.matefarm.comment.enums.DeleteYN;
import com.avengers.matefarm.comment.repository.CommentRepository;
import com.avengers.matefarm.common.exception.CommonException;
import com.avengers.matefarm.common.exception.ErrorCode;
import com.avengers.matefarm.communitypost.dto.CommunityPostEntity;
import com.avengers.matefarm.communitypost.service.CommunityPostService;
import com.avengers.matefarm.user.dto.UserEntity;
import com.avengers.matefarm.user.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/*
 *      참조 관계 :  CommentService -> UserService
 *                                 -> CommunityService
 *
 *
* */
@Service
public class CommentServiceImpl implements CommentService {

    private final CommunityPostService communityPostService;
    private final UserService userService;
    private final CommentRepository commentRepository;

    public CommentServiceImpl(CommunityPostService communityPostService,
                              UserService userService,
                              CommentRepository commentRepository) {

        this.communityPostService = communityPostService;
        this.userService = userService;
        this.commentRepository = commentRepository;
    }

    /* 댓글 생성 메소드 */
    @Override
    @Transactional
    public CommentResponseDTO createComment(CommentCreateRequestDTO requestDTO) {

        // 댓글 작성 전 게시글과 작성자 체크
        CommunityPostEntity postEntity = communityPostService.getPostInfo(requestDTO.getPostId());

        UserEntity userEntity = userService.findUserById(requestDTO.getWriterId());

        CommentEntity commentEntity = CommentEntity.builder()
                .commentContent(requestDTO.getComment())
                .writer(userEntity)
                .postId(postEntity)
                .deleteYn(DeleteYN.N)
                .createdAt(LocalDateTime.now().withNano(0))
                .build();

        CommentEntity savedEntity = commentRepository.save(commentEntity);


        return CommentResponseDTO.from(savedEntity);
    }

    /* 댓글 조회 메소드 */
    @Override
    public List<CommentResponseDTO> getComments(Long postId) {

        // 게시글이 유효한지 조회
        communityPostService.getPostInfo(postId);

        // 댓글 반환
        return commentRepository.findAllByPostId(postId);
    }

    /* 댓글 수정 메소드 */
    @Override
    public CommentResponseDTO updateComment(Long commentId, CommentUpdateRequestDTO requestDTO, Long userId) {


        // 1. 수정하려는 댓글이 존재하는지 확인
        CommentEntity commentEntity = commentRepository.findById(commentId)
                .orElseThrow(() -> new CommonException(ErrorCode.NOT_FOUND_COMMENT));

        // 2. 작성자와 동일인인지 확인
        if (!commentEntity.getWriter().getUserId().equals(userId)) {
            throw new CommonException(ErrorCode.ACCESS_DENIED);     // 403 Forbidden
        }

        // 3. 댓글 수정
        commentEntity.updateComment(requestDTO.getNewComment());
        commentRepository.save(commentEntity);

        // 4. 반환
        return CommentResponseDTO.from(commentEntity);
    }

    /* 댓글 삭제 메소드 */
    @Override
    @Transactional
    public void deleteComment(Long commentId, Long userId) {

        // 1. 댓글 확인.
        CommentEntity commentEntity = commentRepository.findById(commentId)
                .orElseThrow( ()-> new CommonException(ErrorCode.NOT_FOUND_COMMENT));

        // 2. 작성자 여부 확인
        if (!commentEntity.getWriter().getUserId().equals(userId)) {
            throw new CommonException(ErrorCode.ACCESS_DENIED);
        }

        // 3. 삭제. ( SoftDelete )
        commentEntity.deleteComment();
        commentRepository.save(commentEntity);
    }


}
