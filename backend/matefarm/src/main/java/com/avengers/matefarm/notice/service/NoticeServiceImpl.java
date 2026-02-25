package com.avengers.matefarm.notice.service;

import com.avengers.matefarm.common.PageResponseDTO;
import com.avengers.matefarm.common.exception.CommonException;
import com.avengers.matefarm.common.exception.ErrorCode;
import com.avengers.matefarm.files.dto.response.FilesResponseDTO;
import com.avengers.matefarm.files.enums.OwnerType;
import com.avengers.matefarm.files.service.FilesService;
import com.avengers.matefarm.notice.dto.NoticeEntity;
import com.avengers.matefarm.notice.dto.request.NoticeUploadRequestDTO;
import com.avengers.matefarm.notice.dto.response.NoticeDetailResponseDTO;
import com.avengers.matefarm.notice.dto.response.NoticeResponseDTO;
import com.avengers.matefarm.notice.dto.response.NoticeUploadResponseDTO;
import com.avengers.matefarm.notice.enums.FileExist;
import com.avengers.matefarm.notice.repository.NoticeRepository;
import com.avengers.matefarm.user.dto.UserEntity;
import com.avengers.matefarm.user.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/*
 *      NoticeService 참조 방향 : NoticeService -> UserService
 *                                             -> FileService
* */
@Slf4j
@Service
public class NoticeServiceImpl implements NoticeService {

    private final Integer PAGE_SIZE = 10;   // 1~10 | 11~20 처럼 보여줄 페이지 번호의 숫자를 정의

    private final FilesService filesService;
    private final NoticeRepository noticeRepository;
    private final UserService userService;

    public NoticeServiceImpl(FilesService filesService,
                             NoticeRepository noticeRepository, UserService userService) {
        this.filesService = filesService;
        this.noticeRepository = noticeRepository;
        this.userService = userService;
    }

    /* 공지사항 생성 */
    @Override
    @Transactional      // S3에 파일 업로드 요청이 실패할 시 Rollback을 위해 사용
    public NoticeUploadResponseDTO createNotice(NoticeUploadRequestDTO noticeUploadRequestDTO) {

        // userId 조회
        UserEntity userEntity = userService.findUserById(noticeUploadRequestDTO.getWriterId());


        NoticeEntity noticeEntity = NoticeEntity.builder()
                .noticeTitle(noticeUploadRequestDTO.getNoticeTitle())
                .noticeContent(noticeUploadRequestDTO.getNoticeContent())
                .filesTf(noticeUploadRequestDTO.getFileExist()) // Client 에서 파일 여부를 전송.
                .writerId(userEntity)   // Jpa @ManyToOne 관계를 갖는 타입의 객체를 저장
                .build();

        NoticeEntity savedNotice = noticeRepository.save(noticeEntity);


        List<FilesResponseDTO> uploadedFiles = new ArrayList<>();
        // 2. 파일이 존재할 때만 S3 업로드 요청
        if (noticeUploadRequestDTO.getFiles() != null && !noticeUploadRequestDTO.getFiles().isEmpty()) {

            uploadedFiles = filesService.uploadFiles(
                    noticeUploadRequestDTO.getFiles(),
                    OwnerType.NOTICE,
                    savedNotice.getNoticeId()
            );
        }

        // 3. 생성 후 바로 렌더링이 가능하도록 응답 DTO를 구성하여 반환
        return NoticeUploadResponseDTO.builder()
                .noticeId(savedNotice.getNoticeId())
                .noticeTitle(savedNotice.getNoticeTitle())
                .noticeContent(savedNotice.getNoticeContent())
                .fileExist(noticeUploadRequestDTO.getFileExist())   // 파일 존재 여부
                .files(uploadedFiles)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    /* 공지사항 단건 조회 */
    public NoticeDetailResponseDTO getDetailedNotice(Long noticeId) {

        NoticeEntity noticeEntity = noticeRepository.findById(noticeId)
                .orElseThrow( () -> new CommonException(ErrorCode.NOT_FOUND_NOTICE));

        UserEntity userEntity = userService.findUserById(noticeEntity.getWriterId().getUserId());

        // files 조회
        List<FilesResponseDTO> files = filesService.getFilesWithOwnerTypeAndOwnerId(OwnerType.NOTICE, noticeId);


        return NoticeDetailResponseDTO.builder()
                .noticeId(noticeEntity.getNoticeId())
                .noticeTitle(noticeEntity.getNoticeTitle())
                .noticeContent(noticeEntity.getNoticeContent())
                .writerId(noticeEntity.getNoticeId())
                .writerNickname(userEntity.getNickname())
                .files(files)
                .build();
    }


    @Override
    @Transactional
    /* 공지사항 삭제 */
    public void deleteNotice(Long noticeId) {
        // 공지사항 조회

        // 파일 없으면 삭제

        // 파일 있으면 S3에 있는 파일 삭제 후 공지사항 삭제
    }

    /* 공지사항 페이지 조회 */
    @Override
    public PageResponseDTO<NoticeResponseDTO> getNoticeList(Pageable pageable) {

        // Data 조회 ( Page 타입으로 반환 )
        // findAll(Pagealbe)의 경우 일반적인 FindAll()과 다르게 ORDER BY 설정을 두었으므로 LIMIT 10 OFFSET 0 과 같은 쿼리를 추가로 날려 모든 요소가 한 번에 반환되지 않도록 해준다.
        Page<NoticeEntity> notices = noticeRepository.findAll(pageable);

        // EntityToDTO
        List<NoticeResponseDTO> noticeDTOs = notices.getContent().stream()
                .map(NoticeResponseDTO::from)
                .toList();

        // 한 페이지당 보여줄 게시글의 수(getSize())와 1~10, 11~20처럼 페이지 번호를 보여줄 갯수(PAGE_SIZE)를 혼동하지 말 것.
        return new PageResponseDTO<>(
                noticeDTOs,
                notices.getNumber() + 1,    // 0번부터 시작이므로 + 1
                PAGE_SIZE,                         //  보여줄 페이지 번호의 갯수. ( DTO에서 내부 로직에  사용하기 위함 )
                // 아래 두 함수는 int, long을 각각 반환하지만 객체 생성 당시 Auto Boxing으로 인해 Integer로 자동 캐스팅 된다.
                notices.getSize(),                 // 페이지당 요소의 수 ( Controller에서 정의한 Size : ) Pageable 타입의 객체가 생성될 당시 size : 10 정보를 갖고 있어 getSize() 할 시 한 페이지당 보여줄 요소의 수, 10을 반환함
                (int)notices.getTotalElements()    // 토탈 요소의 수
        );
    }


}
