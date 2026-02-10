package com.avengers.matefarm.communitypost.service;

import com.avengers.matefarm.communitypost.dto.CommunityPostEntity;
import com.avengers.matefarm.communitypost.dto.request.CommunityPostUploadRequestDTO;
import com.avengers.matefarm.communitypost.dto.response.CommunityPostDetailedResponseDTO;

public interface CommunityPostService {
    
    CommunityPostDetailedResponseDTO createCommunityPost(CommunityPostUploadRequestDTO requestDTO);

    CommunityPostDetailedResponseDTO getDetailedPost(Long communityPostId);

    /* CommunityPostEntity 타입의 객체가 필요한 도메인에서 사용할 서비스 레이어에만 존재하는 메소드 정의 */
    CommunityPostEntity getPostInfo(Long PostId);
}
