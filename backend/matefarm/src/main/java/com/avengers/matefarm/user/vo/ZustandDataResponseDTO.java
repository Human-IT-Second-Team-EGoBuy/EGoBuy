package com.avengers.matefarm.user.vo;

import com.avengers.matefarm.user.dto.enums.ActiveStatus;
import com.avengers.matefarm.user.dto.enums.UserRole;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Service;

@Builder
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ZustandDataResponseDTO {
    // Zustand를 통해 User 데이터를 전역으로 관리하기 위한 responseDTO

    @JsonProperty("user_id")
    private Long userId;

    @JsonProperty("user_auth_id")
    private String userAuthId;

    @JsonProperty("user_name")
    private String userName;

    @JsonProperty("nickname")
    private String nickname;

    @JsonProperty("user_status")
    private ActiveStatus userStatus;

    @JsonProperty("user_role")
    private UserRole userRole;
}
