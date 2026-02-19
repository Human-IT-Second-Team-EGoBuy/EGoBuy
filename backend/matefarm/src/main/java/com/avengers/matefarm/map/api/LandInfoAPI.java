package com.avengers.matefarm.map.api;

import java.io.StringReader;
import java.net.URI;
import java.time.Duration;
import java.util.*;
import java.util.stream.StreamSupport;

import org.apache.commons.math3.exception.NoDataException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import com.avengers.matefarm.common.exception.CommonException;
import com.avengers.matefarm.common.exception.ErrorCode;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.retry.Retry;

@Component
public class LandInfoAPI {

    private static final Logger log = LoggerFactory.getLogger(LandInfoAPI.class);

    private final WebClient webClient;
    private final XmlMapper xmlMapper = new XmlMapper();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${FARM_SOILINFO_API_URL}")
    private String soilUrl;

    @Value("${DATAPORTAL_API_KEY}")
    private String serviceKey;

    public LandInfoAPI(WebClient webClient) {
        this.webClient = webClient;
    }

    // ==========================================
    // 1. Public Entry Points (공개 API)
    // ==========================================

    public Mono<Map<String, String>> getLandInfoData(String lawdCode) {
        List<ApiTarget> targets = List.of(new ApiTarget("soilUrl", soilUrl));

        return Flux.fromIterable(targets)
                .flatMap(target -> requestsOneWithRetry(target.name(), target.url(), lawdCode)
                        .map(body -> Map.entry(target.name(), body)), 5)
                .collectMap(Map.Entry::getKey, Map.Entry::getValue, LinkedHashMap::new);
    }

    public Map<String, String> getLandInfoDataBlocking(String lawdCode) {
        return getLandInfoData(lawdCode)
                .subscribeOn(Schedulers.boundedElastic())
                .block(Duration.ofSeconds(45));
    }

    // ==========================================
    // 2. Core Business Logic (핵심 비즈니스 로직 - 페이징/결합)
    // ==========================================

    private Mono<String> requestsOneWithRetry(String label, String baseUrl, String lawdCode) {
        return fetchAllPages(label, baseUrl, lawdCode)
                .collectList() // 모든 페이지의 아이템들을 하나의 List로 수집
                .map(allNodes -> {
                    try {
                        // List<JsonNode>를 JSON Array 문자열로 변환
                        return objectMapper.writeValueAsString(allNodes);
                    } catch (Exception e) {
                        return "[]";
                    }
                })
                .timeout(Duration.ofSeconds(60))
                .onErrorResume(e -> Mono.just("ERROR: " + e.getClass().getSimpleName() + ": " + safe(e.getMessage())));
    }

    private Flux<JsonNode> fetchAllPages(String label, String baseUrl, String lawdCode) {
        return getPageAsNode(label, baseUrl, lawdCode, 1)
                .expand(rootNode -> {
                    PageInfo info = extractPageInfo(rootNode);
                    if (info.currentPage() < info.lastPage()) {
                        return getPageAsNode(label, baseUrl, lawdCode, info.currentPage() + 1);
                    }
                    return Mono.empty();
                })
                .flatMapIterable(this::extractItems); // 각 페이지의 item 노드들을 Flux의 개별 요소로 평탄화
    }

    // ==========================================
    // 3. Transport Layer (네트워크 통신 및 1차 가공)
    // ==========================================

    private Mono<JsonNode> getPageAsNode(String label, String baseUrl, String lawdCode, int pageNo) {
        return requestsOneRawXml(label, baseUrl, lawdCode, pageNo)
                .map(xml -> {
                    try {
                        // XML을 JsonNode로 한 번만 파싱
                        JsonNode root = xmlMapper.readTree(new StringReader(xml));
                        validateResponse(label, root);
                        return root;
                    } catch (Exception e) {
                        if (e instanceof CommonException)
                            throw (CommonException) e;
                        throw new CommonException(ErrorCode.MAP_PARSING_ERROR);
                    }
                })
                .retryWhen(Retry.backoff(3, Duration.ofMillis(500)).filter(this::isRetryable));
    }

