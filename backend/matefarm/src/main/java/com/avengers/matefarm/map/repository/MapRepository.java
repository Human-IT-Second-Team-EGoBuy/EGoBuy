package com.avengers.matefarm.map.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.avengers.matefarm.map.entity.LandInfoEntity;
import com.avengers.matefarm.map.entity.RegCodeEntity;

import io.lettuce.core.dynamic.annotation.Param;

import java.util.List;
import java.util.Optional;

public interface MapRepository extends JpaRepository<RegCodeEntity, String> {

    Optional<RegCodeEntity> findRegionCdByLocataddNm(String locatadd_nm);

    Optional<RegCodeEntity> findLawdCdByLocataddNm(String locatadd_nm);

    List<RegCodeEntity> findByLocathighCdOrderByLocatOrderAsc(String locathighCd);

    List<RegCodeEntity> findByLocathighCd(String locathighCd);

    List<RegCodeEntity> findByRegionCdIn(List<String> regionCodes);

   
}