package com.avengers.matefarm.insectpestinfo.dto;

import com.avengers.matefarm.insectpestinfo.entity.InsectEntity;

public record InsectBaseDto(
        Long insect_id,
        Long crop_id,
        String crop_name,
        String ncpms_insect_key,
        String insect_species_kor,
        String insect_species,
        String insect_species_code,
        String tgt_vrmn_name,
        String insect_order,
        String insect_family,
        String insect_genus
) {
    public static InsectBaseDto from(InsectEntity e) {
        return new InsectBaseDto(
                e.getId(),
                e.getCrop().getId(),
                e.getCrop().getName(),
                e.getNcpmsInsectKey(),
                e.getInsectSpeciesKor(),
                e.getInsectSpecies(),
                e.getInsectSpeciesCode(),
                e.getTgtVrmnName(),
                e.getInsectOrder(),
                e.getInsectFamily(),
                e.getInsectGenus()
        );
    }
}