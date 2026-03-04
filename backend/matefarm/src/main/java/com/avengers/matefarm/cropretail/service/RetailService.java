package com.avengers.matefarm.cropretail.service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.avengers.matefarm.common.ResponseDTO;
import com.avengers.matefarm.common.exception.CommonException;
import com.avengers.matefarm.common.exception.ErrorCode;
import com.avengers.matefarm.cropretail.api.KamisAPI;
import com.avengers.matefarm.cropretail.dto.KamisRequestDTO;
import com.avengers.matefarm.cropretail.repository.VarietyRepository;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
@RequiredArgsConstructor
public class RetailService {

    private final VarietyRepository retailRepo;

    private final KamisAPI kamisApi;

    public Mono<Object> getRetailData(String toDate, String regionNm, String cropNm, String filter) {
        String startDate = LocalDate.parse(toDate, DateTimeFormatter.ofPattern("yyyyMMdd"))
                .minusYears(1).format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        String productClsCd = "소매가".equals(filter.trim()) ? "01" : "02";

        return Mono.fromCallable(() -> retailRepo.findCodesByNames(cropNm, regionNm))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(optionalCodes -> Mono.justOrEmpty(optionalCodes))
                .switchIfEmpty(Mono.error(new CommonException(ErrorCode.NOT_FOUND_DATA)))
                .flatMap(codes -> {
                    String rawVrtyCd = codes.getVrtyCd() != null ? codes.getVrtyCd() : "01";
                    String formattedVrtyCd = String.format("%02d", Integer.parseInt(rawVrtyCd));

                    String sggCd = (regionNm != null && !regionNm.isEmpty()) ? codes.getSggCd() : null;

                    KamisRequestDTO requestDTO = KamisRequestDTO.builder()
                            .startDate(startDate)
                            .endDate(toDate)
                            .filter(productClsCd)
                            .ctgryCd(codes.getCtgryCd())
                            .itemCd(codes.getItemCd())
                            .vrtyCd(formattedVrtyCd)
                            .sggCd(sggCd)
                            .build();

                    return kamisApi.getRetailData(requestDTO)
                            .map(rawData -> processRetailSearchResponse(rawData));
                });
    }

    public Mono<ResponseDTO<Object>> getRetailAvgData(String toDate, String cropNm, String filter) {

        String startDate = LocalDate.parse(toDate, DateTimeFormatter.ofPattern("yyyyMMdd"))
                .minusYears(1).format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        String productClsCd = "소매가".equals(filter.trim()) ? "01" : "02";

        return Mono.fromCallable(() -> retailRepo.findCodesByNames(cropNm, null)) // regionNm에 null 전달
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(optionalCodes -> Mono.justOrEmpty(optionalCodes))
                .switchIfEmpty(Mono.error(new CommonException(ErrorCode.NOT_FOUND_DATA)))
                .flatMap(codes -> {
                    KamisRequestDTO requestDTO = KamisRequestDTO.builder()
                            .startDate(startDate)
                            .endDate(toDate)
                            .filter(productClsCd) // 소매가 기준
                            .ctgryCd(codes.getCtgryCd())
                            .itemCd(codes.getItemCd())
                            .vrtyCd(String.format("%02d", Integer.parseInt(codes.getVrtyCd())))
                            .sggCd("") // 전국 평균을 위해 빈 값 설정
                            .build();

                    return kamisApi.getRetailData(requestDTO);
                })
                .map(rawData -> processMonthlyAverage(rawData, toDate))
                .map(processedList -> ResponseDTO.ok((Object) processedList));

    }

    private List<Map<String, Object>> processMonthlyAverage(Object rawData, String toDate) {
        try {
            // rawData(JsonNode/Map)에서 실제 아이템 리스트 추출
            Map<String, Object> dataMap = (Map<String, Object>) rawData;
            Map<String, Object> dataRoot = (Map<String, Object>) dataMap.get("data");
            List<Map<String, String>> items = (List<Map<String, String>>) dataRoot.get("item");

            return items.stream()
                    // 1. "평균" 데이터만 필터링
                    .filter(item -> "평균".equals(item.get("countyname")))
                    // 2. 날짜 파싱 및 최근 4개월 범위 필터링
                    .map(item -> {
                        String dateStr = item.get("yyyy") + "-" + item.get("regday").replace("/", "-");
                        return new Pair<>(LocalDate.parse(dateStr),
                                Double.parseDouble(item.get("price").replace(",", "")));
                    })
                    // 3. 월별로 그룹화 (yyyy-MM)
                    .collect(Collectors.groupingBy(
                            pair -> pair.key.format(DateTimeFormatter.ofPattern("yyyy-MM")),
                            Collectors.averagingDouble(pair -> pair.value)))
                    // 4. 리스트 형태로 변환 및 정렬
                    .entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .map(entry -> {
                        Map<String, Object> map = new HashMap<>();
                        map.put("month", entry.getKey());
                        map.put("avgPrice", Math.round(entry.getValue())); // 소수점 반올림
                        return map;
                    })
                    .collect(Collectors.toList());

        } catch (Exception e) {
            throw new RuntimeException("데이터 가공 중 에러 발생", e);
        }
    }

