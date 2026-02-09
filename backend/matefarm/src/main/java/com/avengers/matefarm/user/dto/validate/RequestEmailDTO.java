package com.avengers.matefarm.user.dto.validate;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class RequestEmailDTO {
    @JsonProperty("email")
    private String email;
}
