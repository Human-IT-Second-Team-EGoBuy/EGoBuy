package com.avengers.matefarm.diagnosis.dto;

import java.util.List;

public record VisionDiagnosisResponse(
        Long cropId,
        String modelKey,
        BestResponse best,
        List<BestResponse> topK,
        MetaResponse meta,
        String ragAnswer 
) {}
