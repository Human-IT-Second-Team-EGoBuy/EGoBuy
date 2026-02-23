package com.avengers.matefarm.insectpestinfo.service;

import com.avengers.matefarm.common.exception.CommonException;
import com.avengers.matefarm.common.exception.ErrorCode;
import com.avengers.matefarm.insectpestinfo.dto.CropCategoryDto;
import com.avengers.matefarm.insectpestinfo.dto.PagedResponse;
import com.avengers.matefarm.insectpestinfo.dto.PestIssueRowDto;
import com.avengers.matefarm.insectpestinfo.repository.CropCategoryRepository;
import com.avengers.matefarm.insectpestinfo.repository.PestIssueRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

// 병해/해충 조회 서비스, 카테고리 목록 조회, 병해/해충 통합 목록 조회(필터/검색/페이징)
@Service
public class InsectPestInfoService {

    private final CropCategoryRepository categoryRepo;
    private final PestIssueRepository pestRepo;

    public InsectPestInfoService(CropCategoryRepository categoryRepo, PestIssueRepository pestRepo) {
        this.categoryRepo = categoryRepo;
        this.pestRepo = pestRepo;
    }

    // 활성(status=1) 카테고리 목록을 이름순으로 가져와 DTO로 변환해서 반환
    public List<CropCategoryDto> getCategories() {
        return categoryRepo.findByStatusOrderByNameAsc(1)
                .stream()
                .map(c -> new CropCategoryDto(c.getId(), c.getName()))
                .toList();
    }

    // 병해/해충 통합 목록 조회 (필터/검색/페이징)
    public PagedResponse<PestIssueRowDto> getPestIssues(Long categoryId, String ptype, String q, int page, int size) {
        if (page < 1 || size < 1) {
                throw new CommonException(ErrorCode.INVALID_INPUT_VALUE);
        }
        
        // 쿼리 파라미터 정규화: null이면 SQL에서 조건을 "적용하지 않음" 처리
        String normPtype = (ptype == null || ptype.isBlank() || "all".equalsIgnoreCase(ptype)) ? null : ptype.trim().toLowerCase();;
        if (normPtype != null && !(normPtype.equalsIgnoreCase("insect") || normPtype.equalsIgnoreCase("disease"))) {
                throw new CommonException(ErrorCode.INVALID_PARAMETER_FORMAT);
        }
        String normQ = (q == null || q.trim().isBlank()) ? null : q.trim();

        // Spring Data Pageable은 0-based page
        PageRequest pageable = PageRequest.of(page - 1, size);

        // 네이티브 쿼리 실행 결과(Page) - Projection(View)로 먼저 받음
        Page<PestIssueRepository.PestIssueRowView> result =
                pestRepo.findPestIssues(categoryId, normPtype, normQ, pageable);

        // Projection(View) -> 응답 DTO로 변환
        List<PestIssueRowDto> items = result.getContent().stream()
                .map(v -> new PestIssueRowDto(
                        v.getPestType(),
                        v.getPestId(),
                        v.getCropId(),
                        v.getCropName(),
                        v.getPestName(),
                        v.getUpdatedAt()
                ))
                .toList();

        return new PagedResponse<>(
                items,
                result.getTotalElements(),
                page,
                size,
                // 데이터가 0건일 때 totalPages가 0으로 나오는 경우를 1로 보정(프론트 UI 대응)
                result.getTotalPages() == 0 ? 1 : result.getTotalPages()
        );
    }
}
