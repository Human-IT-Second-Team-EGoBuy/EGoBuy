package com.avengers.matefarm.insectpestinfo.service;

import com.avengers.matefarm.common.exception.CommonException;
import com.avengers.matefarm.common.exception.ErrorCode;
import com.avengers.matefarm.insectpestinfo.dto.InsectBaseDto;
import com.avengers.matefarm.insectpestinfo.dto.InsectDetailDto;
import com.avengers.matefarm.insectpestinfo.dto.InsectDetailResponse;
import com.avengers.matefarm.insectpestinfo.entity.InsectEntity;
import com.avengers.matefarm.insectpestinfo.repository.InsectDetailRepository;
import com.avengers.matefarm.insectpestinfo.repository.InsectRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InsectDetailService {

    private final InsectRepository insectRepository;
    private final InsectDetailRepository insectDetailRepository;

    @Transactional(readOnly = true)
    public InsectDetailResponse get(Long insectId) {
        InsectEntity base = insectRepository.findByIdAndStatus(insectId, 1)
                .orElseThrow(() -> new CommonException(ErrorCode.NOT_FOUND_POST));

        var detail = insectDetailRepository.findById(insectId).orElse(null);

        return new InsectDetailResponse(
                InsectBaseDto.from(base),
                detail == null ? null : InsectDetailDto.from(detail)
        );
    }
}