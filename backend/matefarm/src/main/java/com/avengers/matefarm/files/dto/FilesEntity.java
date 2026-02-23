package com.avengers.matefarm.files.dto;

import com.avengers.matefarm.files.enums.OwnerType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Entity
@Builder
@Table(name = "files")
@AllArgsConstructor
@NoArgsConstructor
public class FilesEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long fileId;

    @Column(name = "bucket_name", nullable = false, length = 255)
    private String bucketName;

    @Column(name = "object_key", nullable = false, length = 255)
    private String objectKey;        // S3 내부 경로

    @Column(name = "original_file_name", nullable = false, length = 255)
    private String originalFileName;

    @Column(name = "content_type", nullable = false, length = 255)
    private String contentType;

    @Column(name = "file_size", nullable = false, length = 255)
    private Long fileSize;

    @Enumerated(EnumType.STRING)
    @Column(name = "owner_type", nullable = false, length = 255)
    private OwnerType ownerType;     // S3에 업로드 하는 도메인 (COMMUNITY_POST, NOTICE 등)

    @Column(name = "owner_id", nullable = false, length = 255)
    private Long ownerId;            // 해당 도메인의 PK

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;


    // filesId 컬럼도 @Builder의 기본 생성자에는 포함이 되어 있으므로 이를 제외하기 위해 Builder 생성자 추가
    public FilesEntity(
            String bucketName,
            String objectKey,
            String originalFileName,
            String contentType,
            Long fileSize,
            OwnerType ownerType,
            Long ownerId
    ) {
        this.bucketName = bucketName;
        this.objectKey = objectKey;
        this.originalFileName = originalFileName;
        this.contentType = contentType;
        this.fileSize = fileSize;
        this.ownerType = ownerType;
        this.ownerId = ownerId;
    }

    /* TEMP 파일 생성 시 쓰일 임시 OnwerId 할당용 */
    public void updateOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }

    public void updateOwner(OwnerType ownerType, Long ownerId) {
        this.ownerType = ownerType;
        this.ownerId = ownerId;
    }


}
