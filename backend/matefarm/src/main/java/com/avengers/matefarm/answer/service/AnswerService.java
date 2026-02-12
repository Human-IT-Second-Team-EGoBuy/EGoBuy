package com.avengers.matefarm.answer.service;

import com.avengers.matefarm.answer.dto.request.AnswerRequestDTO;
import com.avengers.matefarm.answer.dto.response.AnswerResponseDTO;

public interface AnswerService {

    AnswerResponseDTO createAnswer(Long inquiryId, AnswerRequestDTO answerRequestDTO, Long userId);
}
