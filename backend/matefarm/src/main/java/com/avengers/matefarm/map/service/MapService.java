package com.avengers.matefarm.map.service;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.avengers.matefarm.common.exception.CommonException;
import com.avengers.matefarm.common.exception.ErrorCode;
import com.avengers.matefarm.map.api.LandInfoAPI;
import com.avengers.matefarm.map.api.LandPriceAPI;
import com.avengers.matefarm.map.dto.response.MapRegionResponseDTO;
import com.avengers.matefarm.map.entity.LandInfoEntity;
import com.avengers.matefarm.map.entity.TradeHistoryEntity;
import com.avengers.matefarm.map.entity.RegCodeEntity;
import com.avengers.matefarm.map.repository.LandInfoRepository;
import com.avengers.matefarm.map.repository.TradeHistoryRepository;
import com.avengers.matefarm.map.repository.MapRepository;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

@Service
@RequiredArgsConstructor
public class MapService {

    private final MapRepository mapRepository;
    private final LandInfoAPI landInfoAPI;
    private final LandPriceAPI landPriceAPI;
    private final LandInfoRepository landInfoRepository;
    private final TradeHistoryRepository landTradeHistoryRepository;

    private final XmlMapper xmlMapper = new XmlMapper();

    /**
     * 정책 A:
     * - land_info에 있으면 그대로 반환
     * - 없으면 외부 API 7개 호출
     * - 7개 중 하나라도 최종 실패면 저장하지 않고 에러
     * - 7개 전부 성공이면 저장 후 반환
     */
    @Transactional
    public Map<String, String> regCodeSelect(String locatadd_nm) {
        locatadd_nm = (locatadd_nm == null) ? null : locatadd_nm.trim();
        if (locatadd_nm == null || locatadd_nm.isEmpty()) {
            throw new CommonException(ErrorCode.MISSING_REQUEST_PARAMETER);
        }

        String regionCd = mapRepository.findRegionCdByLocataddNm(locatadd_nm)
                .map(RegCodeEntity::getRegionCd)
                .orElseThrow(() -> new CommonException(ErrorCode.NOT_FOUND_FILES));

        // 1) DB 캐시 hit
        return landInfoRepository.findByRegionCd(regionCd)
                .map(this::toResponseMap)
                // 2) 없으면 외부 API 호출 + 저장
                .orElseGet(() -> fetchSaveAndReturn(regionCd));
    }

    /**
     * 이 메서드는 트랜잭션을 걸지 않는다 (외부 API 호출 때문)
     * 저장만 별도 트랜잭션 메서드로 분리한다.
     */
    private Map<String, String> fetchSaveAndReturn(String regionCd) {

        // 2) 외부 API 호출 (JPA 서비스라 동기 결과가 필요 -> blocking 메서드 사용)
        Map<String, String> apiResult = landInfoAPI.getLandInfoDataBlocking(regionCd);

        // 3) 정책 A 검증 (하나라도 실패면 저장 금지)
        validatePolicyA(apiResult);

        // 4) 엔티티 매핑
        LandInfoEntity entity = new LandInfoEntity();
        entity.setRegionCd(regionCd);
        entity.setExamOmInfo(apiResult.get("examOmInfo"));
        entity.setExamApInfo(apiResult.get("examApInfo"));
        entity.setExamKalInfo(apiResult.get("examKalInfo"));
        entity.setExamPhInfo(apiResult.get("examPhInfo"));
        entity.setExamMgInfo(apiResult.get("examMgInfo"));
        entity.setExamSalInfo(apiResult.get("examSalInfo"));
        entity.setExamCalInfo(apiResult.get("examCalInfo"));

        return saveLandInfoWithConflictHandling(entity);
    }

    /**
     * DB 저장만 트랜잭션
     * - 동시성 UNIQUE 충돌 대비: 재조회 후 반환
     */
    @Transactional
    protected Map<String, String> saveLandInfoWithConflictHandling(LandInfoEntity entity) {
        try {
            LandInfoEntity saved = landInfoRepository.save(entity);
            return toResponseMap(saved);
        } catch (DataIntegrityViolationException e) {
            return landInfoRepository.findByRegionCd(entity.getRegionCd())
                    .map(this::toResponseMap)
                    .orElseThrow(() -> e);
        }
    }

