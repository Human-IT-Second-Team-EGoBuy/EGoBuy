package com.avengers.matefarm.insectpestinfo.disease.dto;

import com.avengers.matefarm.insectpestinfo.entity.DiseaseDetailEntity;

public record DiseaseDetailDto(
        Long disease_id,
        String infection_route,
        String development_condition,
        String symptoms,
        String prevention_method,
        String biology_prvnbe_mth,
        String chemical_prvnbe_mth,
        String virus_name,
        String etc
) {
    public static DiseaseDetailDto from(DiseaseDetailEntity d) {
        return new DiseaseDetailDto(
                d.getId(),
                d.getInfectionRoute(),
                d.getDevelopmentCondition(),
                d.getSymptoms(),
                d.getPreventionMethod(),
                d.getBiologyPrvnbeMth(),
                d.getChemicalPrvnbeMth(),
                d.getVirusName(),
                d.getEtc()
        );
    }
}