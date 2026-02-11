package com.avengers.matefarm.map.repository;

import com.avengers.matefarm.map.entity.TradeHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TradeHistoryRepository extends JpaRepository<TradeHistoryEntity, Long> {
    List<TradeHistoryEntity> findByLawdCd(String lawdCd);
    /**
     * 1. DB 캐시 확인용: 특정 지역과 특정 월의 데이터가 이미 수집되었는지 확인
     */
    List<TradeHistoryEntity> findByLawdCdAndDealYmd(String lawdCd, String dealYmd);

    /**
     * 2. 농지 필터링용: 특정 지역의 데이터 중 지목이 '전', '답', '과'인 데이터만 조회
     */
    List<TradeHistoryEntity> findByLawdCdAndJimokIn(String lawdCd, List<String> jimoks);

    /**
     * 3. 최근 데이터 조회용: 특정 지역의 데이터를 최근 날짜 순으로 조회
     */
    List<TradeHistoryEntity> findByLawdCdOrderByDealYearDescDealMonthDescDealDayDesc(String lawdCd);
}
