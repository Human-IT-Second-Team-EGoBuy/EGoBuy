package com.avengers.matefarm.user.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class RequestLoginedUserPasswordChangeDTO {

    @JsonProperty("current_password")
    private String CurrentPassword;
    @JsonProperty("new_password")
    private String newPassword;
}
