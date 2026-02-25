package com.avengers.matefarm.insectpestinfo.controller;

import com.avengers.matefarm.common.ResponseDTO;
import com.avengers.matefarm.insectpestinfo.dto.InsectDetailResponse;
import com.avengers.matefarm.insectpestinfo.service.InsectDetailService;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/information-hub/insects")
public class InsectDetailController {

    private final InsectDetailService service;

    @GetMapping("/{insectId}")
    public ResponseDTO<InsectDetailResponse> get(@PathVariable("insectId") Long insectId) {
            return ResponseDTO.ok(service.get(insectId));
    }
}