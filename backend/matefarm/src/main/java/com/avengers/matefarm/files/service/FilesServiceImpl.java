package com.avengers.matefarm.files.service;

import com.avengers.matefarm.common.exception.CommonException;
import com.avengers.matefarm.common.exception.ErrorCode;
import com.avengers.matefarm.files.dto.FilesEntity;
import com.avengers.matefarm.files.dto.response.FilesResponseDTO;
import com.avengers.matefarm.files.enums.OwnerType;
import com.avengers.matefarm.files.policy.AllowedMimeType;
import com.avengers.matefarm.files.repository.FilesRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.modelmapper.internal.bytebuddy.implementation.bytecode.Throw;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/*
 *        S3에 파일을 업로드 할 분들은 해당 순서를 꼭 지켜주시킬 바랍니다!
 *
 *        1) validation check
 *        ( 공통 메소드로 만들어 놨습니다. 허용할 타입이 필요하시다면 AllowedMimeType에 추가 하시거나 말씀해주세요. )
 *
 *        2) upload
 *        ( 항상 업로드 순서는
 *         1.게시글 RDB에 저장
 *         2.게시글의 PK와 OwnerType을 S3에 업로드할 FIles와 함께 Upload 함수의 파라미터 값으로 제공합니다.
 *
 *        3) Metadata RDB에 저장
 *         후에 게시글 조회 시, S3에서 파일을 조회하기 위한 메타데이터입니다.
 *
* */

/*         모든 종류의 파일을 S3에 업로드 시킨다면 .exe .sh 등 보안에 위협이 되는 파일들이 무단으로 업로드 되게 된다.
 *        따라서 업로드를 허용할 MIME 타입을 화이트리스트로 관리한다.
 *        이 때, 프론트에서 넘어온 파일의 타입과 Aphach Tika를 이용해 파일의 타입을 직접 추출하여 비교한다.
* */

@Slf4j
@Service("FilesServiceImpl")
public class FilesServiceImpl implements FilesService {

     @Value ("${cloud.aws.s3.buckets}")
     private String S3bucket;

     @Value ("${cdn.cloudfront-domain}")
     private String CDNDomain;

    private final S3Client s3Client;
    private static final long MAX_TOTAL_FILES_SIZE = 1024 * 1024 * 50 ; // 총 파일 용량 제한 : 50MB
    private static final long MAX_INDIVIDUAL_FILE_SIZE = 1024 * 1024 * 10 ; // Byte 단위 계산 ( 개별 파일 용량 제한 : 10MB )
    private static final Tika tika = new Tika();

    private final FilesRepository filesRepository;

    public FilesServiceImpl(S3Client s3Client,
                            FilesRepository filesRepository) {

        this.s3Client = s3Client;
        this.filesRepository = filesRepository;
    }
    /* 전역으로 사용될 tika 객체를 정의 :
      validationCheck 메소드 내부에 new Tika(); 로 객체를 생성하면 요청 당 객체가 생성되며
     해당 로직이 끝난 뒤에는 고아 객체가 된 상태로 Heap 영역에 남아서 GC에 의해 처리될 때 까지 리소스 낭비가 됨.
     따라서 전역으로 객체를 선언하여 한 객체를 여러 Thread에서  Thread-safe한 구조로 사용 .
     tika 객체는 Heap 영역에 존재하는 데이터일 뿐, 여러 API 요청 ( 각 Thread ) 와는 다르다.
     Rest API에 의해 S3에 파일을 업로드 하기 전 validationCheck를 하기 위해 단 하나의 tika 객체에 담긴 Type 정보를 찾기 위해
     여러 Thread가 참조할 뿐이다.

      단일 코어의 프로세서가 여러 프로세스를 직렬로 처리해야 하는 것과 달리 "프로세스 - 쓰레드" 관점에서 본다면,
     서버가 하나의 프로세스이고, 그 속에서 처리되는 Rest API는 각 Thread이며,
     클래스 로드 시점에 이미 메타데이터 및 정적 객체 정보는 Method, Heap 영역의 메모리에 로드되므로
     해당 tika 객체의 주소를 참조할 정보를 각 Thread가 알 수 있고, 참조한다는것.

     tika 객체 안에는 Tika 타입의 인스턴스 멤버, 메소드가 존재하므로 참조가 가능함.
    * */

