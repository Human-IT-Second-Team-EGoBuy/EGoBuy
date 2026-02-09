package com.avengers.matefarm.files.dto.request;

import com.avengers.matefarm.files.enums.OwnerType;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@Setter     // MultipartFile 사용을 위헤 @ModelAttribute 를 사용하는 경우 @Setter를 추가해야 files 변수에 파일 값 주입 가능.
public class FilesUploadRequestDTO {

    @JsonProperty("owner_type")
    private OwnerType ownerType;

    @JsonProperty("owner_id")
    private Long ownerId;

    private List<MultipartFile> files;


}
