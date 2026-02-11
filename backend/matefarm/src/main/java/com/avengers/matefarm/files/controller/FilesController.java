package com.avengers.matefarm.files.controller;

import com.avengers.matefarm.common.ResponseDTO;
import com.avengers.matefarm.files.dto.request.FilesUploadRequestDTO;
import com.avengers.matefarm.files.dto.request.SingleFileUploadRequestDTO;
import com.avengers.matefarm.files.dto.response.FilesResponseDTO;
import com.avengers.matefarm.files.dto.response.SingleFileUploadResponseDTO;
import com.avengers.matefarm.files.enums.OwnerType;
import com.avengers.matefarm.files.service.FilesService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@RestController("FilesController")
@RequestMapping("/api/files")
public class FilesController {

    private final FilesService filesService;

    public FilesController(FilesService filesService) {
        this.filesService = filesService;
    }

    /* S3에 첨부파일 업로드 */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE) // MULTIPART_FORM_DATA_VALUE 를 사용할 시에는 Json Bodt만 받는 @RequestBody 가 아닌 @ModelAttribute 사용
    public ResponseDTO<List<FilesResponseDTO>> uploadFiles(
            @ModelAttribute FilesUploadRequestDTO filesUploadRequestDTO
    ) {
        log.info("업로드 요청: ownerId={}, ownerType={}", filesUploadRequestDTO.getOwnerId(), filesUploadRequestDTO.getOwnerType());
        List<FilesResponseDTO> filesUploadResponseDTO =
                filesService.
                        uploadFiles(filesUploadRequestDTO.getFiles(),
                                filesUploadRequestDTO.getOwnerType(),
                                filesUploadRequestDTO.getOwnerId());

        return ResponseDTO.ok(filesUploadResponseDTO);
    }

    /* 파일 단건 삭제 */
    @DeleteMapping("/{fileId}")
    public ResponseDTO<Void> deleteFile(
            @PathVariable Long fileId
    ) {

        filesService.deleteFile(fileId);

        return ResponseDTO.ok(null);
    }

    /* 첨부 파일 조회 - 공지사항 조회용 */
    @GetMapping("/{ownerType}/{ownerId}")
    public ResponseDTO<List<FilesResponseDTO>> getFiles(
            @PathVariable("ownerType") OwnerType ownerType,
            @PathVariable("ownerId") Long ownerId
            ) {

        List<FilesResponseDTO> responseDTO =
                filesService.
                        getFilesWithOwnerTypeAndOwnerId(
                                ownerType,
                                ownerId);

        return ResponseDTO.ok(responseDTO);

    }

    /* 게시글 작성 중 본문에 삽입할 이미지 전용 - S3에만 업로드 할 때 사용하는 메소드 */
    @PostMapping(value = "/editor-upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseDTO<SingleFileUploadResponseDTO> uploadEditorImage(
            @RequestPart MultipartFile file  // 이름은 사용할 editor에 맞춰서 수정. editor 호환성 때문에 @RequestPart 사용
    ) {

        SingleFileUploadResponseDTO image =
                filesService.
                        uploadSingleImage(file);

        return ResponseDTO.ok(image);
    }

    /* 이미지를 포함한 게시글 생성 성공 시, files의 OwnerId 및 OwnerType을 변경하는 API */
}

/*
* [글 작성 중]
이미지 붙여넣기
   ↓
POST /files/editor-image
   ↓
S3 업로드 (TEMP)
   ↓
CloudFront URL 반환
   ↓
<img src="..."> 본문에 삽입

[글 저장]
POST /notices
   ↓
noticeId 생성
   ↓
본문 이미지 UUID 추출
   ↓
files.ownerId = noticeId 업데이트

*
*
*
* */