package com.avengers.matefarm.user.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class RequestResetPasswordDTO {
//    @JsonProperty("email")
//    private String email;

    @JsonProperty("new_password")
    private String newPassword;

    @JsonProperty("confirm_password")
    private String confirmPassword;

    @JsonProperty("resetToken")
    private String resetToken;
}
