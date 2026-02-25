package com.avengers.matefarm.insectpestinfo.dto;

public record DiseaseDetailResponse(
        DiseaseBaseDto base,
        DiseaseDetailDto detail
) {}