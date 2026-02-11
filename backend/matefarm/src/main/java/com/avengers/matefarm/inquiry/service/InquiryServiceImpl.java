package com.avengers.matefarm.inquiry.service;

import com.avengers.matefarm.inquiry.dto.InquiryEntity;
import com.avengers.matefarm.inquiry.dto.request.InquiryRequestDTO;
import com.avengers.matefarm.inquiry.dto.response.InquiryResponseDTO;
import com.avengers.matefarm.inquiry.enums.InquiryStatus;
import com.avengers.matefarm.inquiry.repository.InquiryRepository;
import com.avengers.matefarm.user.dto.UserEntity;
import com.avengers.matefarm.user.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/*
 *      참초 방향 : InquiryService -> UserService
* */
@Service
public class InquiryServiceImpl implements InquiryService{

    private final UserService userService;
    private final InquiryRepository inquiryRepository;

    public InquiryServiceImpl(UserService userService,
                              InquiryRepository inquiryRepository) {

        this.userService = userService;
        this.inquiryRepository = inquiryRepository;
    }

    /* 문의 생성 */
    @Override
    @Transactional
    public InquiryResponseDTO createInquiry(Long userId, InquiryRequestDTO inquiryRequestDTO) {

        // 유저 유효성 검증.
        UserEntity userEntity = userService.findUserById(userId);

        // 문의 생성
        InquiryEntity inquiryEntity =
                InquiryEntity.builder()
                        .inquiryTitle(inquiryRequestDTO.getInquiryTitle())
                        .inquiryContent(inquiryRequestDTO.getInquiryContent())
                        .inquiryType(inquiryRequestDTO.getInquiryType())
                        .inquiryStatus(InquiryStatus.PENDING)
                        .writerId(userEntity)
                        .createdAt(LocalDateTime.now().withNano(0))
                        .build();

        inquiryRepository.save(inquiryEntity);

        return InquiryResponseDTO.from(inquiryEntity);
    }
}
