package com.avengers.matefarm.cropretail.dto.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "region_master")
@Getter
@NoArgsConstructor
public class RegionEntity {

    @Id
    @Column(name = "sgg_cd", nullable = false)
    private Integer sggCd;

    @Column(name = "sgg_nm", nullable = false, length = 50)
    private String sggNm;
}
