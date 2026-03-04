package com.avengers.matefarm.news.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.avengers.matefarm.common.ResponseDTO;
import com.avengers.matefarm.news.service.NewsService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/news")
@RequiredArgsConstructor
public class NewsController {

    private final NewsService newsService;
    
    @GetMapping("/open")
    public Mono<ResponseDTO<Map<String, Object>>> newsController() {
        System.out.println(">>>>>>>>>>>controller is start");
        return newsService.getAgriNews()
                .map(data -> ResponseDTO.ok(data));
    }
}
