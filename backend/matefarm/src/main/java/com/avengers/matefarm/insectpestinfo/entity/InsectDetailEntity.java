package com.avengers.matefarm.insectpestinfo.entity;

import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "insect_detail")
public class InsectDetailEntity {

    @Id
    @Column(name = "insect_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "insect_id")
    private InsectEntity insect;

    @Column(name = "distrb_info", columnDefinition = "TEXT") 
    private String distrbInfo;

    @Column(name = "stle_info", columnDefinition = "TEXT") 
    private String stleInfo;

    @Column(name = "ecology_info", columnDefinition = "TEXT") 
    private String ecologyInfo;

    @Column(name = "damage_info", columnDefinition = "TEXT") 
    private String damageInfo;

    @Column(name = "qrant_info", columnDefinition = "TEXT") 
    private String qrantInfo;

    @Column(name = "prevent_method", columnDefinition = "TEXT") 
    private String preventMethod;

    @Column(name = "biology_prvnbe_mth", columnDefinition = "TEXT") 
    private String biologyPrvnbeMth;

    @Column(name = "chemical_prvnbe_mth", columnDefinition = "TEXT") 
    private String chemicalPrvnbeMth;
    
    @Column(name = "etc", columnDefinition = "TEXT") 
    private String etc;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false, insertable = false, updatable = false)
    private LocalDateTime updatedAt;
}