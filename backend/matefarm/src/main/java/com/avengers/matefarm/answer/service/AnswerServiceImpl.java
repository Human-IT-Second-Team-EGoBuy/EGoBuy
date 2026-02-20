package com.avengers.matefarm.answer.service;

import com.avengers.matefarm.answer.dto.AnswerEntity;
import com.avengers.matefarm.answer.dto.request.AnswerRequestDTO;
import com.avengers.matefarm.answer.dto.response.AnswerResponseDTO;
import com.avengers.matefarm.answer.enums.DeleteStatus;
import com.avengers.matefarm.answer.repository.AnswerRepository;
import com.avengers.matefarm.common.exception.CommonException;
import com.avengers.matefarm.common.exception.ErrorCode;
import com.avengers.matefarm.inquiry.dto.InquiryEntity;
import com.avengers.matefarm.inquiry.service.InquiryService;
import com.avengers.matefarm.user.dto.UserEntity;
import com.avengers.matefarm.user.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/*
 *      참조방향 :     AnswerService -> UserService
* */
@Service
public class AnswerServiceImpl implements AnswerService {

    private final AnswerRepository answerRepository;
    private final InquiryService inquiryService;
    private final UserService userService;

    public AnswerServiceImpl(AnswerRepository answerRepository,
                             InquiryService inquiryService,
                             UserService userService) {

        this.answerRepository = answerRepository;
        this.inquiryService = inquiryService;
        this.userService = userService;
    }

    @Override
    @Transactional
    public AnswerResponseDTO createAnswer(Long inquiryId, AnswerRequestDTO answerRequestDTO, Long userId) {

        // 관리자 검증
        UserEntity userEntity = userService.findUserById(userId);

        if (!userEntity.isAdmin()) {
            throw new CommonException(ErrorCode.ACCESS_DENIED);
        }

        // 문의 조회
        InquiryEntity inquiryEntity = inquiryService.findInquiryByInquiryId(inquiryId);

        // 중복 답변 방지를 위해 추가
        if (inquiryEntity.isCompleted()) {
            throw new CommonException(ErrorCode.DUPLICATE_ANSWER);
        }

        // 답변 생성
        AnswerEntity answerEntity = AnswerEntity.builder()
                .answerContent(answerRequestDTO.getAnswerContent())
                .isDeleted(DeleteStatus.N)
                .inquiryId(inquiryEntity)
                .answerUserId(userEntity)
                .createdAt(LocalDateTime.now().withNano(0))
                .build();

        answerRepository.save(answerEntity);

        // 문의 상태 변경
        inquiryEntity.ChangeStatusToCompleted();

        return AnswerResponseDTO.from(answerEntity);
    }
}
