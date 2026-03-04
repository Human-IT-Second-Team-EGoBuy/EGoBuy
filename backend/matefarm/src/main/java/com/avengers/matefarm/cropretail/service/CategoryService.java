package com.avengers.matefarm.cropretail.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.avengers.matefarm.cropretail.repository.CategoryRepository;
import com.avengers.matefarm.cropretail.repository.ItemRepository;
import com.avengers.matefarm.cropretail.repository.RegionRepository;
import com.avengers.matefarm.cropretail.repository.VarietyRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepo;
    private final ItemRepository itemRepo;
    private final VarietyRepository varietyRepo;
    private final RegionRepository regionRepo;

    public List<String> getCategory() {
        return categoryRepo.findAllCategoryNames();
    }

    public List<String> getItemByCategory(String ctgryNm) {
        return itemRepo.findItemNamesByCtgryNm(ctgryNm);
    }

    public List<String> getVarietyByItem(String itemNm) {
        return varietyRepo.findVrtyNamesByItemNm(itemNm);
    }

    public List<String> getSggName() {
        return regionRepo.findAllSggNm();
    }

}
