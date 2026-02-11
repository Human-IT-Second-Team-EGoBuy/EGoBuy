package com.avengers.matefarm.communitypost.dto.request;

import com.avengers.matefarm.communitypost.enums.PostType;
import com.avengers.matefarm.notice.enums.FileExist;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class CommunityPostUploadRequestDTO {

    @JsonProperty("community_post_title")
    private String communityPostTitle;

    @JsonProperty("community_post_content")
    private String communityPostContent;

    @JsonProperty("post_type")
    private PostType postType;

    @JsonProperty("files_tf")
    private FileExist filesTf;

    @JsonProperty("writer_id")
    private Long writerId;
}
