package com.avengers.matefarm.map.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(
    name = "land_soil_exam_latest",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uq_land_soil_exam_latest",
            columnNames = {"stdg_cd", "exam_type", "pnu_nm"}
        )
    },
    indexes = {
        @Index(name = "idx_land_soil_latest_stdg", columnList = "stdg_cd"),
        @Index(name = "idx_land_soil_latest_synced", columnList = "synced_at")
    }
)
@Getter
@Setter
public class LandSoilExamLatestEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "stdg_cd", length = 10, nullable = false)
    private String stdgCd;

    @Column(name = "exam_type", nullable = false)
    private Byte examType;

    @Column(name = "pnu_nm", length = 255, nullable = false)
    private String pnuNm;

    @Column(name = "exam_day", length = 8, nullable = false)
    private String examDay;

    @Column(name = "any_year")
    private Short anyYear;

    @Column(name = "no")
    private Integer no;

    @Column(name = "acid", precision = 6, scale = 3)
    private BigDecimal acid;

    @Column(name = "vldpha", precision = 10, scale = 3)
    private BigDecimal vldpha;

    @Column(name = "vldsia", precision = 10, scale = 3)
    private BigDecimal vldsia;

    @Column(name = "om", precision = 10, scale = 3)
    private BigDecimal om;

    @Column(name = "posifert_mg", precision = 10, scale = 3)
    private BigDecimal posifertMg;

    @Column(name = "posifert_k", precision = 10, scale = 3)
    private BigDecimal posifertK;

    @Column(name = "posifert_ca", precision = 10, scale = 3)
    private BigDecimal posifertCa;

    @Column(name = "elcd", precision = 10, scale = 3)
    private BigDecimal elcd;

    @Column(name = "source_history_id")
    private Long sourceHistoryId;

    @Column(name = "synced_at", nullable = false)
    private LocalDateTime syncedAt;

    @Column(name = "created_at", nullable = false, updatable = false, insertable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false, insertable = false)
    private LocalDateTime updatedAt;
}
