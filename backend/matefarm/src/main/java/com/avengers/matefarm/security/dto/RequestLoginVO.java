package com.avengers.matefarm.security.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class RequestLoginVO {

    @JsonProperty("user_auth_id")
    private String userAuthId;

    @JsonProperty("password")
    private String password;
}

