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
@Table(name = "variety_master")
@Getter
@NoArgsConstructor
public class VarietyEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_cd")
    private ItemEntity item;

    @Id
    @Column(name = "vrty_cd", length = 10, nullable = false)
    private Integer vrtyCd;

    @Column(name = "vrty_nm", length = 100)
    private String vrtyNm;

}
