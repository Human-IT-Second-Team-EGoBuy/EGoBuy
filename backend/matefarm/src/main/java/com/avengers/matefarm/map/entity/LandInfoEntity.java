package com.avengers.matefarm.map.entity;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
    name = "land_info",
    uniqueConstraints = {
        @UniqueConstraint(name = "UK_region_cd", columnNames = {"region_cd"})
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LandInfoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "land_info_id")
    private Long landInfoId;

    @Column(name = "region_cd", length = 10, nullable = false)
    private String regionCd;

    // 각 API별 JSON 응답을 통째로 저장 (LONGTEXT)
    @Lob
    @Column(name = "exam_om_info", columnDefinition = "LONGTEXT")
    private String examOmInfo;

    @Lob
    @Column(name = "exam_ap_info", columnDefinition = "LONGTEXT")
    private String examApInfo;

    @Lob
    @Column(name = "exam_kal_info", columnDefinition = "LONGTEXT")
    private String examKalInfo;

    @Lob
    @Column(name = "exam_ph_info", columnDefinition = "LONGTEXT")
    private String examPhInfo;

    @Lob
    @Column(name = "exam_mg_info", columnDefinition = "LONGTEXT")
    private String examMgInfo;

    @Lob
    @Column(name = "exam_sal_info", columnDefinition = "LONGTEXT")
    private String examSalInfo;

    @Lob
    @Column(name = "exam_cal_info", columnDefinition = "LONGTEXT")
    private String examCalInfo;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
