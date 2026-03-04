package com.avengers.matefarm.cropretail.dto.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "category_master")
@Getter
@NoArgsConstructor
public class CategoryEntity {
    
    @Id
    @Column(name = "ctgry_cd", nullable = false)
    private Integer ctgryCd;

    @Column(name = "ctgry_nm", nullable = false, length = 50)
    private String ctgryNm;
}
