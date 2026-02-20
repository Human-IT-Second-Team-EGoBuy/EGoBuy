package com.avengers.matefarm.map.api;

import java.util.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class VworldWfsAPI {

    @Value("${VWORLD_WFS_API_URL}")
    private String vworldApiUrl;

    @Value("${VWORLD_WFS_API_KEY}")
    private String vworldApiKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * @param hasRi true면 리(lt_c_adri_info), false면 읍/면/동(lt_c_ademd_info)
     */
    public List<Map<String, Double>> getWfsData(String regionName, boolean hasRi) {
        try {
            // 0) regionName에 개행/중복공백 섞이면 100% 터짐 → 먼저 정리
            String normalizedRegionName = (regionName == null ? "" : regionName)
                    .replaceAll("\\s+", " ") // \n, \t 포함 모든 공백을 한 칸으로
                    .trim();

            String typeName = hasRi ? "lt_c_adri_info" : "lt_c_ademd_info";
            String propertyName = hasRi ? "full_nm,li_kor_nm,ag_geom" : "full_nm,emd_kor_nm,ag_geom";

            // 1. 필터 문자열 작성 (쌍따옴표 대신 홀따옴표 ' 사용 권장)
            String filter = "<ogc:Filter xmlns:ogc='http://www.opengis.net/ogc'>" +
                    "<ogc:PropertyIsEqualTo matchCase='true'>" +
                    "<ogc:PropertyName>full_nm</ogc:PropertyName>" +
                    "<ogc:Literal>" + normalizedRegionName + "</ogc:Literal>" +
                    "</ogc:PropertyIsEqualTo>" +
                    "</ogc:Filter>";

            // 2. 미리 인코딩(encodeQueryParam) 하지 마세요!
            // UriComponentsBuilder가 내부적으로 가장 적합한 인코딩을 수행합니다.

            String url = UriComponentsBuilder.fromUriString(vworldApiUrl)
                    .queryParam("SERVICE", "WFS")
                    .queryParam("REQUEST", "GetFeature")
                    .queryParam("TYPENAME", typeName)
                    .queryParam("PROPERTYNAME", propertyName)
                    .queryParam("VERSION", "1.1.0")
                    .queryParam("SRSNAME", "EPSG:4326")
                    .queryParam("OUTPUTFORMAT", "application/json")
                    .queryParam("KEY", vworldApiKey)
                    .queryParam("DOMAIN", "localhost:8081")
                    .queryParam("FILTER", filter) // 인코딩 안 된 생(raw) 문자열 주입
                    .build() // build(true)가 아니라 그냥 build() 사용
                    .toUriString();

            System.out.println(">>>>>> 요청 URL: " + url);

            System.out.println(">>>>>>url = " + url);
            System.out.println(">>>>>>normalizedRegionName = [" + normalizedRegionName + "]");
            System.out.println(">>>>>>rawFilter = " + filter);

            // 3) JSON/ XML 구분을 "Content-Type + 첫글자"로 처리
            ResponseEntity<String> res = restTemplate.exchange(url, HttpMethod.GET, null, String.class);

            String body = res.getBody();
            MediaType contentType = res.getHeaders().getContentType();

            if (body == null || body.isBlank()) {
                System.out.println("VWORLD response empty");
                return Collections.emptyList();
            }

            String trimmed = body.trim();

            // 서버가 에러 시 ExceptionReport(XML) 주는 케이스가 많음
            if ((contentType != null
                    && (contentType.includes(MediaType.APPLICATION_XML) || contentType.includes(MediaType.TEXT_XML)))
                    || trimmed.startsWith("<")) {
                System.out.println("VWORLD returned XML (likely error). status=" + res.getStatusCode());
                System.out.println(trimmed.substring(0, Math.min(trimmed.length(), 1200)));
                return Collections.emptyList();
            }

            JsonNode root = objectMapper.readTree(body);

            JsonNode features = root.path("features");
            if (!features.isArray() || features.isEmpty()) {
                return Collections.emptyList();
            }

            JsonNode coordinates = features.get(0).path("geometry").path("coordinates");

            // MultiPolygon/Polygon 방어
            JsonNode ring = coordinates;
            if (ring.isArray() && ring.size() > 0)
                ring = ring.get(0);
            if (ring != null && ring.isArray() && ring.size() > 0)
                ring = ring.get(0);

            if (ring == null || !ring.isArray() || ring.isEmpty()) {
                return Collections.emptyList();
            }

            List<Map<String, Double>> path = new ArrayList<>();
            for (JsonNode point : ring) {
                if (point == null || point.size() < 2)
                    continue;

                Map<String, Double> latLng = new HashMap<>();
                // Vworld 좌표는 [lng, lat]
                latLng.put("lng", point.get(0).asDouble());
                latLng.put("lat", point.get(1).asDouble());
                path.add(latLng);
            }

            return path;

        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }
}
