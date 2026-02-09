package com.avengers.matefarm.files.repository;

import com.avengers.matefarm.files.dto.FilesEntity;
import com.avengers.matefarm.files.dto.response.FilesResponseDTO;
import com.avengers.matefarm.files.enums.OwnerType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FilesRepository extends JpaRepository<FilesEntity, Long> {

    List<FilesEntity> findFilesByOwnerTypeAndOwnerId(OwnerType ownerType, Long ownerId);

    Optional<FilesEntity> findById(Long fileId);
}
