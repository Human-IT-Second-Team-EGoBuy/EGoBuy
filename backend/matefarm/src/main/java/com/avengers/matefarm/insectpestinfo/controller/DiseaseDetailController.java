package com.avengers.matefarm.insectpestinfo.controller;

import com.avengers.matefarm.common.ResponseDTO;
import com.avengers.matefarm.insectpestinfo.dto.DiseaseDetailResponse;
import com.avengers.matefarm.insectpestinfo.service.DiseaseDetailService;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/information-hub/diseases")
public class DiseaseDetailController {

    private final DiseaseDetailService service;

    @GetMapping("/{diseaseId}")
    public ResponseDTO<DiseaseDetailResponse> get(@PathVariable("diseaseId") Long diseaseId) {
            return ResponseDTO.ok(service.get(diseaseId));
    }
}