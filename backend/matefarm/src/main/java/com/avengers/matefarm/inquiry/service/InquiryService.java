package com.avengers.matefarm.inquiry.service;

import com.avengers.matefarm.inquiry.dto.request.InquiryRequestDTO;
import com.avengers.matefarm.inquiry.dto.response.InquiryResponseDTO;

public interface InquiryService {
    InquiryResponseDTO createInquiry(Long userId, InquiryRequestDTO inquiryRequestDTO);
}
