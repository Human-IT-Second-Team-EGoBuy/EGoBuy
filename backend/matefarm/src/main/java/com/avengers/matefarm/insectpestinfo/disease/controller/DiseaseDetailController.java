package com.avengers.matefarm.insectpestinfo.disease.controller;

import com.avengers.matefarm.insectpestinfo.common.ApiResponse;
import com.avengers.matefarm.insectpestinfo.disease.dto.DiseaseDetailResponse;
import com.avengers.matefarm.insectpestinfo.disease.service.DiseaseDetailService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/information-hub/diseases")
public class DiseaseDetailController {

    private final DiseaseDetailService service;

    @GetMapping("/{diseaseId}")
    public ApiResponse<DiseaseDetailResponse> get(@PathVariable("diseaseId") Long diseaseId) {
        try {
            return ApiResponse.success(service.get(diseaseId));
        } catch (IllegalArgumentException e) {
            if ("NF".equals(e.getMessage())) return ApiResponse.notFound("Disease not found");
            return ApiResponse.error("Error");
        } catch (Exception e) {
            return ApiResponse.error("Error");
        }
    }
}