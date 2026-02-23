package com.avengers.matefarm.insectpestinfo.insect.repository;

import com.avengers.matefarm.insectpestinfo.entity.InsectEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InsectRepository extends JpaRepository<InsectEntity, Long> {
    Optional<InsectEntity> findByIdAndStatus(Long id, Integer status);
}