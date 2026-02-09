package com.avengers.matefarm.notice.controller;

import com.avengers.matefarm.common.PageResponseDTO;
import com.avengers.matefarm.common.ResponseDTO;
import com.avengers.matefarm.files.dto.response.FilesResponseDTO;
import com.avengers.matefarm.files.service.FilesService;
import com.avengers.matefarm.notice.dto.request.NoticeUploadRequestDTO;
import com.avengers.matefarm.notice.dto.response.NoticeDetailResponseDTO;
import com.avengers.matefarm.notice.dto.response.NoticeResponseDTO;
import com.avengers.matefarm.notice.dto.response.NoticeUploadResponseDTO;
import com.avengers.matefarm.notice.service.NoticeService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


@RestController("NoticeController")
@RequestMapping("/api/notice")
public class NoticeController {

    private final NoticeService noticeService;

    public NoticeController(NoticeService noticeService) {

        this.noticeService = noticeService;
    }

    /* 공지사항 생성 */
    @PostMapping("/upload-post")
    public ResponseDTO<NoticeUploadResponseDTO> createNotice(
            @ModelAttribute NoticeUploadRequestDTO noticeUploadRequestDTO ) {

        NoticeUploadResponseDTO noticeUploadResponseDTO =
                noticeService.
                        createNotice(noticeUploadRequestDTO);

        return ResponseDTO.ok(noticeUploadResponseDTO);
    }

    /* 공지사항 삭제 */
    @DeleteMapping("/{noticeId}")
    public ResponseDTO<Void> deleteNotice(
            @PathVariable("noticeId") Long noticeId) {

        noticeService.deleteNotice(noticeId);

        return ResponseDTO.ok(null);
    }

    /* 공지사항 수정 */

    /* 공지사항 단건 조회 */
    @GetMapping("/{noticeId}")
    public ResponseDTO<NoticeDetailResponseDTO> getDetailedNotice(
            @PathVariable("noticeId") Long noticeId
    ) {

        NoticeDetailResponseDTO responseDTO =
                noticeService.
                        getDetailedNotice(noticeId);

        return ResponseDTO.ok(responseDTO);

    }

    /* 공지사항 List 조회 */
    @GetMapping
    public ResponseDTO<PageResponseDTO<NoticeResponseDTO>> getNotices(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {

        // Jpa의 Pageable은 LIMIT 10 OFFSET 500 같은 쿼리를 사용하여, 모든 요소를 반환하지 않음.
        PageResponseDTO<NoticeResponseDTO> pages = noticeService.getNoticeList(pageable);
        return ResponseDTO.ok(pages);

    }
}
