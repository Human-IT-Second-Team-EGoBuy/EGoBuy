package com.avengers.matefarm.user.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class RequestCodeVerificationDTO {

    // verification code 인증을 위해 사용될 RequestDTO

    @JsonProperty("email")
    private String email;

    @JsonProperty("verification_code")
    private String verificationCode;

//    private enum verificationType 추후 리펙토링 시 Type에 따른 분기
}
