package com.avengers.matefarm.news.api;

import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class NewsAPI {

    private final WebClient webClient;

    @Value("${oauth2.client.registration.naver.news-client-id}")
    private String clientId;

    @Value("${oauth2.client.registration.naver.news-client-secret}")
    private String clientSecret;

    public NewsAPI(WebClient.Builder webClientBuilder) {
        // baseUrl 설정을 통해 가독성을 높입니다.
        this.webClient = webClientBuilder.clone()
                .baseUrl("https://openapi.naver.com")
                .build();
    }

    public Mono<Map<String, Object>> getNews(String query, int display) {
        log.info(">>> [NEWS API 요청 시작] query: {}, display: {}", query, display);

        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .scheme("https")
                        .host("openapi.naver.com")
                        .path("/v1/search/news.json")
                        .queryParam("query", query)
                        .queryParam("display", display)
                        .queryParam("sort", "sim")
                        .build())
                .header("X-Naver-Client-Id", clientId)
                .header("X-Naver-Client-Secret", clientSecret)
                .retrieve()
                // 1. 응답 바디를 Map 형태로 변환 (가장 범용적인 방식)
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                // 2. 로그 및 디버깅용 사이드 이펙트 추가
                .doOnNext(response -> log.info("<<< [NEWS API 응답 성공]"))
                // 3. 에러 처리
                .onErrorResume(e -> {
                    log.error("!!! [NEWS API 에러 발생] : {}", e.getMessage());
                    return Mono.empty(); // 에러 발생 시 빈 Mono 반환 (필요에 따라 에러 객체 반환 가능)
                });
    }
}