package com.avengers.matefarm.user.dto;

import com.avengers.matefarm.user.dto.enums.ActiveStatus;
import com.avengers.matefarm.user.dto.enums.SignupPath;
import com.avengers.matefarm.user.dto.enums.UserRole;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder // Builder 패턴 적용
@NoArgsConstructor // 기본 생성자
@AllArgsConstructor // 모든 필드를 포함하는 생성자
public class UserDTO {

    @JsonProperty("user_id")
    private Long userId; // 사용자 고유 ID (DB 기본 키)

    @JsonProperty("user_auth_id")
    private String userAuthId; // 사용자 인증 ID, 일반 로그인 시 사용자가 입력한 ID 또는 소셜 로그인 시 고유 ID

    @JsonProperty("password")
    private String password;

    @JsonProperty("user_name")
    private String userName; // 사용자 이름

    @JsonProperty("nickname")
    private String nickname; // 사용자 닉네임

    @JsonProperty("email")
    private String email; // 이메일 (선택 사항)

    @JsonProperty("user_status")
    private ActiveStatus userStatus; // 사용자 상태 (ACTIVE, INACTIVE 등)

    @JsonProperty("created_at")
    private LocalDateTime createdAt; // 생성 날짜

    @JsonProperty("deleted_at")
    private LocalDateTime deletedAt; // 탈퇴 날짜

    @JsonProperty("signup_path")
    private SignupPath signupPath; // 가입 경로 (NORMAL, KAKAO, GOOGLE 등)

    @JsonProperty("user_identifier")
    private String userIdentifier; // 가입 경로 + user_auth_id 결합된 고유 식별자

    @JsonProperty("user_role")
    private UserRole userRole; // 사용자 역할 (관리자, 일반 사용자 등)


}
