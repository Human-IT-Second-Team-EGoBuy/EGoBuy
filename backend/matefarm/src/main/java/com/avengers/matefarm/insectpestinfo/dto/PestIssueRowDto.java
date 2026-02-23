package com.avengers.matefarm.insectpestinfo.dto;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

public record PestIssueRowDto(
        @JsonProperty("pest_type") String pestType,   // "insect" | "disease"
        @JsonProperty("pest_id") Long pestId,
        @JsonProperty("crop_id") Long cropId,
        @JsonProperty("crop_name") String cropName,
        @JsonProperty("pest_name") String pestName,
        @JsonProperty("updated_at") LocalDateTime updatedAt
) {}
