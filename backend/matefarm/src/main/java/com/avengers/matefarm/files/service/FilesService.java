package com.avengers.matefarm.files.service;

import com.avengers.matefarm.files.dto.response.FilesResponseDTO;
import com.avengers.matefarm.files.enums.OwnerType;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface FilesService {

    /* 파일 업로드 하는 모든 메소드가 공용으로 사용하는 업로드할 파일의 MIME 타입을 검사하는 메소드 */
    void validationCheck(List<MultipartFile> files);

    /* 업로드할 파일의 MIME 타입을 검사할 메소드 */
    List<FilesResponseDTO> uploadFiles(List<MultipartFile> files,
                                       OwnerType ownerType,
                                       Long ownerId);

    List<FilesResponseDTO> getFilesWithOwnerTypeAndOwnerId(OwnerType ownerType, Long ownerId);

    void deleteFile(Long fileId);
}
