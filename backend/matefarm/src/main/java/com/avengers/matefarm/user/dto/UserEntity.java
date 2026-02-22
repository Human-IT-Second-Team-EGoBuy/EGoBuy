package com.avengers.matefarm.user.dto;

import com.avengers.matefarm.user.dto.enums.ActiveStatus;
import com.avengers.matefarm.user.dto.enums.SignupPath;
import com.avengers.matefarm.user.dto.enums.UserRole;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "users")
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "user_auth_id", nullable = false, length = 255)
    private String userAuthId; // 신규 추가, 일반 로그인 ID 또는 소셜 로그인 고유번호

    @Column(name = "user_name", nullable = false, length = 255)
    private String userName;

    @Column(name = "user_password", nullable = false, length = 255)
    private String encryptedPwd;

    @Column(name = "nickname", nullable = false, length = 20)
    private String nickname;

    @Column(name = "email", nullable = false, length = 255)
    private String email;

    @Column(name = "phone_number", length = 255)
    private String phoneNumber;

    @Column(name = "road_name_address", length = 255)
    private String roadNameAddress;

    @Column(name = "detailed_address", length = 255)
    private String detailedAddress;

    @Column(name = "postcode", length = 20)
    private String postcode;

    @Enumerated(EnumType.STRING)
    @Column(name = "user_role", nullable = false, length = 255)
    private UserRole userRole;

    @Enumerated(EnumType.STRING)
    @Column(name = "user_status", nullable = false, length = 255)
    private ActiveStatus userStatus = ActiveStatus.ACTIVE;

    @Enumerated(EnumType.STRING)
    @Column(name = "signup_path", nullable = false, length = 255)
    private SignupPath signupPath;

    @Column(name = "user_identifier", nullable = false, unique = true, length = 511)
    private String userIdentifier; // signup_path + user_auth_id 조합으로 생성

    @Column(name = "privacy_agreement_yn", nullable = false)
    private String privacyAgreementYn = "Y";

    @Column(name = "marketing_email_agreement_yn", nullable = false)
    private String marketingEmailAgreementYn = "N";

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;


    public void deleteUser() {
        this.userStatus = ActiveStatus.DELETED;
        // this.deletedAt =  LocalDateTime.now().withNano(0);
    }


    /* 관리자 전용 조회 메서드에 사용하기 위해 추가 */
    public boolean isAdmin() {
        return this.userRole == UserRole.ADMIN;
    }
}
