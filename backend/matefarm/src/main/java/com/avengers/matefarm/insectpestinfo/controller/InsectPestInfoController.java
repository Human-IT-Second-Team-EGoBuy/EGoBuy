package com.avengers.matefarm.insectpestinfo.controller;

import com.avengers.matefarm.common.ResponseDTO;
import com.avengers.matefarm.insectpestinfo.dto.CropCategoryDto;
import com.avengers.matefarm.insectpestinfo.dto.PagedResponse;
import com.avengers.matefarm.insectpestinfo.dto.PestIssueRowDto;
import com.avengers.matefarm.insectpestinfo.service.InsectPestInfoService;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;

import java.util.List;

// 정보허브(Information Hub) - 병해/해충 목록 조회 API 컨트롤러
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/information-hub")
public class InsectPestInfoController {

    private final InsectPestInfoService service;

    // [GET] /api/information-hub/crop-categories, 작물 카테고리 목록을 조회
    @GetMapping("/crop-categories")
    public ResponseDTO<List<CropCategoryDto>> cropCategories() {
        return ResponseDTO.ok(service.getCategories());
    }

    // [GET] /api/information-hub/pest-issues, 병해/해충 목록을 조회(필터/검색/페이징)
    @GetMapping("/pest-issues")
    public ResponseDTO<PagedResponse<PestIssueRowDto>> pestIssues(
            @RequestParam(name = "categoryId", required = false) Long categoryId,
            @RequestParam(name = "ptype", required = false) String ptype,
            @RequestParam(name = "q", required = false) String q,
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "size", defaultValue = "10") int size
    ) {
        /**
         * 프론트에서 ptype="all" 또는 빈 값으로 보내는 경우
         * → 필터를 적용하지 않는 것으로 처리하고 싶다.
         * → service 쪽에는 null을 넘겨서 "조건 없음"으로 쿼리 구성하기 쉽게 만든다.
         */
        String normPtype = (ptype == null || ptype.isBlank() || "all".equalsIgnoreCase(ptype))
                ? null : ptype;

        // service.getPestIssues(...) : 필터/검색/페이징을 적용한 목록 조회
        return ResponseDTO.ok(service.getPestIssues(categoryId, normPtype, q, page, size));
    }
}
