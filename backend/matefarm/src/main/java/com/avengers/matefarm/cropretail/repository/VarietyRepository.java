package com.avengers.matefarm.cropretail.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.avengers.matefarm.cropretail.dto.entity.VarietyEntity;

public interface VarietyRepository extends JpaRepository<VarietyEntity, Long> {

    @Query("SELECT v.vrtyNm FROM VarietyEntity v WHERE v.item.itemNm = :itemNm")
    List<String> findVrtyNamesByItemNm(@Param("itemNm") String itemNm);

    @Query(value = "SELECT " +
            "    i.ctgry_cd as ctgryCd, " +
            "    i.item_cd as itemCd, " +
            "    COALESCE(v.vrty_cd, '01') as vrtyCd, " +
            "    CASE WHEN :sggNm IS NULL OR :sggNm = '' THEN '' ELSE r.sgg_cd END as sggCd " +
            "FROM item_master i " +
            "LEFT JOIN variety_master v ON i.item_cd = v.item_cd " +
            "LEFT JOIN region_master r ON (:sggNm IS NOT NULL AND r.sgg_nm = :sggNm) " +
            "WHERE (i.item_nm = :cropNm OR v.vrty_nm = :cropNm) " + // ✅ 품목명 또는 품종명 매칭
            "ORDER BY (CASE WHEN v.vrty_nm = :cropNm THEN 0 ELSE 1 END), v.vrty_cd ASC " + // ✅ 품종명이 정확히 일치하는 것을 최우선순위로
            "LIMIT 1", nativeQuery = true)
    Optional<ICodeProjection> findCodesByNames(@Param("cropNm") String cropNm, @Param("sggNm") String sggNm);

    interface ICodeProjection {
        String getCtgryCd();

        String getItemCd();

        String getVrtyCd();

        String getSggCd();
    }
}