package com.avengers.matefarm.inquiry.controller;

import com.avengers.matefarm.common.PageResponseDTO;
import com.avengers.matefarm.common.ResponseDTO;
import com.avengers.matefarm.inquiry.dto.request.InquiryRequestDTO;
import com.avengers.matefarm.inquiry.dto.response.InquiryResponseDTO;
import com.avengers.matefarm.inquiry.service.InquiryService;
import com.avengers.matefarm.notice.dto.response.NoticeResponseDTO;
import com.avengers.matefarm.security.JwtUtil;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController("InquiryController")
@RequestMapping("/api/inquiries")
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

    /* 문의 수정 - InquiryStatus가 Pending인 경우메만 가능 */
    @PatchMapping("/{inquiryId}/update")
    public ResponseDTO<InquiryResponseDTO> updateInquiry(
            @PathVariable("inquiryId") Long inquiryId,
            @RequestBody InquiryRequestDTO inquiryRequestDTO,
            @RequestHeader("Authorization") String bearerToken
    ) {

        // 토큰에서 userId 추출
        String token = bearerToken.substring(7);
        Long userId = jwtUtil.getUserIdFromToken(token);

        InquiryResponseDTO responseDTO =
                inquiryService.
                        updateInquiry(
                                inquiryId,
                                userId,
                                inquiryRequestDTO);

        return ResponseDTO.ok(responseDTO);
    }

    /* 문의 상태 수정 - PROCESSING으로 상태 변경 ( 관리자용 ) */
    @PatchMapping("/{inquiryId}")
    public ResponseDTO<Void> updateInquiryStatus(
            @PathVariable("inquiryId") Long inquiryId,
            @RequestHeader("Authorization") String bearerToken
    ) {

        // 토큰에서 userId 추출
        String token = bearerToken.substring(7);
        Long userId = jwtUtil.getUserIdFromToken(token);

        inquiryService.
                updateInquiryStatusToProcessing(
                        inquiryId,
                        userId);

        return ResponseDTO.ok(null);
    }

    /* 문의 삭제 - Pending인 경우에만 가능. */
    @DeleteMapping("/{inquiryId}")
    public ResponseDTO<Void> deleteInquiry(
            @PathVariable("inquiryId") Long inquiryId,
            @RequestHeader("Authorization") String bearerToken

    ) {
        // 토큰에서 userId 추출
        String token = bearerToken.substring(7);
        Long userId = jwtUtil.getUserIdFromToken(token);

        inquiryService.deleteInquiry(userId, inquiryId);

        return ResponseDTO.ok(null);
    }

    /* 문의 상세 조회 */
    @GetMapping("/{inquiryId}")
    public ResponseDTO<InquiryResponseDTO> getInquiry(
            @PathVariable("inquiryId") Long inquiryId,
            @RequestHeader("Authorization") String bearerToken
    ) {

        // 토큰에서 userId 추출
        String token = bearerToken.substring(7);
        Long userId = jwtUtil.getUserIdFromToken(token);

        InquiryResponseDTO responseDTO =
                inquiryService.getDetailedInquiry(inquiryId, userId);


        return ResponseDTO.ok(responseDTO);
    }

    /* 문의 List 조회 - 사용자용 */
    @GetMapping("/inquiry-list")
    public ResponseDTO<PageResponseDTO<InquiryResponseDTO>> getInquiryList(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            @RequestHeader("Authorization") String bearerToken
    ){

        // 토큰에서 userId 추출
        String token = bearerToken.substring(7);
        Long userId = jwtUtil.getUserIdFromToken(token);

        PageResponseDTO<InquiryResponseDTO> pages =
                inquiryService.
                        getInquiryList(
                                pageable,
                                userId);

        return ResponseDTO.ok(pages);
    }

    /* 문의 List 조회 - 관리자용 */
    @GetMapping("/inquiries-admin")
    public ResponseDTO<PageResponseDTO<InquiryResponseDTO>> getInquiryListForAdmin(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            @RequestHeader("Authorization") String bearerToken
    ) {

        // 토큰에서 userId 추출
        String token = bearerToken.substring(7);
        Long userId = jwtUtil.getUserIdFromToken(token);

        PageResponseDTO<InquiryResponseDTO> responseDTO =
                inquiryService.
                        getInquiryListForAdmin(
                                userId,
                                pageable);

        return ResponseDTO.ok(responseDTO);
    }
}