    private static class Pair<K, V> {
        K key;
        V value;

        Pair(K k, V v) {
            this.key = k;
            this.value = v;
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> processRetailSearchResponse(Object rawData) {
        try {
            Map<String, Object> root = (Map<String, Object>) rawData;

            Map<String, Object> result = new HashMap<>(root);

            Map<String, Object> data = (Map<String, Object>) root.get("data");
            if (data == null)
                return result;

            List<Map<String, Object>> item = (List<Map<String, Object>>) data.get("item");
            if (item == null)
                return result;

            LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

            // 1) 평균 제거 + 평년 제외 => "실제 지역 원본 데이터"
            List<Map<String, Object>> itemFiltered = item.stream()
                    .filter(row -> !"평균".equals(String.valueOf(row.get("countyname"))))
                    .filter(row -> !"평년".equals(String.valueOf(row.get("countyname"))))
                    .map(row -> {
                        Map<String, Object> mapped = new HashMap<>(row);

                        // 프론트 편의용 필드 추가 (선택이지만 추천)
                        try {
                            String yyyy = String.valueOf(row.get("yyyy"));
                            String regday = String.valueOf(row.get("regday"));
                            LocalDate rowDate = LocalDate.parse(yyyy + "-" + regday.replace("/", "-"));

                            mapped.put("date", rowDate.format(formatter));
                            mapped.put("priceValue", parsePrice(row.get("price")));
                        } catch (Exception e) {
                            // 날짜 파싱 실패 시 원본 유지
                        }

                        return mapped;
                    })
                    .collect(Collectors.toList());

            // 2) itemFiltered를 YYYYMM 기준으로 그룹핑
            Map<String, List<Map<String, Object>>> groupedItem = itemFiltered.stream()
                    .collect(Collectors.groupingBy(
                            row -> {
                                String yyyy = String.valueOf(row.get("yyyy")); // "2025"
                                String regday = String.valueOf(row.get("regday")); // "03/04"
                                String mm = regday.substring(0, 2); // "03"
                                return yyyy + mm; // "202503"
                            },
                            java.util.TreeMap::new,
                            Collectors.toList()));

            // 3) 평년 데이터 분리 (오늘 이후만)
            List<Map<String, Object>> item2 = item.stream()
                    .filter(row -> "평년".equals(String.valueOf(row.get("countyname"))))
                    .filter(row -> {
                        try {
                            String yyyy = String.valueOf(row.get("yyyy"));
                            String regday = String.valueOf(row.get("regday"));
                            LocalDate rowDate = LocalDate.parse(yyyy + "-" + regday.replace("/", "-"));
                            return !rowDate.isBefore(today);
                        } catch (Exception e) {
                            return false;
                        }
                    })
                    .map(row -> {
                        Map<String, Object> mapped = new HashMap<>(row);

                        try {
                            String yyyy = String.valueOf(row.get("yyyy"));
                            String regday = String.valueOf(row.get("regday"));
                            LocalDate rowDate = LocalDate.parse(yyyy + "-" + regday.replace("/", "-"));

                            mapped.put("date", rowDate.format(formatter));
                            mapped.put("priceValue", parsePrice(row.get("price")));
                        } catch (Exception e) {
                            // 원본 유지
                        }

                        return mapped;
                    })
                    .collect(Collectors.toList());

            // (선택) 평년 데이터도 월별 그룹핑하고 싶으면 아래 추가
            Map<String, List<Map<String, Object>>> groupedItem2 = item2.stream()
                    .collect(Collectors.groupingBy(
                            row -> {
                                String yyyy = String.valueOf(row.get("yyyy"));
                                String regday = String.valueOf(row.get("regday"));
                                String mm = regday.substring(0, 2);
                                return yyyy + mm;
                            },
                            java.util.TreeMap::new,
                            Collectors.toList()));

            Map<String, Object> newData = new HashMap<>(data);

            // ✅ 네가 원한 형태: item을 월별 객체로 교체
            newData.put("item", groupedItem);

            // 평년도 월별 객체 필요하면 같이 추가
            newData.put("item2Grouped", groupedItem2);

            result.put("data", newData);

            return result;

        } catch (Exception e) {
            throw new RuntimeException("소매가 검색 응답 가공 중 에러 발생", e);
        }
    }

    private Long parsePrice(Object priceObj) {
        if (priceObj == null)
            return null;
        try {
            return Long.parseLong(String.valueOf(priceObj).replace(",", "").trim());
        } catch (Exception e) {
            return null;
        }
    }
}
