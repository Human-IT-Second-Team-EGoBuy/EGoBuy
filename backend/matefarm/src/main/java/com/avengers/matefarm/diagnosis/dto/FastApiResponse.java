package com.avengers.matefarm.diagnosis.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record FastApiResponse(
        @JsonProperty("conversation_id") String conversationId,
        @JsonProperty("crop_id") Long cropId,
        @JsonProperty("target_model") String targetModel,
        String decision,
        @JsonProperty("model_result") ModelResult modelResult,
        @JsonProperty("final") FinalSection finalSection,
        Meta meta,
        @JsonProperty("rag_answer") String ragAnswer
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ModelResult(
            String model,
            BestItem best,
            @JsonProperty("topk") List<BestItem> topk,
            MetaInside meta
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record BestItem(
            String label,
            @JsonProperty("label_ko") String labelKo,
            @JsonProperty("prob") Double prob,                 // 조건부 확률
            @JsonProperty("prob_global") Double probGlobal,
            Integer index
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record MetaInside(
            @JsonProperty("latency_ms") Long latencyMs,
            @JsonProperty("crop_id") Long cropId,                 
            @JsonProperty("filtered_by_crop") Boolean filteredByCrop, 
            @JsonProperty("allowed_count") Integer allowedCount
            
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record FinalSection(
            @JsonProperty("target_model") String targetModel,
            @JsonProperty("top1_prob") Double top1Prob,
            Double margin,
            String label,
            @JsonProperty("label_ko") String labelKo,
            Raw raw,
            @JsonProperty("top1_prob_global") Double top1ProbGlobal,
            @JsonProperty("margin_global") Double marginGlobal,
            @JsonProperty("filtered_by_crop") Boolean filteredByCrop,
            @JsonProperty("score_basis") String scoreBasis
    ) {
        @JsonIgnoreProperties(ignoreUnknown = true)
        public record Raw(
                String label,
                @JsonProperty("label_ko") String labelKo,
                @JsonProperty("prob") Double prob,
                @JsonProperty("prob_global") Double probGlobal,
                Integer index
        ) {}
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Meta(
            @JsonProperty("latency_ms_total") Long latencyMsTotal,
            @JsonProperty("latency_ms_llm") Long latencyMsLlm
    ) {}
}
