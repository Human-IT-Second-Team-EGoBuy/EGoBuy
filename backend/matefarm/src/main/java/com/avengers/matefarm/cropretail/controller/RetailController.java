package com.avengers.matefarm.cropretail.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.avengers.matefarm.common.ResponseDTO;
import com.avengers.matefarm.cropretail.service.CategoryService;
import com.avengers.matefarm.cropretail.service.RetailService;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/api/retail")
@RequiredArgsConstructor
public class RetailController {

    private final RetailService retailService;
    private final CategoryService categoryService;

    @GetMapping("/open")
    public ResponseDTO<List<String>> getCategories() {
        return ResponseDTO.ok(categoryService.getCategory());
    }

    @GetMapping("/sgg-open")
    public ResponseDTO<List<String>> getSggName() {
        return ResponseDTO.ok(categoryService.getSggName());
    }
    
    @GetMapping("/item-open")
    public ResponseDTO<List<String>> getItems(@RequestParam String ctgryNm) {
        return ResponseDTO.ok(categoryService.getItemByCategory(ctgryNm));
    }

    @GetMapping("/variety-open")
    public ResponseDTO<List<String>> getVarieties(@RequestParam String itemNm) {
        return ResponseDTO.ok(categoryService.getVarietyByItem(itemNm));
    }
    
    @GetMapping("/search")
    public Mono<ResponseDTO<Object>> getRetailData(
        @RequestParam String toDate,
        @RequestParam String regionNm, 
        @RequestParam String cropNm,
        @RequestParam String filter) {

        return retailService.getRetailData(toDate,regionNm, cropNm, filter)
                .map(ResponseDTO::ok);
    }

    @GetMapping("/avg")
    public Mono<ResponseDTO<Object>> getRetailAvgData(
        @RequestParam String toDate,
        @RequestParam String cropNm,
        @RequestParam String filter) {

        return retailService.getRetailAvgData(toDate, cropNm, filter);
    }
    
}
