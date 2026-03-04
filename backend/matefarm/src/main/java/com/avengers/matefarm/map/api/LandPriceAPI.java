package com.avengers.matefarm.map.api;

import java.net.URI;
import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.DefaultUriBuilderFactory;
import org.springframework.web.util.UriComponentsBuilder;

import reactor.core.publisher.Mono;

@Component
public class LandPriceAPI {

    /**
     * - System.out.println 대신 Logger를 사용해서 로그 수집/레벨 관리가 가능하도록 함
     * - 요청 파라미터(법정동, 월, 남은 재시도)를 구조적으로 남겨서 장애/품질 분석
     */
    private static final Logger log = LoggerFactory.getLogger(LandPriceAPI.class);

    private final WebClient webClient;

    /**
     * 환경변수/설정 주입.
     * - 운영 배포에서 값이 비어있으면 즉시 장애가 나도록(=fail-fast) 검증하는 편이 안전함
     * - 아래 validateConfig()에서 체크
     */
    @Value("${map.land_price_api_url}")
    private String tradePriceUrl;

    @Value("${dataportal.api-key}")
    private String serviceKey;

    public LandPriceAPI(WebClient.Builder webClientBuilder) {
        /**
         * 공공데이터 serviceKey는 이미 URL-encoded 형태인 경우가 많아,
         * 기본 인코딩이 걸리면 "이중 인코딩" 문제가 생길 수 있음.
         * => EncodingMode.NONE 유지
         */
        DefaultUriBuilderFactory factory = new DefaultUriBuilderFactory();
        factory.setEncodingMode(DefaultUriBuilderFactory.EncodingMode.NONE);

        this.webClient = webClientBuilder
                .uriBuilderFactory(factory)
                .build();
    }

    /**
     * 토지 실거래가 API 호출 (월 단위)
     *
     * 동작 규칙:
     * 1) 입력값이 비정상이면 즉시 Mono.error로 종료 (원인 빠르게 드러냄)
     * 2) HTTP 오류(4xx/5xx)는 Mono.error로 올림 (상위 서비스가 fallback 정책 결정)
     * 3) 응답이 "정상(00)"이지만 데이터가 없으면 이전 달로 내려가며 재시도
     * 4) timeout은 "호출 1회" 기준으로 적용 (재시도 횟수는 retryCount로 제한)
     */
    public Mono<String> landPriceRequest(String fullLawdCd, String targetMonth, int retryCount) {

        // 0) 설정값 검증
        try {
            validateConfig();
        } catch (RuntimeException e) {
            return Mono.error(e);
        }

        // 1) 입력값 검증
        if (fullLawdCd == null || fullLawdCd.length() < 5) {
            return Mono.error(new IllegalArgumentException("fullLawdCd must be at least 5 chars"));
        }
        if (targetMonth == null || targetMonth.length() != 6 || !isDigits(targetMonth)) {
            return Mono.error(new IllegalArgumentException("targetMonth must be yyyyMM digits"));
        }
        if (retryCount < 0) {
            return Mono.error(new IllegalArgumentException("retryCount must be >= 0"));
        }

        String lawdCd = fullLawdCd.substring(0, 5);

        // 2) 요청 URI 생성
        URI uri = UriComponentsBuilder
                .fromUriString(tradePriceUrl)
                .queryParam("LAWD_CD", lawdCd)
                .queryParam("DEAL_YMD", targetMonth)
                .queryParam("numOfRows", 1000)
                .queryParam("serviceKey", serviceKey)
                .build(true)
                .toUri();

        // serviceKey는 원문 노출금지
        log.info("LandPrice API request start: lawdCd={}, targetMonth={}, retryCountLeft={}, serviceKey={}",
                lawdCd, targetMonth, retryCount, maskKey(serviceKey));

        return webClient.get()
                .uri(uri)
                .retrieve()

                /**
                 * 3) HTTP 상태코드 에러는 명확히 에러로 처리
                 */
                .onStatus(HttpStatusCode::isError, resp ->
                        resp.bodyToMono(String.class)
                            .defaultIfEmpty("")
                            .flatMap(body -> Mono.error(new RuntimeException(
                                    "LandPrice API HTTP error: status=" + resp.statusCode() + ", body=" + truncate(body, 500)
                            )))
                )

                .bodyToMono(String.class)

                // 4) API 응답 처리: 성공/데이터유무 판단 후 필요 시 이전 달 재시도
                .flatMap(xml -> {
                    // (a) 공공데이터 API는 보통 resultCode=000이 성공
                    String resultCode = extractTagValue(xml, "resultCode");
                    String resultMsg  = extractTagValue(xml, "resultMsg");

                    if (resultCode != null && !("00".equals(resultCode) || "000".equals(resultCode))) {
                        // "정상 응답이지만 실패 코드"인 케이스(인증키/파라미터 오류 등)
                        return Mono.error(new RuntimeException(
                                "LandPrice API result not success: resultCode=" + resultCode + ", resultMsg=" + safe(resultMsg)
                        ));
                    }

                    // (b) 데이터 유무 판단
                    Integer totalCount = extractTagValueAsInt(xml, "totalCount");
                    boolean hasItem = xml.contains("<item>");

                    boolean hasData = (totalCount != null && totalCount > 0) || hasItem;

                    if (!hasData && retryCount > 0) {
                        String prevMonth = getPreviousMonth(targetMonth);
                        log.warn("LandPrice API no data. try previous month: prevMonth={}, lawdCd={}, retryCountLeft={}",
                                prevMonth, lawdCd, retryCount - 1);

                        // 재호출(월을 한 단계 내림) retryCount는 상한 역할
                        return landPriceRequest(fullLawdCd, prevMonth, retryCount - 1);
                    }

                    log.info("LandPrice API response done: lawdCd={}, targetMonth={}, hasData={}, totalCount={}",
                            lawdCd, targetMonth, hasData, totalCount);

                    return Mono.just(xml);
                })

                // 5) 호출 1회 timeout (재시도는 retryCount로 제한)
                .timeout(Duration.ofSeconds(40))

                /**
                 * 6) 에러는 삼키지 않고 위로 전달
                 * - 상위 서비스에서: (에러면 DB fallback / 저장 안함 / 프론트에 캐시값만 반환) 같은 정책을 결정
                 */
                .doOnError(e -> log.error("LandPrice API failed: lawdCd={}, targetMonth={}, retryCountLeft={}, err={}",
                        lawdCd, targetMonth, retryCount, e.toString()));
    }

