package com.avengers.matefarm.map.service;

import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.avengers.matefarm.common.exception.CommonException;
import com.avengers.matefarm.common.exception.ErrorCode;
import com.avengers.matefarm.map.dto.entity.RegCodeEntity;
import com.avengers.matefarm.map.dto.response.MapRegionResponseDTO;
import com.avengers.matefarm.map.repository.MapRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RegionService {

    private final MapRepository mapRepository;

    public List<MapRegionResponseDTO> getSidoList() {
        List<String> codes = Arrays.asList(
                "1100000000", "2600000000", "2700000000", "2800000000",
                "2900000000", "3000000000", "3100000000", "3611000000",
                "4100000000", "4300000000", "4400000000", "4600000000",
                "4700000000", "4800000000", "5000000000", "5100000000",
                "5200000000");

        return mapRepository.findByRegionCdIn(codes).stream()
                .map(r -> new MapRegionResponseDTO(r.getRegionCd(), r.getLocallowNm()))
                .toList();
    }

    public List<MapRegionResponseDTO> getChildRegions(String parentRegionCd) {
        if (parentRegionCd == null || parentRegionCd.isBlank()) {
            throw new CommonException(ErrorCode.MISSING_REQUEST_PARAMETER);
        }

        List<RegCodeEntity> rows;

        if ("4100000000".equals(parentRegionCd)) {
            rows = mapRepository.findCitiesByGyeonggi();
        } else if (parentRegionCd.length() > 4 && parentRegionCd.charAt(4) == '0') {
            rows = mapRepository.findDistrictsByCity(parentRegionCd.substring(0, 4));
            if (rows.isEmpty()) {
                rows = mapRepository.findByLocathighCdOrderByLocatOrderAsc(parentRegionCd);
            }
        } else {
            rows = mapRepository.findByLocathighCdOrderByLocatOrderAsc(parentRegionCd);
        }

        return rows.stream()
                .map(r -> new MapRegionResponseDTO(r.getRegionCd(), r.getLocallowNm()))
                .toList();
    }
}
