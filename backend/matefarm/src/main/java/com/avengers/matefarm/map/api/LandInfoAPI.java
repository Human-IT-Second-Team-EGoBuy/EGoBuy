package com.avengers.matefarm.map.api;

import java.io.StringReader;
import java.net.URI;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

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

    /**
     * Mapper는 thread-safe로 알려져 있고(일반적 사용), 재사용이 성능상 유리함.
     * - 요청마다 new로 만들지 말고 필드로 두는게 일반적
     */
    private final XmlMapper xmlMapper = new XmlMapper();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public LandInfoAPI(WebClient webClient) {
        this.webClient = webClient;
    }

    @Value("${FARM_OMINFO_API_URL}")
    private String ominfoUrl;

    @Value("${FARM_APINFO_API_URL}")
    private String apiinfoUrl;

    @Value("${FARM_KALINFO_API_URL}")
    private String kalinfoUrl;

    @Value("${FARM_PHINFO_API_URL}")
    private String phinfoUrl;

    @Value("${FARM_MGINFO_API_URL}")
    private String mginfoUrl;

    @Value("${FARM_SAINFO_API_URL}")
    private String sainfoUrl;

    @Value("${FARM_CALINFO_API_URL}")
    private String calinfoUrl;

    @Value("${DATAPORTAL_API_KEY}")
    private String serviceKey;

    /**
     * ✅ 데드록/스레드 홀딩 리스크를 줄이기 위해 block() 제거
     * - WebFlux 컨트롤러/서비스에서는 이 Mono를 그대로 반환/조합하는게 정석
     */
    public Mono<Map<String, String>> getLandInfoData(String lawdCode) {

        List<ApiTarget> targets = List.of(
                new ApiTarget("examOmInfo", ominfoUrl),
                new ApiTarget("examApInfo", apiinfoUrl),
                new ApiTarget("examKalInfo", kalinfoUrl),
                new ApiTarget("examPhInfo", phinfoUrl),
                new ApiTarget("examMgInfo", mginfoUrl),
                new ApiTarget("examSalInfo", sainfoUrl),
                new ApiTarget("examCalInfo", calinfoUrl)
        );

        int concurrency = 5;

        return Flux.fromIterable(targets)
                .flatMap(target ->
                        requestsOneWithRetry(target.name(), target.url(), lawdCode)
                                .map(body -> Map.entry(target.name(), body))
                        , concurrency)
                .collectMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        LinkedHashMap::new
                );
    }

    /**
     * ✅ “꼭 동기 Map”이 필요한 legacy/MVC 지점에서만 사용
     * - WebFlux 체인(reactor thread)에서 호출하면 안 됨
     * - boundedElastic으로 격리해서 데드락/이벤트루프 블로킹을 피함
     */
    public Map<String, String> getLandInfoDataBlocking(String lawdCode) {
        return getLandInfoData(lawdCode)
                .subscribeOn(Schedulers.boundedElastic())
                .block(Duration.ofSeconds(45));
    }

    /**
     * ✅ 정책:
     * - 4xx는 재시도 금지(즉시 실패)
     * - XML header 단계에서 성공/실패 판단
     * - item 없으면 데이터 없음으로 간주하고 재시도
     * - 전체 상한 timeout 40초(재시도 포함)
     */
    private Mono<String> requestsOneWithRetry(String label, String baseUrl, String lawdCode) {
        return requestsOneRawXml(label, baseUrl, lawdCode)
                // 1) XML 단계에서 header.resultCode 검사 + item 추출(성공이면 item JSON 반환)
                .map(xml -> parseXmlToItemJsonOrThrow(label, xml))

                // 2) 재시도(일시적 오류만)
                .retryWhen(
                        Retry.backoff(5, Duration.ofMillis(500))
                                .maxBackoff(Duration.ofSeconds(8))
                                .filter(this::isRetryable)
                                .doBeforeRetry(rs -> {
                                    log.warn("{} retry={} cause={} msg={}",
                                            label,
                                            rs.totalRetries() + 1,
                                            rs.failure().getClass().getSimpleName(),
                                            safe(rs.failure().getMessage()));
                                })
                )

                // 3) ✅ 전체 상한 40초 (재시도 포함)
                .timeout(Duration.ofSeconds(40))

                // 4) 최종 실패시: 호출한 쪽에서 map에 ERROR로 남기기
                .onErrorResume(e -> Mono.just("ERROR: " + e.getClass().getSimpleName() + ": " + safe(e.getMessage())));
    }

    /**
     * ✅ “원본 XML”을 가져오는 단계
     * - HTTP 4xx/5xx 구분해서 예외 타입을 다르게 던짐
     * - 여기서는 XML 파싱/업무판단 안 함(순수 transport)
     */
    private Mono<String> requestsOneRawXml(String label, String baseUrl, String lawdCode) {

        URI uri = UriComponentsBuilder
                .fromUriString(baseUrl)
                .queryParam("serviceKey", serviceKey)
                .queryParam("STDG_CD", lawdCode)
                .build(true)
                .toUri();

        return webClient.get()
                .uri(uri)
                .retrieve()

                // ✅ HTTP 에러는 여기서 명확히 분류
                .onStatus(HttpStatusCode::isError, resp ->
                        resp.bodyToMono(String.class)
                                .defaultIfEmpty("")
                                .flatMap(body -> {
                                    int status = resp.statusCode().value();
                                    String msg = "HTTP " + status + " body=" + shrink(body);
                                    if (status >= 400 && status < 500) {
                                        // 4xx는 재시도 금지
                                        return Mono.error(new NonRetryableHttpException(msg));
                                    }
                                    // 5xx는 재시도 가능
                                    return Mono.error(new RetryableHttpException(msg));
                                })
                )

                .bodyToMono(String.class)

                // 개별 호출 타임아웃(네가 이미 10초를 쓰고 있었고, 유지)
                .timeout(Duration.ofSeconds(10))

                .doOnSubscribe(s -> log.info(">>> [API 호출 시작] label={} url={} lawdCode={}", label, baseUrl, lawdCode))
                .doOnNext(xml -> log.info("<<< [API 호출 성공] label={} url={} xml(일부)={}", label, baseUrl, shrink(xml)))
                .doOnError(e -> log.error("!!! [API 호출 에러] label={} url={} lawdCode={} err={}",
                        label, baseUrl, lawdCode, e.toString()));
    }

    /**
     * ✅ XML 단계에서 판단:
     * 1) header.resultCode 확인 (성공코드: 000 또는 00)
     * 2) 성공이면 body/items/item 추출
     * 3) item이 없으면 "데이터 없음"으로 간주하고 예외(NoDataException) -> retry 트리거
     * 4) 최종적으로 item을 JSON 문자열로 반환
     */
    private String parseXmlToItemJsonOrThrow(String label, String xml) {
        try {
            JsonNode root = xmlMapper.readTree(new StringReader(xml));

            // 응답이 <response>로 감싸진 케이스 / 바로 header, body가 최상위인 케이스 모두 대응
            JsonNode responseNode = root.has("response") ? root.path("response") : root;

            String resultCode = responseNode.path("header").path("resultCode").asText(null);
            String resultMsg = responseNode.path("header").path("resultMsg").asText(null);

            // ✅ 성공코드 허용(요구사항: XML 단계에서 판단)
            if (resultCode != null && !("000".equals(resultCode) || "00".equals(resultCode))) {
                // “업무 실패”는 재시도 가능/불가능이 섞일 수 있어 일단 retryable로 둠
                // 특정 코드만 재시도하고 싶으면 여기서 분기 가능
                throw new BizErrorException(label + " resultCode=" + resultCode + " resultMsg=" + safe(resultMsg));
            }

            JsonNode itemNode = responseNode.path("body").path("items").path("item");

            // ✅ 데이터 없음이어도 재시도(요구사항)
            if (itemNode.isMissingNode() || itemNode.isNull()
                    || (itemNode.isArray() && itemNode.size() == 0)
                    || (itemNode.isObject() && itemNode.size() == 0)) {
                throw new NoDataException(label + " no item in response");
            }

            return objectMapper.writeValueAsString(itemNode);

        } catch (NonRetryableHttpException e) {
            // 혹시 parse 단계에 섞여 들어오면 그대로 던짐
            throw e;
        } catch (BizErrorException | NoDataException e) {
            // 정책상 retry 트리거하려고 예외 유지
            throw e;
        } catch (Exception e) {
            // 파싱 실패는 보통 스키마 변경/비정상 응답이라 retry 가치가 애매함
            // 요구사항에 “데이터 없음도 재시도”는 있지만 파싱 실패는 케이스가 달라서 기본은 retryable로 두지 않음
            throw new XmlParsingFailedException(label + " XML_PARSING_FAILED: " + e.getMessage(), e);
        }
    }

    /**
     * ✅ 재시도 대상만 선별:
     * - 4xx(NonRetryableHttpException) 절대 재시도 금지
     * - timeout, 5xx(RetryableHttpException), BizErrorException, NoDataException은 재시도
     * - XmlParsingFailedException은 기본은 재시도하지 않음(원하면 true로 바꿔도 됨)
     */
    private boolean isRetryable(Throwable e) {
        if (e instanceof NonRetryableHttpException) return false;
        if (e instanceof java.util.concurrent.TimeoutException) return true;
        if (e instanceof RetryableHttpException) return true;
        if (e instanceof BizErrorException) return true;
        if (e instanceof NoDataException) return true;
        if (e instanceof XmlParsingFailedException) return false;
        return false;
    }

    private String shrink(String s) {
        if (s == null) return "";
        return s.length() > 200 ? s.substring(0, 200) + "..." : s;
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }

    private record ApiTarget(String name, String url) {}

    // ---- 예외 타입(정책 구분용) ----
    private static class RetryableHttpException extends RuntimeException {
        RetryableHttpException(String message) { super(message); }
    }

    private static class NonRetryableHttpException extends RuntimeException {
        NonRetryableHttpException(String message) { super(message); }
    }

    private static class BizErrorException extends RuntimeException {
        BizErrorException(String message) { super(message); }
    }

    private static class NoDataException extends RuntimeException {
        NoDataException(String message) { super(message); }
    }

    private static class XmlParsingFailedException extends RuntimeException {
        XmlParsingFailedException(String message, Throwable cause) { super(message, cause); }
    }
}
