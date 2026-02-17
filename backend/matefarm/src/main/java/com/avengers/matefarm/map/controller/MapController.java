package com.avengers.matefarm.map.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.avengers.matefarm.common.ResponseDTO;
import com.avengers.matefarm.map.dto.requests.MapRequestDTO;
import com.avengers.matefarm.map.dto.response.MapRegionResponseDTO;
import com.avengers.matefarm.map.entity.TradeHistoryEntity;
import com.avengers.matefarm.map.service.MapService;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;

@RestController("mapcontroller")
@RequestMapping("/api/map")
@RequiredArgsConstructor
public class MapController {

    private final MapService mapService;

    @GetMapping("/sido-open")
    public ResponseDTO<List<MapRegionResponseDTO>> getSidoData() {
        return ResponseDTO.ok(mapService.sidoOpen());
    }

    @GetMapping("/sgg-open")
    public ResponseDTO<List<MapRegionResponseDTO>> getSggData(@RequestParam String parentRegionCd) {
        return ResponseDTO.ok(mapService.childOpen(parentRegionCd));
    }

    @GetMapping("/umd-open")
    public ResponseDTO<List<MapRegionResponseDTO>> getUmdData(@RequestParam String parentRegionCd) {
        return ResponseDTO.ok(mapService.childOpen(parentRegionCd));
    }

    @GetMapping("/ri-open")
    public ResponseDTO<List<MapRegionResponseDTO>> getRiData(@RequestParam String parentRegionCd) {
        return ResponseDTO.ok(mapService.childOpen(parentRegionCd));
    }

    @GetMapping("/landinfo-search")
    public ResponseDTO<Map<String, Object>> getLandInfo(
            @RequestParam String locatadd_nm,
            @RequestParam(defaultValue = "false") boolean hasRi) {
        return ResponseDTO.ok(mapService.regCodeSelect(locatadd_nm, hasRi));
    }

    @GetMapping("/landprice-search")
    public Mono<ResponseDTO<Map<String, Object>>> getLandPrice(
            @RequestParam String locatadd_nm,
            @RequestParam(defaultValue = "false") boolean hasRi) {
        return mapService.lawdCodeSelect(locatadd_nm, hasRi)
                .map(ResponseDTO::ok);
    }

}
