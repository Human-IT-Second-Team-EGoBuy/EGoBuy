package com.avengers.matefarm.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class FastApiWebClientConfig {

    @Bean
    public WebClient fastApiWebClient(@Value("${FASTAPI_BASE_URL}") String baseUrl) {
        return WebClient.builder()
                .baseUrl(baseUrl) // http://orchestrator:8000
                .filter((req, next) -> {
                    // 실제 어디로 호출하는지 로그로 확정
                    System.out.println("[FastAPI] " + req.method() + " " + req.url());
                    return next.exchange(req);
                })
                .build();
    }
}