package com.avengers.matefarm.insectpestinfo.entity;

import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDateTime;

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

    @Column(name = "insect_species_kor")
    private String pestName;

    @Column(name = "status", nullable = false)
    private Integer status;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
