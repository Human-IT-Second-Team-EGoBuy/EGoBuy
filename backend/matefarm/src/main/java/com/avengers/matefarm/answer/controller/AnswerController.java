package com.avengers.matefarm.answer.controller;

import com.avengers.matefarm.answer.dto.request.AnswerRequestDTO;
import com.avengers.matefarm.answer.dto.response.AnswerResponseDTO;
import com.avengers.matefarm.answer.service.AnswerService;
import com.avengers.matefarm.common.ResponseDTO;
import com.avengers.matefarm.security.JwtUtil;
import org.springframework.web.bind.annotation.*;

@RestController("AnswerController")
@RequestMapping("/api/answer")
public class AnswerController {

    private final AnswerService answerService;
    private final JwtUtil jwtUtil;

    public AnswerController(AnswerService answerService,
                            JwtUtil jwtUtil) {

        this.answerService = answerService;
        this.jwtUtil = jwtUtil;
    }

    /* 답변 생성 ( 관리자용 ) - 답변 완료 시 Completed로 Inquiry 상태 변환. */
    @PostMapping("/{inquiryId}/answer")
    public ResponseDTO<AnswerResponseDTO> createAnswer(
            @PathVariable("inquiryId") Long inquiryId,
            @RequestBody AnswerRequestDTO answerRequestDTO,
            @RequestHeader("authorization") String bearerToken) {

        String token = bearerToken.substring(7);
        Long userId = jwtUtil.getUserIdFromToken(token);

        AnswerResponseDTO responseDTO =
                answerService.
                        createAnswer(
                                inquiryId,
                                answerRequestDTO,
                                userId);

        return ResponseDTO.ok(responseDTO);

    }

    /* 답변 수정 - 문의 상태가 Processing인 경우에만 가능 */


    /* 답변 상세 조회 */
}
