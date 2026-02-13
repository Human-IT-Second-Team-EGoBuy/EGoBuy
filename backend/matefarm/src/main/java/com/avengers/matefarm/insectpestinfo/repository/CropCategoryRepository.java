package com.avengers.matefarm.insectpestinfo.repository;

import com.avengers.matefarm.insectpestinfo.entity.CropCategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CropCategoryRepository extends JpaRepository<CropCategoryEntity, Long> {
    List<CropCategoryEntity> findByStatusOrderByNameAsc(Integer status);
}
