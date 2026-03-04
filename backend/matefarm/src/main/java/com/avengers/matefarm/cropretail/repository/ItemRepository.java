package com.avengers.matefarm.cropretail.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.avengers.matefarm.cropretail.dto.entity.ItemEntity;

import io.lettuce.core.dynamic.annotation.Param;

public interface ItemRepository extends JpaRepository<ItemEntity, Integer> {
    
    @Query("SELECT i.itemNm FROM ItemEntity i WHERE i.category.ctgryNm = :ctgryNm")
    List<String> findItemNamesByCtgryNm(@Param("ctgryNm") String ctgryNm);
}