    /**
     * yyyyMM -> 이전 달 yyyyMM
     * 예) 202602 -> 202601, 202601 -> 202512
     */
    private String getPreviousMonth(String ym) {
        int year = Integer.parseInt(ym.substring(0, 4));
        int month = Integer.parseInt(ym.substring(4, 6));
        if (month == 1) {
            year--;
            month = 12;
        } else {
            month--;
        }
        return String.format("%04d%02d", year, month);
    }

    // -------------------------
    // 아래는 유틸 메서드들
    // -------------------------

    /** 설정 누락은 배포 직후 바로 터지는 장애라서 fail-fast */
    private void validateConfig() {
        if (tradePriceUrl == null || tradePriceUrl.isBlank()) {
            throw new IllegalStateException("TRADE_PRICE_API_URL is blank");
        }
        if (serviceKey == null || serviceKey.isBlank()) {
            throw new IllegalStateException("DATAPORTAL_API_KEY is blank");
        }
    }

    /** 공공 API XML에서 <tag>value</tag> 형태의 단순 값을 문자열로 추출 (가벼운 방어용) */
    private String extractTagValue(String xml, String tagName) {
        if (xml == null || tagName == null) return null;
        String open = "<" + tagName + ">";
        String close = "</" + tagName + ">";
        int s = xml.indexOf(open);
        if (s < 0) return null;
        int e = xml.indexOf(close, s + open.length());
        if (e < 0) return null;
        return xml.substring(s + open.length(), e).trim();
    }

    /** 숫자 파싱 실패는 null 처리(운영에서 형식이 바뀌는 경우 방어) */
    private Integer extractTagValueAsInt(String xml, String tagName) {
        String v = extractTagValue(xml, tagName);
        if (v == null || v.isBlank()) return null;
        try {
            return Integer.parseInt(v);
        } catch (NumberFormatException ignore) {
            return null;
        }
    }

    /** serviceKey 같은 민감값은 마스킹해서 로그에 남김 */
    private String maskKey(String key) {
        if (key == null) return "null";
        int n = key.length();
        if (n <= 8) return "********";
        return key.substring(0, 4) + "****" + key.substring(n - 4);
    }

    /** 로그에 너무 큰 응답 바디가 찍히지 않도록 잘라냄 */
    private String truncate(String s, int max) {
        if (s == null) return null;
        if (s.length() <= max) return s;
        return s.substring(0, max) + "...(truncated)";
    }

    private boolean isDigits(String s) {
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c < '0' || c > '9') return false;
        }
        return true;
    }

    private String safe(String s) {
        return (s == null ? "" : s);
    }
}
