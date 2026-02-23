package com.avengers.matefarm.insectpestinfo.insect.dto;

import com.avengers.matefarm.insectpestinfo.entity.InsectDetailEntity;

public record InsectDetailDto(
        Long insect_id,
        String distrb_info,
        String stle_info,
        String ecology_info,
        String damage_info,
        String qrant_info,
        String prevent_method,
        String biology_prvnbe_mth,
        String chemical_prvnbe_mth,
        String etc
) {
    public static InsectDetailDto from(InsectDetailEntity d) {
        return new InsectDetailDto(
                d.getId(),
                d.getDistrbInfo(),
                d.getStleInfo(),
                d.getEcologyInfo(),
                d.getDamageInfo(),
                d.getQrantInfo(),
                d.getPreventMethod(),
                d.getBiologyPrvnbeMth(),
                d.getChemicalPrvnbeMth(),
                d.getEtc()
        );
    }
}