    private void validatePolicyA(Map<String, String> apiResult) {
        if (apiResult == null || apiResult.isEmpty()) {
            throw new CommonException(ErrorCode.INTERNAL_SERVER_ERROR);
        }

        boolean hasError = apiResult.entrySet().stream()
                .anyMatch(entry -> {
                    String v = entry.getValue();
                    return v == null || v.isBlank() || v.startsWith("ERROR:");
                });

        if (hasError) {
            throw new CommonException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    private Map<String, String> toResponseMap(LandInfoEntity e) {
        Map<String, String> m = new LinkedHashMap<>();
        m.put("regionCd", e.getRegionCd());

        m.put("examOmInfo", e.getExamOmInfo());
        m.put("examApInfo", e.getExamApInfo());
        m.put("examKalInfo", e.getExamKalInfo());
        m.put("examPhInfo", e.getExamPhInfo());
        m.put("examMgInfo", e.getExamMgInfo());
        m.put("examSalInfo", e.getExamSalInfo());
        m.put("examCalInfo", e.getExamCalInfo());

        return m;
    }

    @Transactional
    public Mono<List<TradeHistoryEntity>> lawdCodeSelect(String locatadd_nm) {
        locatadd_nm = (locatadd_nm == null) ? null : locatadd_nm.trim();
        if (locatadd_nm == null || locatadd_nm.isEmpty()) {
            throw new CommonException(ErrorCode.MISSING_REQUEST_PARAMETER);
        }

        String fullLawdCd = mapRepository.findLawdCdByLocataddNm(locatadd_nm)
                .map(RegCodeEntity::getLawdCd)
                .orElseThrow(() -> new CommonException(ErrorCode.NOT_FOUND_FILES));

        String shortLawdCd = fullLawdCd.substring(0, 5);
        String startMonth = "202502";

        List<TradeHistoryEntity> cachedData = landTradeHistoryRepository.findByLawdCd(shortLawdCd);
        if (!cachedData.isEmpty()) {
            return Mono.just(cachedData);
        }

        return landPriceAPI.landPriceRequest(fullLawdCd, startMonth, 12)
                .publishOn(Schedulers.boundedElastic())
                .map(xml -> {
                    List<TradeHistoryEntity> parsedList = parseLandPriceXml(xml, shortLawdCd);

                    if (!parsedList.isEmpty()) {
                        try {
                            return landTradeHistoryRepository.saveAll(parsedList);
                        } catch (Exception e) {
                            return landTradeHistoryRepository.findByLawdCd(shortLawdCd);
                        }
                    }
                    return parsedList;
                })

                .timeout(Duration.ofSeconds(45));
    }

    @Transactional(readOnly = true)
    public List<MapRegionResponseDTO> sidoOpen() {
        List<String> regionCodes = Arrays.asList(
                "1100000000", "2600000000", "2700000000", "2800000000",
                "2900000000", "3000000000", "3100000000", "3611000000",
                "4100000000", "4300000000", "4400000000", "4600000000",
                "4700000000", "4800000000", "5000000000", "5100000000",
                "5200000000");

        List<RegCodeEntity> rows = mapRepository.findByRegionCdIn(regionCodes);

        return rows.stream()
                .map(r -> new MapRegionResponseDTO(r.getRegionCd(), r.getLocallowNm()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<MapRegionResponseDTO> childOpen(String parentRegionCd) {
        parentRegionCd = (parentRegionCd == null) ? null : parentRegionCd.trim();
        if (parentRegionCd == null || parentRegionCd.isEmpty()) {
            throw new CommonException(ErrorCode.MISSING_REQUEST_PARAMETER);
        }

        List<RegCodeEntity> rows = mapRepository.findByLocathighCdOrderByLocatOrderAsc(parentRegionCd);

        return rows.stream()
                .map(r -> new MapRegionResponseDTO(r.getRegionCd(), r.getLocallowNm()))
                .toList();
    }

    private List<TradeHistoryEntity> parseLandPriceXml(String xml, String shortLawdCd) {
        try {
            JsonNode root = xmlMapper.readTree(xml.getBytes(StandardCharsets.UTF_8));
            JsonNode responseNode = root.has("response") ? root.path("response") : root;

            JsonNode itemsNode = responseNode.path("body").path("items").path("item");

            List<TradeHistoryEntity> list = new java.util.ArrayList<>();

            if (itemsNode.isArray()) {
                for (JsonNode item : itemsNode) {
                    TradeHistoryEntity e = mapToEntity(item, shortLawdCd);
                    if (e != null) list.add(e);
                }
            } else if (!itemsNode.isMissingNode() && !itemsNode.isNull()) {
                TradeHistoryEntity e = mapToEntity(itemsNode, shortLawdCd);
                if (e != null) list.add(e);
            }

            return list;
        } catch (Exception e) {
            return List.of();
        }
    }

    private TradeHistoryEntity mapToEntity(JsonNode node, String shortLawdCd) {
        TradeHistoryEntity entity = new TradeHistoryEntity();
        entity.setLawdCd(shortLawdCd);

        String amountStr = node.path("dealAmount").asText("");
        amountStr = amountStr.replace(",", "").trim();
        if (amountStr.isEmpty()) {
            // 금액이 없으면 스킵
            return null;
        }

        entity.setDealAmount(Integer.parseInt(amountStr));
        entity.setDealArea(node.path("dealArea").asDouble());

        entity.setDealYear(node.path("dealYear").asInt());
        entity.setDealMonth(node.path("dealMonth").asInt());
        entity.setDealDay(node.path("dealDay").asInt());
        entity.setDealYmd(String.format("%d%02d", entity.getDealYear(), entity.getDealMonth()));

        entity.setSggNm(node.path("sggNm").asText("").trim());
        entity.setUmdNm(node.path("umdNm").asText("").trim());
        entity.setJibun(node.path("jibun").asText("").trim());
        entity.setJimok(node.path("jimok").asText("").trim());
        entity.setLandUse(node.path("landUse").asText("").trim());
        entity.setDealingGbn(node.path("dealingGbn").asText("").trim());

        return entity;
    }
}
