package com.avengers.matefarm.user.dto.response;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;


@Getter
public class ResponseUserAuthIdDTO {

    @JsonProperty("user_auth_id")
    private String userAuthId; // 사용자 인증 ID, 일반 로그인 시 사용자가 입력한 ID 또는 소셜 로그인 시 고유 ID

    /* 전달인자가 필요한 생성자 명시 */
    public ResponseUserAuthIdDTO(String userAuthId) {
        this.userAuthId = userAuthId;
    }
}
