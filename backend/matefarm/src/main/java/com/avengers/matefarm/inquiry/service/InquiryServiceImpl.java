package com.avengers.matefarm.inquiry.service;

import com.avengers.matefarm.common.PageResponseDTO;
import com.avengers.matefarm.common.exception.CommonException;
import com.avengers.matefarm.common.exception.ErrorCode;
import com.avengers.matefarm.inquiry.dto.InquiryEntity;
import com.avengers.matefarm.inquiry.dto.request.InquiryRequestDTO;
import com.avengers.matefarm.inquiry.dto.response.InquiryResponseDTO;
import com.avengers.matefarm.inquiry.enums.InquiryStatus;
import com.avengers.matefarm.inquiry.repository.InquiryRepository;
import com.avengers.matefarm.user.dto.UserEntity;
import com.avengers.matefarm.user.dto.enums.UserRole;
import com.avengers.matefarm.user.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/*
 *      참초 방향 : InquiryService -> UserService
* */
@Service
public class InquiryServiceImpl implements InquiryService{

    private final Integer PAGE_SIZE = 10;

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

    /* 문의 상태 변경 메소드 ( Pending -> Processing ) */
    @Override
    @Transactional
    public void updateInquiryStatusToProcessing(Long inquiryId, Long userId) {
        
        // Admin 확인
        UserEntity userEntity = userService.findUserById(userId);

        if (!userEntity.isAdmin()) {
            throw new CommonException(ErrorCode.ACCESS_DENIED);
        }

        // 문의 게시글 유효성 검사
        InquiryEntity inquiryEntity = inquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new CommonException(ErrorCode.NOT_FOUND_INQUIRY));

