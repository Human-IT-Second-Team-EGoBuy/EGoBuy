package com.avengers.matefarm.communitypost.controller;

import com.avengers.matefarm.common.ResponseDTO;
import com.avengers.matefarm.communitypost.dto.request.CommunityPostUploadRequestDTO;
import com.avengers.matefarm.communitypost.dto.response.CommunityPostDetailedResponseDTO;
import com.avengers.matefarm.communitypost.service.CommunityPostService;
import org.springframework.web.bind.annotation.*;

@RestController("CommunityPostController")
@RequestMapping("/api/co-post")
public class CommunityPostController {

    private final CommunityPostService communityPostService;

    public CommunityPostController(CommunityPostService communityPostService) {
        this.communityPostService = communityPostService;
    }

    /* 게시글 생성 메소드 */
    @PostMapping("/upload-post")
    public ResponseDTO<CommunityPostDetailedResponseDTO> uploadPost(
            @RequestBody CommunityPostUploadRequestDTO requestDTO
    ) {

        CommunityPostDetailedResponseDTO response =
                communityPostService.
                        createCommunityPost(requestDTO);

        return ResponseDTO.ok(response);
    }

    /* 게시글 조회 메소드 */
    @GetMapping("/{community_post_id}")
    public ResponseDTO<CommunityPostDetailedResponseDTO> getDetailedPost(
            @PathVariable("community_post_id") Long communityPostId
    ) {
        CommunityPostDetailedResponseDTO responseDTO =
                communityPostService.
                        getDetailedPost(communityPostId);

        return ResponseDTO.ok(responseDTO);
    }
}
