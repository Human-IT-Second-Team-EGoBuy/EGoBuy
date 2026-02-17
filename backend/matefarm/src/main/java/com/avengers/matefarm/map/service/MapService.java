package com.avengers.matefarm.map.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.avengers.matefarm.map.dto.response.MapRegionResponseDTO;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class MapService {

    private final SoilService soilService;
    private final LandPriceService landPriceService;
    private final RegionService regionService;

    /**
     * 토양 + boundary + centerAddress
     */
    @Transactional
    public Map<String, Object> regCodeSelect(String locatadd_nm, boolean hasRi) {
        return soilService.getSoilAndBoundaryInfo(locatadd_nm, hasRi);
    }

    /**
     * 토지 매매가 + boundary + centerAddress
     */
    @Transactional
    public Mono<Map<String, Object>> lawdCodeSelect(String locatadd_nm, boolean hasRi) {
        return landPriceService.getTradeHistory(locatadd_nm, hasRi);
    }

    /**
     * 시/도 open
     */
    @Transactional
    public List<MapRegionResponseDTO> sidoOpen() {
        return regionService.getSidoList();
    }

    /**
     * 하위 지역 open
     */
    @Transactional(readOnly = true)
    public List<MapRegionResponseDTO> childOpen(String parentRegionCd) {
        return regionService.getChildRegions(parentRegionCd);
    }
}
