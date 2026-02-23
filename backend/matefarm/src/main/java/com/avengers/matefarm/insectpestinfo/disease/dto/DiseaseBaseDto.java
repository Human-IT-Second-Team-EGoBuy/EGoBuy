package com.avengers.matefarm.insectpestinfo.disease.dto;

import com.avengers.matefarm.insectpestinfo.entity.DiseaseEntity;

public record DiseaseBaseDto(
        Long disease_id,
        Long crop_id,
        String crop_name,
        String ncpms_sick_key,
        String sick_name_kor,
        String sick_name_eng,
        String sick_name_chn,
        Integer sort_order2
) {
    public static DiseaseBaseDto from(DiseaseEntity e) {
        return new DiseaseBaseDto(
                e.getId(),
                e.getCrop().getId(),
                e.getCrop().getName(),
                e.getNcpmsSickKey(),
                e.getSickNameKor(),
                e.getSickNameEng(),
                e.getSickNameChn(),
                e.getSortOrder2()
        );
    }
}