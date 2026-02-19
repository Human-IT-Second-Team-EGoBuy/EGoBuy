package com.avengers.matefarm.map.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.avengers.matefarm.common.exception.CommonException;
import com.avengers.matefarm.common.exception.ErrorCode;
import com.avengers.matefarm.map.api.LandInfoAPI;
import com.avengers.matefarm.map.api.VworldWfsAPI;
import com.avengers.matefarm.map.dto.entity.LandSoilExamEntity;
import com.avengers.matefarm.map.dto.entity.LandSoilExamLatestEntity;
import com.avengers.matefarm.map.dto.entity.RegCodeEntity;
import com.avengers.matefarm.map.repository.LandSoilExamLatestRepository;
import com.avengers.matefarm.map.repository.LandSoilExamRepository;
import com.avengers.matefarm.map.repository.MapRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class SoilService {

    private final MapRepository mapRepository;
    private final LandInfoAPI landInfoAPI;
    private final VworldWfsAPI vworldWfsAPI;

    private final LandSoilExamRepository landSoilExamRepository;
    private final LandSoilExamLatestRepository landSoilExamLatestRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // TTL (일 단위) : "우리가 API를 마지막으로 확인한 시각(synced_at)" 기준
    private static final int SOIL_TTL_DAYS = 30;

    // 기본 Exam_Type (너 예시 응답이 1)
    private static final int DEFAULT_EXAM_TYPE = 1;

    /**
     * 프론트에 줄 응답(토양 + boundary + centerAddress)을 한 번에 만든다.
     * - latest가 있고 synced_at이 TTL 안이면 DB latest 그대로 반환
     * - 아니면 API 재조회 -> 히스토리 저장 -> latest 갱신 -> 반환
     */
    public Map<String, Object> getSoilAndBoundaryInfo(String locatadd_nm, boolean hasRi) {
        String cleanAddr = (locatadd_nm == null) ? "" : locatadd_nm.trim();
        if (cleanAddr.isEmpty()) {
            throw new CommonException(ErrorCode.MISSING_REQUEST_PARAMETER);
        }

        Map<String, Object> finalResponse = new HashMap<>();

        // 1) 법정동(여기서는 stdgCd로 쓸 regionCd) 조회
        String regionCd = mapRepository.findRegionCdByLocataddNm(cleanAddr)
                .map(RegCodeEntity::getRegionCd)
                .orElse(null);

        // 2) 토양 데이터
        if (regionCd != null) {
            int examType = DEFAULT_EXAM_TYPE;

            Map<String, String> soilData = landSoilExamLatestRepository
                    .findByStdgCdAndPnuNmAndExamType(regionCd, cleanAddr, examType)
                    .filter(latest -> !isStale(latest.getSyncedAt(), SOIL_TTL_DAYS))
                    .map(this::toSoilResponseMapFromLatest)
                    .orElseGet(() -> fetchSaveAndReturnSoil(regionCd, cleanAddr, examType));

            finalResponse.putAll(soilData);
        } else {
            // regionCd 매칭 실패해도 boundary는 내려주니까, 로그만 남김
            System.out.println("⚠️ DB 매칭 실패 주소: " + cleanAddr);
        }

        // 3) boundary (regionCd 유무와 상관없이 시도)
        try {
            List<Map<String, Double>> boundary = vworldWfsAPI.getWfsData(cleanAddr, hasRi);
            finalResponse.put("boundary", boundary);
        } catch (Exception e) {
            finalResponse.put("boundary", Collections.emptyList());
        }

        finalResponse.put("centerAddress", cleanAddr);
        return finalResponse;
    }

    // -------------------------------
    // 외부 API 재조회 + 저장 + latest 갱신
    // -------------------------------

    private Map<String, String> fetchSaveAndReturnSoil(String regionCd, String cleanAddr, int examType) {
        Map<String, String> apiResult = landInfoAPI.getLandInfoDataBlocking(regionCd);
        validateSoilApiResult(apiResult);

        String json = apiResult.get("soilUrl");
        // 위에서 수정한 메서드를 호출하여 리스트를 가져옴
        List<JsonNode> matchedItems = parseAndFilterSoilItems(json, cleanAddr, examType);

        if (json == null || json.contains("301")) {
            Map<String, String> emptyMap = new HashMap<>();
            emptyMap.put("centerAddress", cleanAddr); // 주소는 넘겨줘야 어디가 미완료인지 알 수 있음
            emptyMap.put("regionCd", regionCd);
            return emptyMap;
        }

        if (matchedItems.isEmpty()) {
            // 여기서 에러를 던지면 화면이 아예 안 나옵니다.
            // return new HashMap<>(); 등으로 변경 고려
            return Collections.emptyMap();
        }

        // 17건 중 첫 번째 데이터의 실제 지번 주소를 가져옴 (예: "중동 123")
        String actualPnuNm = matchedItems.get(0).path("PNU_Nm").asText("").trim();

        // 히스토리에는 실제 지번 주소(actualPnuNm)로 저장
        saveSoilHistory(regionCd, matchedItems);

        // 가장 최근 검사 기록 조회
        LandSoilExamEntity newest = landSoilExamRepository
                .findFirstByStdgCdAndPnuNmAndExamTypeOrderByExamDayDescIdDesc(regionCd, actualPnuNm, examType)
                .orElseThrow(() -> new CommonException(ErrorCode.INTERNAL_SERVER_ERROR));

        // Latest(최신) 테이블에는 사용자가 요청한 주소(cleanAddr, 예: "중동")를 키로 저장하여
        // 다음 요청 시 바로 검색되도록 함
        upsertLatestFromHistory(regionCd, cleanAddr, examType, newest);

        LandSoilExamLatestEntity latest = landSoilExamLatestRepository
                .findByStdgCdAndPnuNmAndExamType(regionCd, cleanAddr, examType)
                .orElseThrow(() -> new CommonException(ErrorCode.INTERNAL_SERVER_ERROR));

        return toSoilResponseMapFromLatest(latest);
    }

    private void validateSoilApiResult(Map<String, String> apiResult) {
        if (apiResult == null || apiResult.isEmpty()) {
            // 데이터가 아예 없는 경우 에러를 던지지 말고 그냥 반환하거나 로그만 남김
            return;
        }

        String json = apiResult.get("soilUrl");

        if (json != null && json.contains("<Result_Code>301</Result_Code>")) {
            // 로그만 남기고 정상 흐름으로 보냄
            log.info("해당 지역의 토양 데이터가 존재하지 않습니다. (Result_Code: 301)");
            return;
        }

        // API 응답 자체가 ERROR로 시작하는 경우(인증키 문제 등)만 에러 처리
        if (json != null && json.startsWith("ERROR:")) {
            throw new CommonException(ErrorCode.INTERNAL_SERVER_ERROR);
        }

        // 만약 데이터가 없다는 응답(301 등)이 포함된 XML/JSON이라면
        // 여기서 에러를 던지지 말고, parse 단계에서 빈 리스트를 반환하게 해야 함
    }

    private List<JsonNode> parseAndFilterSoilItems(String json, String cleanAddr, int examType) {
        try {
            JsonNode arr = objectMapper.readTree(json);
            if (!arr.isArray())
                return List.of();

            List<JsonNode> list = new java.util.ArrayList<>();
            for (JsonNode item : arr) {
                // API 데이터의 주소 (예: "전라남도 광양시 중동 123")
                String pnuNm = item.path("PNU_Nm").asText("").trim();
                int type = item.path("Exam_Type").asInt(-1);

                // 주소 일치 여부 확인: 사용자가 입력한 주소(cleanAddr)로 시작하는지 체크
                // 예: "전라남도 광양시 중동 123".startsWith("전라남도 광양시 중동") -> true
                boolean addrMatch = pnuNm.startsWith(cleanAddr) || pnuNm.contains(cleanAddr);
                boolean typeMatch = (examType <= 0) || (type == examType);

                if (addrMatch && typeMatch) {
                    list.add(item);
                }
            }
            return list;
        } catch (Exception e) {
            return List.of();
        }
    }

    private String normalizeAddr(String s) {
        if (s == null)
            return "";
        return s.replaceAll("\\s+", ""); // 공백 제거
    }

    @Transactional
    protected void saveSoilHistory(String regionCd, List<JsonNode> matchedItems) {
        for (JsonNode item : matchedItems) {
            String pnuNm = item.path("PNU_NM").asText("").trim();
            int type = item.path("Exam_Type").asInt(-1);
            System.out.println("saving history: stdgCd=" + regionCd + ", pnuNm=" + pnuNm + ", examType=" + type);

            LandSoilExamEntity entity = toSoilExamEntity(regionCd, item);

            try {
                landSoilExamRepository.save(entity);
            } catch (DataIntegrityViolationException e) {
                // UNIQUE(stdg_cd, exam_day, exam_type, pnu_nm) 충돌이면 그냥 무시
                // (이미 들어있는 데이터)
            }
        }
    }

    @Transactional
    protected void upsertLatestFromHistory(String regionCd, String cleanAddr, int examType, LandSoilExamEntity newest) {
        LandSoilExamLatestEntity latest = landSoilExamLatestRepository
                .findByStdgCdAndPnuNmAndExamType(regionCd, cleanAddr, examType)
                .orElseGet(LandSoilExamLatestEntity::new);

        latest.setStdgCd(regionCd);
        latest.setPnuNm(cleanAddr);
        latest.setExamType((byte) examType);

        latest.setExamDay(newest.getExamDay());
        latest.setAnyYear(newest.getAnyYear());
        latest.setNo(newest.getNo());

        latest.setAcid(newest.getAcid());
        latest.setVldpha(newest.getVldpha());
        latest.setVldsia(newest.getVldsia());
        latest.setOm(newest.getOm());
        latest.setPosifertMg(newest.getPosifertMg());
        latest.setPosifertK(newest.getPosifertK());
        latest.setPosifertCa(newest.getPosifertCa());
        latest.setElcd(newest.getElcd());

        latest.setSourceHistoryId(newest.getId());
        latest.setSyncedAt(LocalDateTime.now());

        landSoilExamLatestRepository.save(latest);
    }

    // -------------------------------
    // 매핑 / 유틸
    // -------------------------------

    private LandSoilExamEntity toSoilExamEntity(String regionCd, JsonNode item) {
        LandSoilExamEntity e = new LandSoilExamEntity();

        e.setStdgCd(regionCd);

        // 주의: 없는 값이면 null
        e.setNo(item.path("No").isMissingNode() ? null : item.path("No").asInt());
        Integer anyYear = item.path("Any_Year").isMissingNode() ? null : item.path("Any_Year").asInt();

        e.setAnyYear(anyYear == null ? null : (short) anyYear.intValue());

        e.setExamDay(item.path("Exam_Day").asText("").trim());
        Integer examType = item.path("Exam_Type").isMissingNode() ? null : item.path("Exam_Type").asInt();
        e.setExamType(examType == null ? null : examType.byteValue());

        e.setPnuNm(item.path("PNU_Nm").asText("").trim());

        e.setAcid(asBigDecimal(item, "ACID"));
        e.setVldpha(asBigDecimal(item, "VLDPHA"));
        e.setVldsia(asBigDecimal(item, "VLDSIA"));
        e.setOm(asBigDecimal(item, "OM"));
        e.setPosifertMg(asBigDecimal(item, "POSIFERT_MG"));
        e.setPosifertK(asBigDecimal(item, "POSIFERT_K"));
        e.setPosifertCa(asBigDecimal(item, "POSIFERT_CA"));
        e.setElcd(asBigDecimal(item, "ELCD"));

        return e;
    }

    private BigDecimal asBigDecimal(JsonNode item, String field) {
        String v = item.path(field).asText(null);
        if (v == null || v.isBlank())
            return null;
        try {
            return new BigDecimal(v.trim());
        } catch (Exception ex) {
            return null;
        }
    }

    private Map<String, String> toSoilResponseMapFromLatest(LandSoilExamLatestEntity e) {
        Map<String, String> m = new LinkedHashMap<>();

        // 프론트 기존 키 유지 (너가 쓰던 LandInfoEntity 응답 형태로 맞춤)
        m.put("regionCd", e.getStdgCd());

        // 의미 매핑(예시)
        m.put("examOmInfo", toStr(e.getOm())); // OM
        m.put("examApInfo", toStr(e.getVldpha())); // 유효인산
        m.put("examKalInfo", toStr(e.getPosifertK())); // K
        m.put("examPhInfo", toStr(e.getAcid())); // pH(ACID)
        m.put("examMgInfo", toStr(e.getPosifertMg())); // Mg
        m.put("examSalInfo", toStr(e.getElcd())); // EC(염류)
        m.put("examCalInfo", toStr(e.getPosifertCa())); // Ca
        m.put("examSalInfo", toStr(e.getElcd())); // 전기전도도
        // 디버깅/검증 필요하면 열어도 됨
        // m.put("examDay", e.getExamDay());
        // m.put("syncedAt", String.valueOf(e.getSyncedAt()));

        return m;
    }

    private String toStr(Object v) {
        return v == null ? "" : String.valueOf(v);
    }

    private boolean isStale(LocalDateTime syncedAt, int ttlDays) {
        if (syncedAt == null)
            return true;
        return syncedAt.isBefore(LocalDateTime.now().minusDays(ttlDays));
    }
}
