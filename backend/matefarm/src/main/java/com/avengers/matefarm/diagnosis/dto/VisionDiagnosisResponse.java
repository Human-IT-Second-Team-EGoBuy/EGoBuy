package com.avengers.matefarm.diagnosis.dto;

import java.util.List;

public record VisionDiagnosisResponse(
        Integer cropId,
        String modelKey,
        BestResponse best,
        List<BestResponse> topK,
        MetaResponse meta
) {}
