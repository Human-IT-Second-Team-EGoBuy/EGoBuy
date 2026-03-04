package com.avengers.matefarm.cropretail.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.avengers.matefarm.cropretail.dto.entity.CategoryEntity;

public interface CategoryRepository extends JpaRepository<CategoryEntity, Integer> {

    @Query("SELECT c.ctgryNm FROM CategoryEntity c ORDER BY c.ctgryCd ASC")
    List<String> findAllCategoryNames();
}
