package com.avengers.matefarm.map.service;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.avengers.matefarm.common.exception.CommonException;
import com.avengers.matefarm.common.exception.ErrorCode;
import com.avengers.matefarm.map.api.LandPriceAPI;
import com.avengers.matefarm.map.api.VworldWfsAPI;
import com.avengers.matefarm.map.dto.entity.RegCodeEntity;
import com.avengers.matefarm.map.dto.entity.TradeHistoryEntity;
import com.avengers.matefarm.map.repository.MapRepository;
import com.avengers.matefarm.map.repository.TradeHistoryRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
@RequiredArgsConstructor
public class LandPriceService {

    private final MapRepository mapRepository;
    private final TradeHistoryRepository landTradeHistoryRepository;

    private final LandPriceAPI landPriceAPI;
    private final VworldWfsAPI vworldWfsAPI;

    private final XmlMapper xmlMapper = new XmlMapper();

    /**
     * 기존 MapService.lawdCodeSelect()에 있던 토지 매매가 로직을 그대로 분리
     * - DB 캐시 있으면 반환
     * - 없으면 API 호출 -> 파싱 -> 저장(saveAll) -> 반환
     * - boundary도 같이 묶어서 반환
     */
    @Transactional
    public Mono<Map<String, Object>> getTradeHistory(String locatadd_nm, boolean hasRi) {
        final String cleanAddr = (locatadd_nm == null) ? null : locatadd_nm.trim();
        if (cleanAddr == null || cleanAddr.isEmpty()) {
            throw new CommonException(ErrorCode.MISSING_REQUEST_PARAMETER);
        }

        String fullLawdCd = mapRepository.findLawdCdByLocataddNm(cleanAddr)
                .map(RegCodeEntity::getLawdCd)
                .orElseThrow(() -> new CommonException(ErrorCode.NOT_FOUND_FILES));

        String shortLawdCd = fullLawdCd.substring(0, 5);
        String startMonth = "202601";

        // 1) boundary는 별도 Mono (블로킹이면 boundedElastic)
        Mono<List<Map<String, Double>>> boundaryMono = Mono.fromCallable(() -> vworldWfsAPI.getWfsData(cleanAddr, hasRi))
                .subscribeOn(Schedulers.boundedElastic())
                .onErrorReturn(Collections.emptyList());

        // 2) 매매가 데이터 Mono
        Mono<List<TradeHistoryEntity>> tradeMono;

        List<TradeHistoryEntity> cachedData = landTradeHistoryRepository.findByLawdCd(shortLawdCd);
        if (!cachedData.isEmpty()) {
            tradeMono = Mono.just(cachedData);
        } else {
            tradeMono = landPriceAPI.landPriceRequest(shortLawdCd, startMonth, 12)
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

        // 3) 합쳐서 response
        return Mono.zip(tradeMono, boundaryMono)
                .map(tuple -> {
                    List<TradeHistoryEntity> tradeList = tuple.getT1();
                    List<Map<String, Double>> boundary = tuple.getT2();

                    Map<String, Object> finalResponse = new HashMap<>();
                    finalResponse.put("tradeList", tradeList);
                    finalResponse.put("boundary", boundary);
                    finalResponse.put("centerAddress", cleanAddr);

                    return finalResponse;
                });
    }

    // ==========================
    // 파싱 로직 (기존 MapService 것 그대로)
    // ==========================

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
