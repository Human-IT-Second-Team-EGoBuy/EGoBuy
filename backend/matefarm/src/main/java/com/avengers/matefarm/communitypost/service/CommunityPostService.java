package com.avengers.matefarm.communitypost.service;

import com.avengers.matefarm.communitypost.dto.request.CommunityPostUploadRequestDTO;
import com.avengers.matefarm.communitypost.dto.response.CommunityPostDetailedResponseDTO;

public interface CommunityPostService {
    
    CommunityPostDetailedResponseDTO createCommunityPost(CommunityPostUploadRequestDTO requestDTO);

    CommunityPostDetailedResponseDTO getDetailedPost(Long communityPostId);
}
