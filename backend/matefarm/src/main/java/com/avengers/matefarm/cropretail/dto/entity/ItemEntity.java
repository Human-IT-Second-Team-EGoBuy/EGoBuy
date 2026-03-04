package com.avengers.matefarm.cropretail.dto.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "item_master")
@Getter
@NoArgsConstructor
public class ItemEntity {
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ctgry_cd")
    private CategoryEntity category;

    @Id
    @Column(name = "item_cd", nullable = false)
    private Integer itemCd;

    @Column(name = "item_nm", nullable = false, length = 50)
    private String itemNm;


}
