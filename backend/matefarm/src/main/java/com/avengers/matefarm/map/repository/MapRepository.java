package com.avengers.matefarm.map.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.avengers.matefarm.map.dto.entity.RegCodeEntity;

import io.lettuce.core.dynamic.annotation.Param;

import java.util.List;
import java.util.Optional;

public interface MapRepository extends JpaRepository<RegCodeEntity, String> {

    Optional<RegCodeEntity> findRegionCdByLocataddNm(String locatadd_nm);

    Optional<RegCodeEntity> findLawdCdByLocataddNm(String locatadd_nm);

    List<RegCodeEntity> findByLocathighCdOrderByLocatOrderAsc(String locathighCd);

    List<RegCodeEntity> findByLocathighCd(String locathighCd);

    List<RegCodeEntity> findByRegionCdIn(List<String> regionCodes);

    @Query(value = "SELECT * FROM lawd_code " +
                   "WHERE sido_cd = '41' " +
                   "AND SUBSTR(lawd_cd, 5, 1) = '0' " +
                   "AND umd_cd = '000' " +
                   "AND region_cd != '4100000000' " +
                   "ORDER BY locat_order ASC", nativeQuery = true)
    List<RegCodeEntity> findCitiesByGyeonggi();

    @Query(value = "SELECT * FROM lawd_code " +
                   "WHERE lawd_cd LIKE CONCAT(:cityPrefix, '%') " +
                   "AND SUBSTR(lawd_cd, 5, 1) != '0' " +
                   "AND umd_cd = '000' " +
                   "ORDER BY locat_order ASC", nativeQuery = true)
    List<RegCodeEntity> findDistrictsByCity(@Param("cityPrefix") String cityPrefix);
   
}