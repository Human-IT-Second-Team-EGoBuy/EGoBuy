package com.avengers.matefarm.insectpestinfo.service;

import com.avengers.matefarm.common.exception.CommonException;
import com.avengers.matefarm.common.exception.ErrorCode;
import com.avengers.matefarm.insectpestinfo.dto.DiseaseBaseDto;
import com.avengers.matefarm.insectpestinfo.dto.DiseaseDetailDto;
import com.avengers.matefarm.insectpestinfo.dto.DiseaseDetailResponse;
import com.avengers.matefarm.insectpestinfo.entity.DiseaseEntity;
import com.avengers.matefarm.insectpestinfo.repository.DiseaseDetailRepository;
import com.avengers.matefarm.insectpestinfo.repository.DiseaseRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DiseaseDetailService {

    private final DiseaseRepository diseaseRepository;
    private final DiseaseDetailRepository diseaseDetailRepository;

    @Transactional(readOnly = true)
    public DiseaseDetailResponse get(Long diseaseId) {
        DiseaseEntity base = diseaseRepository.findByIdAndStatus(diseaseId, 1)
                .orElseThrow(() -> new CommonException(ErrorCode.NOT_FOUND_POST));

        var detail = diseaseDetailRepository.findById(diseaseId).orElse(null);

        return new DiseaseDetailResponse(
                DiseaseBaseDto.from(base),
                detail == null ? null : DiseaseDetailDto.from(detail)
        );
    }
}