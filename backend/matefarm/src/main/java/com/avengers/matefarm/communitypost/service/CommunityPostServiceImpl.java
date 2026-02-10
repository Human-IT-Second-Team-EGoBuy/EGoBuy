package com.avengers.matefarm.communitypost.service;

import com.avengers.matefarm.common.exception.CommonException;
import com.avengers.matefarm.common.exception.ErrorCode;
import com.avengers.matefarm.communitypost.dto.CommunityPostEntity;
import com.avengers.matefarm.communitypost.dto.request.CommunityPostUploadRequestDTO;
import com.avengers.matefarm.communitypost.dto.response.CommunityPostDetailedResponseDTO;
import com.avengers.matefarm.communitypost.repository.CommunityPostRepository;
import com.avengers.matefarm.user.dto.UserEntity;
import com.avengers.matefarm.user.service.UserService;
import org.springframework.stereotype.Service;

/*
 *      CommunityPostService 참조 방향 : CommunityPostService -> UserService
 *
* */

@Service
public class CommunityPostServiceImpl implements CommunityPostService {


    private final CommunityPostRepository communityPostRepository;
    private final UserService userService;

    public CommunityPostServiceImpl(CommunityPostRepository communityPostRepository, UserService userService) {
        this.communityPostRepository = communityPostRepository;
        this.userService = userService;
    }

    /* 게시글 생성 */
    @Override
    public CommunityPostDetailedResponseDTO createCommunityPost(CommunityPostUploadRequestDTO requestDTO) {

        // 생성 전, 유저 조회
        UserEntity writer = userService.findUserById(requestDTO.getWriterId());

        CommunityPostEntity communityPostEntity = CommunityPostEntity.builder()
                .communityPostTitle(requestDTO.getCommunityPostTitle())
                .communityPostContent(requestDTO.getCommunityPostContent())
                .postType(requestDTO.getPostType())
                .filesTf(requestDTO.getFilesTf())
                .writerId(writer)
                .build();

        CommunityPostEntity savedEntity = communityPostRepository.save(communityPostEntity);

        return CommunityPostDetailedResponseDTO.from(savedEntity);
    }

    /* 게시글 단건 조회 */
    @Override
    public CommunityPostDetailedResponseDTO getDetailedPost(Long communityPostId) {

        CommunityPostEntity communityPostEntity = communityPostRepository.findById(communityPostId)
                .orElseThrow(() -> new CommonException(ErrorCode.NOT_FOUND_POST));

        return CommunityPostDetailedResponseDTO.from(communityPostEntity);
    }
}
