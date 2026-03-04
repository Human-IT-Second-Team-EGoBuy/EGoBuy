package com.avengers.matefarm.cropretail.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class KamisRequestDTO {
    private String startDate;
    private String endDate;
    private String filter;
    private String ctgryCd;
    private String itemCd;
    private String vrtyCd;
    private String sggCd;
}
