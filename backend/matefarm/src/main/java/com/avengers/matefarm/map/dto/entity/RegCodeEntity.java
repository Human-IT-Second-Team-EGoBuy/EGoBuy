package com.avengers.matefarm.map.dto.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "lawd_code")
public class RegCodeEntity {

    @Id
    @Column(name = "region_cd", length = 10, nullable = false)
    private String regionCd;

    @Column(name = "lawd_cd", length = 5, nullable = false)
    private String lawdCd;

    @Column(name = "sido_cd", length = 2, nullable = false)
    private String sidoCd;

    @Column(name = "sgg_cd", length = 3, nullable = false)
    private String sggCd;

    @Column(name = "umd_cd", length = 3, nullable = false)
    private String umdCd;

    @Column(name = "ri_cd", length = 2, nullable = false)
    private String riCd;

    @Column(name = "locatadd_nm", length = 120, nullable = false)
    private String locataddNm;

    @Column(name = "locallow_nm", length = 60, nullable = false)
    private String locallowNm;

    @Column(name = "locathigh_cd", length = 10, nullable = false)
    private String locathighCd;

    @Column(name = "locat_order")
    private Integer locatOrder;

    @Column(name = "adpt_de", length = 8)
    private String adptDe;

    @Column(name = "locat_rm", length = 200)
    private String locatRm;
}


