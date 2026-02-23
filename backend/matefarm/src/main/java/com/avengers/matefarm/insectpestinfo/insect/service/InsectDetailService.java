package com.avengers.matefarm.insectpestinfo.insect.service;

import com.avengers.matefarm.insectpestinfo.entity.InsectEntity;
import com.avengers.matefarm.insectpestinfo.insect.dto.*;
import com.avengers.matefarm.insectpestinfo.insect.repository.InsectDetailRepository;
import com.avengers.matefarm.insectpestinfo.insect.repository.InsectRepository;
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
                .orElseThrow(() -> new IllegalArgumentException("NF"));

        var detail = insectDetailRepository.findById(insectId).orElse(null);

        return new InsectDetailResponse(
                InsectBaseDto.from(base),
                detail == null ? null : InsectDetailDto.from(detail)
        );
    }
}