    /*
     *   CloudFront 배포 설정 시 :
     *    기본적으로 /path 경로를 기준으로 디렉토리를 탐색하도록 경롤르 설정하지 않고
     *    s3에서 디렉토리 구조를 만들고 그 것을 백엔드 서버에서 관리하고 어떤 디렉토리에 넣을지 다 설정할거니까
     *    굳이 이 설정을 할 필요는 없다.
     *
     *     즉, 사용할 도메인에서 미리 S3에 정의된 경로를 직접 설정해주고, 어느 디렉토리를 사용할 것인지 직접 입력해줘야 한다는 의미.
     *     한마디로, 이미 우리의 RDB에는 S3 객체의 키가 원본으로 존재하기에 CloudFront에게 해당 객체의 URL 경로만 주면
     *     CloudFront는 해당 URL 경로로 라우팅만 해주면 된다. 따라서 별도의 기본 URL 설정이 필요 없는 것.
     *
     *     추후 배포 시, 캐시 무효화 찾아보기.
     *
    * */

    @Override
    /* 파일 업로드 하는 모든 메소드가 공용으로 사용하는 업로드할 파일의 MIME 타입을 검사하는 메소드 */
    public void validationCheck(List<MultipartFile> files) {

        // 파일이 없는 경우.
        if (files == null || files.isEmpty()) {
            return;
        }

        long totalSize = 0;
        // 파일의 용량 및 타입 검증
        for (MultipartFile file : files) {
            try {

                // 1. 개별 파일 사이즈 검증. MultiPartFile 클래스에서 제공하는 Size() 메소드 : Byte 단위 사이즈 반환
                if (file.getSize() > MAX_INDIVIDUAL_FILE_SIZE) {
                    log.error("파일 사이즈 초과 - 파일명: {}, 크기: {} Byte", file.getOriginalFilename(), file.getSize());
                    throw new CommonException(ErrorCode.EXCEEDED_FILE_SIZE);
                }

                totalSize += file.getSize();

                // 2. Tika를 사용하여 파일의 실제 내용(Input Stream) 분석
                String detectedMimeType = tika.detect(file.getInputStream());
                log.info("추출된 MIME Type: {}, 원본 FileName: {}", detectedMimeType, file.getOriginalFilename());

                // 3. 화이트리스트와 비교
                if (!AllowedMimeType.isAllowed(detectedMimeType)) {
                    log.error("허용되지 않는 파일 형식 시도: {}", detectedMimeType);
                    throw new CommonException(ErrorCode.INVALID_FILE_TYPE);
                }

            } catch (IOException e) {
                log.error("파일 분석중 오류 발생 : {}", file.getOriginalFilename());
                throw new CommonException(ErrorCode.INTERNAL_SERVER_ERROR);
            }

            // 4. 전체 파일 합계 사이즈 검증
            if (totalSize > MAX_TOTAL_FILES_SIZE) {
                log.error("전체 사이즈 초과 - 합계: {} bytes", totalSize);
                throw new CommonException(ErrorCode.INVALID_INPUT_VALUE);
            }
        }
    }


    // 파일 업로드
    @Override
    public List<FilesResponseDTO> uploadFiles(List<MultipartFile> files, OwnerType ownerType, Long ownerId) {

        if (files == null || files.isEmpty()) {
            throw new CommonException(ErrorCode.NOT_FOUND_FILES);
        }

        // 공통 로직
        // validation check
        validationCheck(files);

        // upload
        List<FilesResponseDTO> uploadedFiles = new ArrayList<>();

        for (MultipartFile file : files) {
            if(file != null && !file.isEmpty()){
                // 파일 업로드
                FilesResponseDTO fileUploadResponseDTO = uploadSingleFile(file, ownerId, ownerType);

                // 업로드된 파일마다 DB에 저장.
                FilesEntity filesEntity = FilesEntity.builder()
                        .bucketName(S3bucket)
                        .objectKey(fileUploadResponseDTO.getObjectKey())
                        .originalFileName(file.getOriginalFilename())
                        .contentType(file.getContentType())
                        .fileSize(file.getSize())
                        .ownerType(ownerType)
                        .ownerId(ownerId)
                        .createdAt(LocalDateTime.now().withNano(0))
                        .build();

                FilesEntity savedEntity = filesRepository.save(filesEntity);

                // filesResponseDTO 객체에 file_id를 반환하기 위해 add 순서를 save() 이후에 배치.
                uploadedFiles.add(
                        FilesResponseDTO.builder()
                                .fileId(savedEntity.getFileId())
                                .fileOriginalName(fileUploadResponseDTO.getFileOriginalName())
                                .objectKey(fileUploadResponseDTO.getObjectKey())
                                .fileSize(fileUploadResponseDTO.getFileSize())
                                .cloudFrontUrl(fileUploadResponseDTO.getCloudFrontUrl())
                                .build()
                );
            }
        }

        // List<FilesResponseDTO> 반환
        return uploadedFiles;
    }

