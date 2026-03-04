package com.avengers.matefarm.cropretail.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.avengers.matefarm.cropretail.dto.entity.RegionEntity;

public interface RegionRepository extends JpaRepository<RegionEntity, Integer> {
    
    @Query("SELECT r.sggNm FROM RegionEntity r")
    List<String> findAllSggNm();
}
