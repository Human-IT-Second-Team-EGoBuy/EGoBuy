package com.avengers.matefarm.map.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.avengers.matefarm.map.entity.LandSoilExamEntity;

public interface LandSoilExamRepository extends JpaRepository<LandSoilExamEntity, Long> {

    Optional<LandSoilExamEntity> findFirstByStdgCdAndPnuNmAndExamTypeOrderByExamDayDescIdDesc(
            String stdgCd, String pnuNm, Integer examType);

    List<LandSoilExamEntity> findByStdgCdAndPnuNmAndExamTypeOrderByExamDayDesc(
            String stdgCd, String pnuNm, Integer examType);
}
