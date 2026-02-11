package com.avengers.matefarm.map.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.avengers.matefarm.map.entity.LandInfoEntity;

public interface LandInfoRepository extends JpaRepository<LandInfoEntity, Long>{

     Optional<LandInfoEntity> findByRegionCd(String regionCd);
}
