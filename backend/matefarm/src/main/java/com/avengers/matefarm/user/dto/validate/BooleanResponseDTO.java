package com.avengers.matefarm.user.dto.validate;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class BooleanResponseDTO {

    @JsonProperty("exist")
    private boolean isExist;
}
