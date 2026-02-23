package com.avengers.matefarm.insectpestinfo.entity;

import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "disease")
public class DiseaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "disease_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "crop_id", nullable = false)
    private CropEntity crop;

    @Column(name = "ncpms_sick_key")
    private String ncpmsSickKey;

    @Column(name = "sick_name_kor", nullable = false)
    private String sickNameKor;

    @Column(name = "sick_name_eng")
    private String sickNameEng;

    @Column(name = "sick_name_chn")
    private String sickNameChn;

    @Column(name = "sort_order2")
    private Integer sortOrder2;

    @Column(name = "status", nullable = false)
    private Integer status;

    // DB에서 자동 관리하는 경우 안전하게
    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;
}