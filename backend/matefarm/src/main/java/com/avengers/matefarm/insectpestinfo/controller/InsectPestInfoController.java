package com.avengers.matefarm.insectpestinfo.controller;

import com.avengers.matefarm.common.ResponseDTO;
import com.avengers.matefarm.insectpestinfo.dto.CropCategoryDto;
import com.avengers.matefarm.insectpestinfo.dto.PagedResponse;
import com.avengers.matefarm.insectpestinfo.dto.PestIssueRowDto;
import com.avengers.matefarm.insectpestinfo.service.InsectPestInfoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/information-hub")
public class InsectPestInfoController {

    private final InsectPestInfoService service;

    public InsectPestInfoController(InsectPestInfoService service) {
        this.service = service;
    }

    @GetMapping("/crop-categories")
    public ResponseEntity<ResponseDTO<List<CropCategoryDto>>> cropCategories() {
        var body = ResponseDTO.ok(service.getCategories());
        return ResponseEntity.status(body.getHttpStatus()).body(body);
    }

    @GetMapping("/pest-issues")
    public ResponseDTO<PagedResponse<PestIssueRowDto>> pestIssues(
            @RequestParam(name = "categoryId", required = false) Long categoryId,
            @RequestParam(name = "ptype", required = false) String ptype,
            @RequestParam(name = "q", required = false) String q,
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "size", defaultValue = "10") int size
    ) {
        String normPtype = (ptype == null || ptype.isBlank() || "all".equalsIgnoreCase(ptype))
                ? null : ptype;

        return ResponseDTO.ok(service.getPestIssues(categoryId, normPtype, q, page, size));
    }
}
