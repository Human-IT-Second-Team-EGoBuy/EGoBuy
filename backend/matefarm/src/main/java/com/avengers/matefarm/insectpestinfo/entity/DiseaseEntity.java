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

    @Column(name = "sick_name_kor", nullable = false)
    private String pestName;

    @Column(name = "status", nullable = false)
    private Integer status;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
