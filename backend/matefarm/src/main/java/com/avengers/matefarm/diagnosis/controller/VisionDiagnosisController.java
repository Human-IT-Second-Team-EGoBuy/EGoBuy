package com.avengers.matefarm.diagnosis.controller;

import com.avengers.matefarm.common.ResponseDTO;
import com.avengers.matefarm.diagnosis.dto.VisionDiagnosisResponse;
import com.avengers.matefarm.diagnosis.service.VisionDiagnosisService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/ai-chat/vision")
@RequiredArgsConstructor
public class VisionDiagnosisController {

    private final VisionDiagnosisService visionDiagnosisService;

    @PostMapping(value = "/diagnose", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseDTO<VisionDiagnosisResponse> diagnose(
            @RequestParam("cropId") Long cropId,
            @RequestParam("image") MultipartFile image,
            @RequestParam(value = "topK", required = false) Integer topK
    ) {
        VisionDiagnosisResponse data = visionDiagnosisService.diagnose(cropId, image, topK);
        return ResponseDTO.ok(data);
    }
}
