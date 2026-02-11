package com.avengers.matefarm.map.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "land_trade_history", indexes = {
    @Index(name = "idx_lawd_ymd", columnList = "lawdCd, dealYmd"),
    @Index(name = "idx_jimok", columnList = "jimok")
})
@Getter 
@Setter
@NoArgsConstructor
public class TradeHistoryEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 5, nullable = false)
    private String lawdCd;     // 법정동코드 앞 5자리

    @Column(length = 6, nullable = false)
    private String dealYmd;    // 계약년월 (YYYYMM)

    private String sggNm;      // 시군구명
    private String umdNm;      // 읍면동명
    private String jibun;      // 지번 (마스킹 포함)

    private Integer dealAmount; // 거래금액 (만원)
    private Double dealArea;    // 면적 (㎡)
    
    private Integer dealYear;
    private Integer dealMonth;
    private Integer dealDay;

    private String jimok;       // 지목 (전, 답, 과 등)
    private String landUse;     // 용도지역
    private String dealingGbn;  // 중개거래/직거래
}
