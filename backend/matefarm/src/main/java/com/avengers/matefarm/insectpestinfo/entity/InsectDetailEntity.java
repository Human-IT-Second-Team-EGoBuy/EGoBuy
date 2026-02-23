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

    @Lob @Column(name = "distrb_info") private String distrbInfo;

    @Lob @Column(name = "stle_info") private String stleInfo;

    @Lob @Column(name = "ecology_info") private String ecologyInfo;

    @Lob @Column(name = "damage_info") private String damageInfo;

    @Lob @Column(name = "qrant_info") private String qrantInfo;

    @Lob @Column(name = "prevent_method") private String preventMethod;

    @Lob @Column(name = "biology_prvnbe_mth") private String biologyPrvnbeMth;

    @Lob @Column(name = "chemical_prvnbe_mth") private String chemicalPrvnbeMth;
    
    @Lob @Column(name = "etc") private String etc;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;
}