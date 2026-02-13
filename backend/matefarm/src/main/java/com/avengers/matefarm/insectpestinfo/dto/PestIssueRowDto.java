package com.avengers.matefarm.insectpestinfo.dto;

import java.time.LocalDateTime;

public record PestIssueRowDto(
        String pest_type,   // "insect" | "disease"
        Long pest_id,
        Long crop_id,
        String crop_name,
        String pest_name,
        LocalDateTime updated_at
) {}
