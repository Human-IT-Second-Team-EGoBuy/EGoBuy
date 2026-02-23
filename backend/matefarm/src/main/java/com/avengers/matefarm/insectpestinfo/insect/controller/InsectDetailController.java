package com.avengers.matefarm.insectpestinfo.insect.controller;

import com.avengers.matefarm.insectpestinfo.common.ApiResponse;
import com.avengers.matefarm.insectpestinfo.insect.dto.InsectDetailResponse;
import com.avengers.matefarm.insectpestinfo.insect.service.InsectDetailService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/information-hub/insects")
public class InsectDetailController {

    private final InsectDetailService service;

    @GetMapping("/{insectId}")
    public ApiResponse<InsectDetailResponse> get(@PathVariable("insectId") Long insectId) {
        try {
            return ApiResponse.success(service.get(insectId));
        } catch (IllegalArgumentException e) {
            if ("NF".equals(e.getMessage())) return ApiResponse.notFound("Insect not found");
            return ApiResponse.error("Error");
        } catch (Exception e) {
            return ApiResponse.error("Error");
        }
    }
}