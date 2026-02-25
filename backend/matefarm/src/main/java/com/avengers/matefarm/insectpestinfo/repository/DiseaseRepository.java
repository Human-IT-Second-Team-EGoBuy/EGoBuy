package com.avengers.matefarm.insectpestinfo.repository;

import com.avengers.matefarm.insectpestinfo.entity.DiseaseEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DiseaseRepository extends JpaRepository<DiseaseEntity, Long> {
    Optional<DiseaseEntity> findByIdAndStatus(Long id, Integer status);
}