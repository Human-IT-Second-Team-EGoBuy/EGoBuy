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

    /* 답변 생성 ( 관리자용 ) */
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
}
