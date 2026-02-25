package com.avengers.matefarm.news.service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.avengers.matefarm.news.api.NewsAPI;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class NewsService {
    
    private final NewsAPI newsApi;

    public Mono<Map<String, Object>> getAgriNews() {
        System.out.println(">>>>>>>>>>>>>>Service is start");
        String query = "농업 관련 지역뉴스";
        String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
        return newsApi.getNews(encodedQuery, 10);
    }
}
