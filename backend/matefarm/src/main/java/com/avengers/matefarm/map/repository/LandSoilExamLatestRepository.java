package com.avengers.matefarm.map.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.avengers.matefarm.map.dto.entity.LandSoilExamLatestEntity;

public interface LandSoilExamLatestRepository extends JpaRepository<LandSoilExamLatestEntity, Long> {

    Optional<LandSoilExamLatestEntity> findByStdgCdAndPnuNmAndExamType(
            String stdgCd, String pnuNm, Integer examType);
}
