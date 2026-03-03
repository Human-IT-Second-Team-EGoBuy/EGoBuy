package com.avengers.matefarm.insectpestinfo.entity;

import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "disease_detail")
public class DiseaseDetailEntity {

    @Id
    @Column(name = "disease_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "disease_id")
    private DiseaseEntity disease;

    @Column(name = "infection_route")
    private String infectionRoute;

    @Column(name = "development_condition")
    private String developmentCondition;

    @Column(name = "symptoms")
    private String symptoms;

    @Column(name = "prevention_method")
    private String preventionMethod;

    @Column(name = "biology_prvnbe_mth")
    private String biologyPrvnbeMth;

    @Column(name = "chemical_prvnbe_mth")
    private String chemicalPrvnbeMth;

    @Column(name = "virus_name")
    private String virusName;

    @Column(name = "etc")
    private String etc;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false, insertable = false, updatable = false)
    private LocalDateTime updatedAt;
}