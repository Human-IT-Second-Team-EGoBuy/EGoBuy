package com.avengers.matefarm.insectpestinfo.entity;

import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDateTime;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Getter
@Entity
@Table(name = "insect")
public class InsectEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "insect_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "crop_id", nullable = false)
    private CropEntity crop;

    @Column(name = "ncpms_insect_key")
    private String ncpmsInsectKey;

    @Column(name = "insect_species_kor")
    private String insectSpeciesKor;

    @Column(name = "insect_species")
    private String insectSpecies;

    @Column(name = "insect_species_code")
    private String insectSpeciesCode;

    @Column(name = "tgt_vrmn_name")
    private String tgtVrmnName;

    @Column(name = "insect_order")
    private String insectOrder;

    @Column(name = "insect_family")
    private String insectFamily;

    @Column(name = "insect_genus")
    private String insectGenus;

    @JdbcTypeCode(SqlTypes.TINYINT)
    @Column(name = "status", nullable = false)
    private Byte status;

    @Column(name = "updated_at", nullable = false, insertable = false, updatable = false)
    private LocalDateTime updatedAt;
}