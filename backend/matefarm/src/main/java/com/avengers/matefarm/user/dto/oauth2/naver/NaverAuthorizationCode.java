package com.avengers.matefarm.user.dto.oauth2.naver;

import lombok.Getter;

// naver login 성공 시 Callback Url을 통해 전달받을 값을 정의한 클래스.
@Getter
public class NaverAuthorizationCode {
    private String code;
    private String state;
}
