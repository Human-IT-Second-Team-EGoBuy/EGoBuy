package com.avengers.matefarm.map.dto.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(
    name = "land_soil_exam",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uq_land_soil_exam",
            columnNames = {"stdg_cd", "exam_day", "exam_type", "pnu_nm"}
        )
    },
    indexes = {
        @Index(name = "idx_land_soil_exam_stdg_day", columnList = "stdg_cd, exam_day"),
        @Index(name = "idx_land_soil_exam_pnu", columnList = "pnu_nm")
    }
)
@Getter
@Setter
public class LandSoilExamEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "no")
    private Integer no;

    @Column(name = "stdg_cd", length = 10, nullable = false)
    private String stdgCd;

    @Column(name = "any_year")
    private Short anyYear;

    @Column(name = "exam_day", length = 8, nullable = false)
    private String examDay;

    @Column(name = "exam_type", nullable = false)
    private Byte examType;

    @Column(name = "pnu_nm", length = 255, nullable = false)
    private String pnuNm;

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

    @Column(name = "created_at", nullable = false, updatable = false, insertable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false, insertable = false)
    private LocalDateTime updatedAt;
}

