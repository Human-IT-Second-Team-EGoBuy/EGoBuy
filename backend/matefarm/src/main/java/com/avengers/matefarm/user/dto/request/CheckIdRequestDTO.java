package com.avengers.matefarm.user.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class CheckIdRequestDTO {
    @JsonProperty("email")
    private String email;
}
