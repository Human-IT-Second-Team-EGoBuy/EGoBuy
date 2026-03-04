package com.avengers.matefarm.cropretail.api;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import com.avengers.matefarm.cropretail.dto.KamisRequestDTO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class KamisAPI {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final String kamisKey;
    private final String kamisId;
    private final WebClient webClient;

    public KamisAPI(
            @Value("${kamis.kamis_api_url}") String kamisUrl,
            @Value("${kamis.kamis_api_key}") String kamisKey,
            @Value("${kamis.kamis_api_id}") String kamisId,
            WebClient.Builder webClientBuilder) {

        this.kamisKey = kamisKey;
        this.kamisId = kamisId;
        this.webClient = webClientBuilder.clone()
                            .baseUrl(kamisUrl)
                            .codecs(configurer -> configurer
                                    .defaultCodecs()
                                    .maxInMemorySize(2 * 1024 * 1024)
                            )
                            .build();
    }

    public Mono<Object> getRetailData(KamisRequestDTO dto) {
        return webClient.get()
                .uri(uriBuilder -> {uriBuilder
                        .queryParam("action", "periodProductList")
                        .queryParam("p_cert_key", kamisKey)
                        .queryParam("p_cert_id", kamisId)
                        .queryParam("p_returntype", "json")
                        .queryParam("p_startday", dto.getStartDate())
                        .queryParam("p_endday", dto.getEndDate())
                        .queryParam("p_productclscode", dto.getFilter())
                        .queryParam("p_itemcategorycode", dto.getCtgryCd())
                        .queryParam("p_itemcode", dto.getItemCd())
                        .queryParam("p_kindcode", dto.getVrtyCd());

                        if (dto.getSggCd() != null && !dto.getSggCd().isEmpty()) {
                            uriBuilder.queryParam("p_countrycode", dto.getSggCd());
                        }

                        return uriBuilder.build();
                    })
                .accept(MediaType.TEXT_PLAIN, MediaType.APPLICATION_XML, MediaType.ALL)
                .retrieve()
                .bodyToMono(String.class)
                .map(str -> {
                    try {
                        return objectMapper.readValue(str, Object.class);
                    } catch (Exception e) {
                        throw new RuntimeException("json 파싱 에러: " + e.getMessage());
                    }
                });
    }
}