    private Mono<String> requestsOneRawXml(String label, String baseUrl, String lawdCode, int pageNo) {
        URI uri = UriComponentsBuilder.fromUriString(baseUrl)
                .queryParam("serviceKey", serviceKey)
                .queryParam("Page_Size", 200)
                .queryParam("Page_No", pageNo)
                .queryParam("STDG_CD", lawdCode)
                .build(true).toUri();

        log.info(">>> [API URI] {}", uri.toString());

        return webClient.get().uri(uri).retrieve()
                .onStatus(HttpStatusCode::isError, resp -> resp.bodyToMono(String.class)
                        .flatMap(body -> {
                            int status = resp.statusCode().value();
                            return Mono.error(new CommonException(ErrorCode.MAP_API_ERROR));
                        }))
                .bodyToMono(String.class)
                .doOnNext(body -> log.info("<<< [API 응답] {} page {}\n{}", label, pageNo, shrink(body)))
                .timeout(Duration.ofSeconds(10))
                .doOnSubscribe(s -> log.info(">>> [API 시작] {} - page {}", label, pageNo));
    }

    // ==========================================
    // 4. Helper Methods (데이터 추출 및 유틸리티)
    // ==========================================

    private void validateResponse(String label, JsonNode root) {
        JsonNode header = root.has("response")
                ? root.path("response").path("header")
                : root.path("header");

        // 네 응답 스키마
        String resultCode = header.path("Result_Code").asText(null);
        String resultMsg = header.path("Result_Msg").asText(null);

        // 혹시 다른 스키마도 대비 (기존 호환)
        if (resultCode == null)
            resultCode = header.path("resultCode").asText(null);
        if (resultMsg == null)
            resultMsg = header.path("resultMsg").asText(null);

        boolean ok = "200".equals(resultCode) || "000".equals(resultCode) || "00".equals(resultCode);

        if (!ok) {
            String msg = (resultMsg != null && !resultMsg.isBlank()) ? resultMsg : "API error";
            throw new CommonException(ErrorCode.MAP_API_ERROR);
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> safeMap(Object o) {
        if (o instanceof Map<?, ?> m)
            return (Map<String, Object>) m;
        return java.util.Collections.emptyMap();
    }

    private String getAsString(Object o) {
        return (o == null) ? null : String.valueOf(o).trim();
    }

    private PageInfo extractPageInfo(JsonNode root) {
        JsonNode body = root.has("response")
                ? root.path("response").path("body")
                : root.path("body");

        int rcdCnt = body.path("Rcdcnt").asInt(0);
        if (rcdCnt == 0)
            rcdCnt = body.path("rcdcnt").asInt(0);

        int currentPage = body.path("Page_No").asInt(1);
        if (currentPage == 1)
            currentPage = body.path("pageNo").asInt(1);

        int totalCount = body.path("Total_Count").asInt(0);
        if (totalCount == 0)
            totalCount = body.path("totalCount").asInt(0);

        int lastPage = (rcdCnt <= 0) ? currentPage : (int) Math.ceil((double) totalCount / rcdCnt);
        return new PageInfo(currentPage, lastPage);
    }

    private Object firstNonNull(Object... values) {
        for (Object v : values)
            if (v != null)
                return v;
        return null;
    }

    private int getAsInt(Object o, int defaultValue) {
        if (o == null)
            return defaultValue;
        try {
            return Integer.parseInt(String.valueOf(o).trim());
        } catch (Exception ex) {
            return defaultValue;
        }
    }

    private Iterable<JsonNode> extractItems(JsonNode root) {
        JsonNode itemsNode = root.has("response") ? root.path("response").path("body").path("items").path("item")
                : root.path("body").path("items").path("item");

        if (itemsNode.isMissingNode() || itemsNode.isNull()) {
            log.info("<<< [API 파싱] items 없음");
            return Collections.emptyList();
        }

        int size = itemsNode.isArray() ? itemsNode.size() : 1;
        log.info("<<< [API 파싱] items count={}", size);

        // 단일 객체인 경우와 배열인 경우 모두 대응
        return itemsNode.isArray() ? itemsNode : Collections.singletonList(itemsNode);
    }

    private boolean isRetryable(Throwable e) {
        if (e instanceof CommonException ce) {
            // 500번대 에러(MAP_API_ERROR 등)일 때만 재시도하도록 설정 가능
            return ce.getErrorCode().getHttpStatus().is5xxServerError();
        }
        return e instanceof java.util.concurrent.TimeoutException;
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }

    private String shrink(String s) {
        return (s != null && s.length() > 200) ? s.substring(0, 200) + "..." : s;
    }

    private record ApiTarget(String name, String url) {
    }

    private record PageInfo(int currentPage, int lastPage) {
    }

}