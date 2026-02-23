package com.avengers.matefarm.insectpestinfo.dto;
import com.fasterxml.jackson.annotation.JsonProperty;

public record CropCategoryDto(
    @JsonProperty("category_id") Long categoryId,
    @JsonProperty("category_name") String categoryName
) {}