    @Override
    @Transactional(readOnly = true) // readOnly를 통해 Dirty Checking 을 하지 않음. 즉 영속성 컨텍스트에 해당 객체의 스냅샷이 찍히지 않는다
    /* 파일의 메타데이터 조회용 API */
    public List<FilesResponseDTO> getFilesWithOwnerTypeAndOwnerId(OwnerType ownerType, Long ownerId) {

        // Collection의 경우 조회 결과가 null 이면 빈 Collection 반환하므로 OrElseThrow가 필요 없음.
        List<FilesEntity> files = filesRepository.findFilesByOwnerTypeAndOwnerId(ownerType, ownerId);

        if(files == null || files.isEmpty()){
            throw new CommonException(ErrorCode.NOT_FOUND_FILES);
        }

        List<FilesResponseDTO> filesResponseDTO = new ArrayList<>();
        // id name key size url
        for (FilesEntity file : files) {

            // 객체마다 CDNDomain + OObjectKey로 CloudfrontUrl 생성
            String cloudfrontUrl = CDNDomain + "/" + file.getObjectKey();

            // List에 들어갈 객체들을 Builder 패턴을 통해 생성
            filesResponseDTO.add(
                    FilesResponseDTO.builder()
                            .fileId(file.getFileId())
                            .fileOriginalName(file.getOriginalFileName())
                            .objectKey(file.getObjectKey())
                            .fileSize(file.getFileSize())
                            .cloudFrontUrl(cloudfrontUrl)
                            .build()
            );
        }

        return filesResponseDTO;
    }


    /* 파일 업로드 메소드. 여러 파일을 업로드 하는 경우 List<FilesUploadResponseDTO>를 반환하도록 구현하기 */
    private FilesResponseDTO uploadSingleFile(MultipartFile file, Long ownerId, OwnerType ownerType) {

        String originalName = file.getOriginalFilename();
        String objectKey = generateS3Key(ownerType, ownerId, originalName);

        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(S3bucket)
                    .key(objectKey)
                    .contentType(file.getContentType())
                    .build();

            s3Client.putObject(putObjectRequest,
                    RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

            log.info("S3 업로드 성공: {}", objectKey);

            return FilesResponseDTO.builder()
                    .fileOriginalName(originalName)
                    .objectKey(objectKey)   // FilesEntity 가 사용할 키값 추가
                    .cloudFrontUrl(String.format("%s/%s", CDNDomain, objectKey))
                    .fileSize(file.getSize())
                    .build();

        } catch (IOException e) {
            log.error("파일 업로드 중 오류 발생: {}", originalName);
            throw new CommonException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @Transactional
    /* 파일 단건 삭제 메소드 */
    public void deleteFile(Long fileId) {
        FilesEntity filesEntity = filesRepository.findById(fileId)
                .orElseThrow(()-> new CommonException(ErrorCode.NOT_FOUND_FILES));

        try {
            // S3 파일 삭제 객체 생성
            DeleteObjectRequest deleteRequestObject = DeleteObjectRequest.builder()
                    .bucket(S3bucket)
                    .key(filesEntity.getObjectKey())
                    .build();

            // 삭제할 때에는 Back -> S3로 다이렉트 접근.
            s3Client.deleteObject(deleteRequestObject);

        } catch (Exception e) {
            log.info("S3의 파일 삭제중 에러 발생 : {}", filesEntity.getObjectKey());
            throw new CommonException(ErrorCode.INTERNAL_SERVER_ERROR);
        }

        // file 메타데이터 삭제
        filesRepository.delete(filesEntity);

    }

    /* Object Key 생성 메소드 */
    private String generateS3Key(OwnerType ownerType, Long ownerId, String originalName) {
        // Ex. NOTICE/1/UUID.pdf 형식
        String extension = originalName.substring(originalName.lastIndexOf("."));
        return String.format("%s/%d/%s%s", ownerType.name(), ownerId, UUID.randomUUID(), extension);
    }

    /* @Transactiona을 통해 게시글 생성 실패 시 S3에 올라간 파일들을 전부 삭제하는 예외 처리 전용 메소드 */

}
