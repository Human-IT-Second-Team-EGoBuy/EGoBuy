package com.avengers.matefarm.inquiry.service;

import com.avengers.matefarm.common.PageResponseDTO;
import com.avengers.matefarm.inquiry.dto.InquiryEntity;
import com.avengers.matefarm.inquiry.dto.request.InquiryRequestDTO;
import com.avengers.matefarm.inquiry.dto.response.InquiryResponseDTO;
import org.springframework.data.domain.Pageable;

public interface InquiryService {
    InquiryResponseDTO createInquiry(Long userId, InquiryRequestDTO inquiryRequestDTO);

    void updateInquiryStatusToProcessing(Long inquiryId, Long userId);

    void deleteInquiry(Long userId, Long inquiryId);

    InquiryResponseDTO getDetailedInquiry(Long inquiryId, Long userId);

    PageResponseDTO<InquiryResponseDTO> getInquiryList(Pageable pageable,Long userId);

    PageResponseDTO<InquiryResponseDTO> getInquiryListForAdmin(Long userId, Pageable pageable);

    InquiryResponseDTO updateInquiry(Long inquiryId, Long userId, InquiryRequestDTO inquiryRequestDTO);

    InquiryEntity findInquiryByInquiryId(Long inquiryId);
}
