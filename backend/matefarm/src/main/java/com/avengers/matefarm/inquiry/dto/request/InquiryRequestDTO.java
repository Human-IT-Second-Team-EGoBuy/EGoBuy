package com.avengers.matefarm.inquiry.dto.request;

import com.avengers.matefarm.inquiry.enums.InquiryStatus;
import com.avengers.matefarm.inquiry.enums.InquiryType;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class InquiryRequestDTO {

    @JsonProperty("inquiry_title")
    private String inquiryTitle;

    @JsonProperty("inquiry_content")
    private String inquiryContent;

    @JsonProperty("inquiry_type")
    private InquiryType inquiryType;

}
