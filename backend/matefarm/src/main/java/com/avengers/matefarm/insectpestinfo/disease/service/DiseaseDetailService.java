package com.avengers.matefarm.insectpestinfo.disease.service;

import com.avengers.matefarm.insectpestinfo.disease.dto.*;
import com.avengers.matefarm.insectpestinfo.disease.repository.DiseaseDetailRepository;
import com.avengers.matefarm.insectpestinfo.disease.repository.DiseaseRepository;
import com.avengers.matefarm.insectpestinfo.entity.DiseaseEntity;
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
                .orElseThrow(() -> new IllegalArgumentException("NF"));

        var detail = diseaseDetailRepository.findById(diseaseId).orElse(null);

        return new DiseaseDetailResponse(
                DiseaseBaseDto.from(base),
                detail == null ? null : DiseaseDetailDto.from(detail)
        );
    }
}