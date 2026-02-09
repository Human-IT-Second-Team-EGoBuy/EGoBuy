package com.avengers.matefarm.user.dto.validate;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class RequestLoginIdDTO {
    @JsonProperty("user_auth_id")
    private String UserAuthId;
}