        inquiryEntity.ChangeStatusToProcessing();
    }

    /* 문의 삭제 메소드 */
    @Override
    @Transactional
    public void deleteInquiry(Long userId, Long inquiryId) {
        // user, inquiry 체크
        UserEntity userEntity = userService.findUserById(userId);

        InquiryEntity inquiryEntity = inquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new CommonException(ErrorCode.NOT_FOUND_INQUIRY));

        // 1. 문의를 한 회원인지 확인
        if (!inquiryEntity.getWriterId().getUserId().equals(userId)) {
            throw new CommonException(ErrorCode.ACCESS_DENIED);
        }

        // 2. Pending 이 아닌 경우 에러 발생
        if (!(inquiryEntity.getInquiryStatus() == InquiryStatus.PENDING)) {
            throw new CommonException(ErrorCode.INQUIRY_STATUS_NOT_PENDING);
        }

        inquiryRepository.delete(inquiryEntity);
    }

    /* 문의 상세 조회 */
    @Override
    @Transactional(readOnly = true)
    public InquiryResponseDTO getDetailedInquiry(Long inquiryId, Long userId) {

        // 문의 게시글 유효성 검증
        InquiryEntity inquiryEntity = inquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new CommonException(ErrorCode.NOT_FOUND_INQUIRY));


        // 사용자 검증
        if(!inquiryEntity.getWriterId().getUserId().equals(userId)) {
            throw new CommonException(ErrorCode.ACCESS_DENIED);
        }

        return InquiryResponseDTO.from(inquiryEntity);
    }

    /* 문의사항 List 조회 - 사용자용 */
    @Override
    @Transactional(readOnly = true)
    public PageResponseDTO<InquiryResponseDTO> getInquiryList(Pageable pageable, Long userId) {

        // 특정 유저의 문의사항만 조회하기 위해 토큰에서 받은 UserId로 UserEntity 객체 조회
        UserEntity userEntity = userService.findUserById(userId);

        // Data 조회, Page 타입 반환
        Page<InquiryEntity> inquiries = inquiryRepository.findAllByWriterId(userEntity,pageable);

        // Entity To DTO 변환
        List<InquiryResponseDTO> inquiryResponseDTOs = inquiries.getContent().stream()
                .map(InquiryResponseDTO::from)
                .toList();


         // 한 페이지당 보여줄 게시글의 수(getSize())와 1~10, 11~20처럼 페이지 번호를 보여줄 갯수(PAGE_SIZE)를 혼동하지 말 것.
        return new PageResponseDTO<>(
                inquiryResponseDTOs,
                inquiries.getNumber() + 1,    // 0번부터 시작이므로 + 1
                PAGE_SIZE,                         //  보여줄 페이지 번호의 갯수. ( DTO에서 내부 로직에  사용하기 위함 )
                // 아래 두 함수는 int, long을 각각 반환하지만 객체 생성 당시 Auto Boxing으로 인해 Integer로 자동 캐스팅 된다.
                inquiries.getSize(),                 // 페이지당 요소의 수 ( Controller에서 정의한 Size : ) Pageable 타입의 객체가 생성될 당시 size : 10 정보를 갖고 있어 getSize() 할 시 한 페이지당 보여줄 요소의 수, 10을 반환함
                (int)inquiries.getTotalElements()    // 토탈 요소의 수;
        );
    }

    /* 문의사항 List 조회 - 관리자용 */
    @Override
    @Transactional(readOnly = true)
    public PageResponseDTO<InquiryResponseDTO> getInquiryListForAdmin(Long userId, Pageable pageable) {

        // 조회를 하는 유저가 관리자인지 확인
        UserEntity userEntity = userService.findUserById(userId);

        if (!userEntity.isAdmin()) {
            throw new CommonException(ErrorCode.ACCESS_DENIED);
        }

        // Data 조회, Page 타입 반환
        Page<InquiryEntity> inquiries = inquiryRepository.findAll(pageable);

        // Entity To DTO 변환
        List<InquiryResponseDTO> inquiryResponseDTOs = inquiries.getContent().stream()
                .map(InquiryResponseDTO::from)
                .toList();

        // 한 페이지당 보여줄 게시글의 수(getSize())와 1~10, 11~20처럼 페이지 번호를 보여줄 갯수(PAGE_SIZE)를 혼동하지 말 것.
        return new PageResponseDTO<>(
                inquiryResponseDTOs,
                inquiries.getNumber() + 1,    // 0번부터 시작이므로 + 1
                PAGE_SIZE,                         //  보여줄 페이지 번호의 갯수. ( DTO에서 내부 로직에  사용하기 위함 )
                // 아래 두 함수는 int, long을 각각 반환하지만 객체 생성 당시 Auto Boxing으로 인해 Integer로 자동 캐스팅 된다.
                inquiries.getSize(),                 // 페이지당 요소의 수 ( Controller에서 정의한 Size : ) Pageable 타입의 객체가 생성될 당시 size : 10 정보를 갖고 있어 getSize() 할 시 한 페이지당 보여줄 요소의 수, 10을 반환함
                (int) inquiries.getTotalElements()    // 토탈 요소의 수;
        );
    }

    /* 문의 수정 ( 사용자용 ) */
    @Override
    @Transactional
    public InquiryResponseDTO updateInquiry(Long inquiryId, Long userId, InquiryRequestDTO inquiryRequestDTO) {

        // 문의 존재 여부 확인
        InquiryEntity inquiryEntity = inquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new CommonException(ErrorCode.NOT_FOUND_INQUIRY));

        // 문의 상태가 Pending인지 확인
        if (inquiryEntity.getInquiryStatus() != InquiryStatus.PENDING) {
            throw new CommonException(ErrorCode.INQUIRY_STATUS_NOT_PENDING);
        }

        // 사용자 본인의 문의인지 확인
        if(!inquiryEntity.getWriterId().getUserId().equals(userId)) {
            throw new CommonException(ErrorCode.ACCESS_DENIED);
        }

        // 수정
        inquiryEntity.updateInquiry(
                inquiryRequestDTO.getInquiryTitle(),
                inquiryRequestDTO.getInquiryContent(),
                inquiryRequestDTO.getInquiryType()
        );

//        inquiryRepository.save(inquiryEntity); // Transaction 종료 시점에 자동 반영

        return InquiryResponseDTO.from(inquiryEntity);
    }


    /* Answer에서 사용할 목적으로 만든 Service 계층에만 존재하는 메소드 */
    @Override
    public InquiryEntity findInquiryByInquiryId(Long inquiryId) {
        return inquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new CommonException(ErrorCode.NOT_FOUND_INQUIRY));
    }
}
