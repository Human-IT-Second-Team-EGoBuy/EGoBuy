package com.avengers.matefarm.user.dto.oauth2.kakao;

import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class KakaoUserProfile {

    private String id;          // 네이버 고유 id
    private String name;        // Naver 로그인 유저의 실명.
    private String nickname;
    private String email;       // Naver 회원이 계정에 등록한 Email 주소
    private String mobile;      // Naver 회원이 계정에 등록한 휴대전화번호
}
