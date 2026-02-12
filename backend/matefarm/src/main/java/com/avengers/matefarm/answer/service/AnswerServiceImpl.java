package com.avengers.matefarm.answer.service;

import com.avengers.matefarm.answer.dto.request.AnswerRequestDTO;
import com.avengers.matefarm.answer.dto.response.AnswerResponseDTO;
import com.avengers.matefarm.answer.repository.AnswerRepository;
import org.springframework.stereotype.Service;

@Service
public class AnswerServiceImpl implements AnswerService {

    private final AnswerRepository answerRepository;

    public AnswerServiceImpl(AnswerRepository answerRepository) {
        this.answerRepository = answerRepository;
    }

    @Override
    public AnswerResponseDTO createAnswer(Long inquiryId, AnswerRequestDTO answerRequestDTO, Long userId) {

        // 관리자 검증

        // 문의 조회

        // 답변 생성

        
        return null;
    }
}
