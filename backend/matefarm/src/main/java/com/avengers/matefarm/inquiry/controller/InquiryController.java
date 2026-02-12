package com.avengers.matefarm.inquiry.controller;

import com.avengers.matefarm.common.ResponseDTO;
import com.avengers.matefarm.inquiry.dto.request.InquiryRequestDTO;
import com.avengers.matefarm.inquiry.dto.response.InquiryResponseDTO;
import com.avengers.matefarm.inquiry.service.InquiryService;
import com.avengers.matefarm.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController("InquiryController")
@RequestMapping("/api/inquiries/")
public class InquiryController {

    private final JwtUtil jwtUtil;
    private final InquiryService inquiryService;

    public InquiryController(JwtUtil jwtUtil,
                             InquiryService inquiryService) {

        this.jwtUtil = jwtUtil;
        this.inquiryService = inquiryService;
    }

    /* 문의 생성 */
    @PostMapping("/inquiry")
    public ResponseDTO<InquiryResponseDTO> createInquiry(
            @RequestBody InquiryRequestDTO inquiryRequestDTO,
            @RequestHeader("Authorization") String bearerToken) {

        // 토큰에서 userId 추출
        String token = bearerToken.substring(7);
        Long userId = jwtUtil.getUserIdFromToken(token);

        InquiryResponseDTO responseDTO =
                inquiryService.
                        createInquiry(
                                userId,
                                inquiryRequestDTO);

        return ResponseDTO.ok(responseDTO);


    }

    /* 문의 수정 */

    /* 문의 삭제 */

    /* 문의 상세 조회 */

    /* 문의 List 조회 */
}
