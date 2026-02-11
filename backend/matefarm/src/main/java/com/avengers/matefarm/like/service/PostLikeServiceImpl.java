package com.avengers.matefarm.like.service;

import com.avengers.matefarm.communitypost.dto.CommunityPostEntity;
import com.avengers.matefarm.communitypost.service.CommunityPostService;
import com.avengers.matefarm.like.dto.PostLikeEntity;
import com.avengers.matefarm.like.dto.response.PostLikeResponseDTO;
import com.avengers.matefarm.like.repository.PostLikeRepository;
import com.avengers.matefarm.user.dto.UserEntity;
import com.avengers.matefarm.user.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class PostLikeServiceImpl implements PostLikeService {

    private final PostLikeRepository postLikeRepository;
    private final UserService userService;
    private final CommunityPostService communityPostService;

    public PostLikeServiceImpl(PostLikeRepository postLikeRepository,
                               UserService userService,
                               CommunityPostService communityPostService) {

        this.postLikeRepository = postLikeRepository;
        this.userService = userService;
        this.communityPostService = communityPostService;
    }


    /* 좋아요 or 싫어요 */
    @Override
    @Transactional
    public PostLikeResponseDTO postLike(Long userId, Long postId) {

        // 유저가 존재하는가
        UserEntity userEntity = userService.findUserById(userId);

        // 게시글이 존재하는가
        CommunityPostEntity postEntity = communityPostService.getPostInfo(postId);

        // 좋아요 여부를 확인하기 위해 Optional로 받아서 처리
        Optional<PostLikeEntity> existingLike = postLikeRepository.findByPostIdAndUserId(postEntity, userEntity);   // PostLikeEntity 입장에서 postId와 userId는 각각의 Entity타입이므로 파라미터 값을 UserEntity 등으로 제공.

        boolean isLiked;

        if (existingLike.isPresent()) {
            postLikeRepository.delete(existingLike.get());  // isPresent()로 객체 여부 확인 후 get() 사용
            isLiked = false;

            postEntity.decreaseLikeCount(); // Dirty Checking으로 영속성 컨텍스트에 의해 요청 종료 시점에 변경점 반영
        } else {
            PostLikeEntity postLikeEntity = PostLikeEntity.builder()
                    .postId(postEntity)
                    .userId(userEntity)
                    .createdAt(LocalDateTime.now().withNano(0))
                    .build();

            postLikeRepository.save(postLikeEntity);
            postEntity.increaseLikeCount(); // Dirty Checking
            isLiked = true;
        }

        return PostLikeResponseDTO.builder()
                .postId(postId)
                .likeCount(postLikeRepository.countByPostId(postEntity))    // 게시글의 좋아요 Count
                .isLiked(isLiked)
                .build();
    }
}
