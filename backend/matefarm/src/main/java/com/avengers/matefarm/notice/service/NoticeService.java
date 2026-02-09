package com.avengers.matefarm.notice.service;

import com.avengers.matefarm.common.PageResponseDTO;
import com.avengers.matefarm.notice.dto.request.NoticeUploadRequestDTO;
import com.avengers.matefarm.notice.dto.response.NoticeDetailResponseDTO;
import com.avengers.matefarm.notice.dto.response.NoticeResponseDTO;
import com.avengers.matefarm.notice.dto.response.NoticeUploadResponseDTO;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

/*
 *   참조 방향 : NoticeServiceImpl -> FilesService
* */

public interface NoticeService {
    NoticeUploadResponseDTO createNotice(NoticeUploadRequestDTO noticeUploadRequestDTO);

    NoticeDetailResponseDTO getDetailedNotice(Long noticeId);

    void deleteNotice(Long noticeId);

    PageResponseDTO<NoticeResponseDTO> getNoticeList(Pageable pageable);
}
