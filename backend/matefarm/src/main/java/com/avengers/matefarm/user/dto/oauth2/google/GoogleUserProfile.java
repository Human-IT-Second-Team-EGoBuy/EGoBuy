package com.avengers.matefarm.user.dto.oauth2.google;

import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class GoogleUserProfile {

    private String id;
    private String name;
    private String nickname;
    private String email;
}
