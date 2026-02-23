package com.avengers.matefarm.insectpestinfo.repository;

import com.avengers.matefarm.insectpestinfo.entity.CropEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface PestIssueRepository extends JpaRepository<CropEntity, Long> {

    interface PestIssueRowView {
        String getPestType();
        Long getPestId();
        Long getCropId();
        String getCropName();
        String getPestName();
        LocalDateTime getUpdatedAt();
    }

    @Query(
        value = """
            SELECT
                t.pestType  AS pestType,
                t.pestId    AS pestId,
                t.cropId    AS cropId,
                t.cropName  AS cropName,
                t.pestName  AS pestName,
                t.updatedAt AS updatedAt
            FROM (
                SELECT
                    'insect' AS pestType,
                    i.insect_id AS pestId,
                    c.crop_id AS cropId,
                    c.crop_name AS cropName,
                    i.insect_species_kor AS pestName,
                    i.updated_at AS updatedAt,
                    cc.category_id AS categoryId
                FROM insect i
                JOIN crop c ON c.crop_id = i.crop_id
                JOIN crop_categories cc ON cc.category_id = c.category_id
                WHERE i.status = 1

                UNION ALL

                SELECT
                    'disease' AS pestType,
                    d.disease_id AS pestId,
                    c.crop_id AS cropId,
                    c.crop_name AS cropName,
                    d.sick_name_kor AS pestName,
                    d.updated_at AS updatedAt,
                    cc.category_id AS categoryId
                FROM disease d
                JOIN crop c ON c.crop_id = d.crop_id
                JOIN crop_categories cc ON cc.category_id = c.category_id
                WHERE d.status = 1
            ) t
            WHERE (:categoryId IS NULL OR t.categoryId = :categoryId)
              AND (:ptype IS NULL OR t.pestType = :ptype)
              AND (
                    :q IS NULL
                    OR t.cropName LIKE CONCAT('%', :q, '%')
                    OR t.pestName LIKE CONCAT('%', :q, '%')
              )
            ORDER BY t.cropName ASC, t.pestName ASC
            """,
        countQuery = """
            SELECT COUNT(*)
            FROM (
                SELECT
                    'insect' AS pestType,
                    i.insect_id AS pestId,
                    c.crop_id AS cropId,
                    c.crop_name AS cropName,
                    i.insect_species_kor AS pestName,
                    i.updated_at AS updatedAt,
                    cc.category_id AS categoryId
                FROM insect i
                JOIN crop c ON c.crop_id = i.crop_id
                JOIN crop_categories cc ON cc.category_id = c.category_id
                WHERE i.status = 1

                UNION ALL

                SELECT
                    'disease' AS pestType,
                    d.disease_id AS pestId,
                    c.crop_id AS cropId,
                    c.crop_name AS cropName,
                    d.sick_name_kor AS pestName,
                    d.updated_at AS updatedAt,
                    cc.category_id AS categoryId
                FROM disease d
                JOIN crop c ON c.crop_id = d.crop_id
                JOIN crop_categories cc ON cc.category_id = c.category_id
                WHERE d.status = 1
            ) t
            WHERE (:categoryId IS NULL OR t.categoryId = :categoryId)
              AND (:ptype IS NULL OR t.pestType = :ptype)
              AND (
                    :q IS NULL
                    OR t.cropName LIKE CONCAT('%', :q, '%')
                    OR t.pestName LIKE CONCAT('%', :q, '%')
              )
            """,
        nativeQuery = true
    )
    Page<PestIssueRowView> findPestIssues(
        @Param("categoryId") Long categoryId,
        @Param("ptype") String ptype,
        @Param("q") String q,
        Pageable pageable
    );
}
