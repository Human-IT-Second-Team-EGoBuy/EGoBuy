package com.avengers.matefarm.insectpestinfo.service;

import com.avengers.matefarm.insectpestinfo.dto.CropCategoryDto;
import com.avengers.matefarm.insectpestinfo.dto.PagedResponse;
import com.avengers.matefarm.insectpestinfo.dto.PestIssueRowDto;
import com.avengers.matefarm.insectpestinfo.repository.CropCategoryRepository;
import com.avengers.matefarm.insectpestinfo.repository.PestIssueRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InsectPestInfoService {

    private final CropCategoryRepository categoryRepo;
    private final PestIssueRepository pestRepo;

    public InsectPestInfoService(CropCategoryRepository categoryRepo, PestIssueRepository pestRepo) {
        this.categoryRepo = categoryRepo;
        this.pestRepo = pestRepo;
    }

    public List<CropCategoryDto> getCategories() {
        return categoryRepo.findByStatusOrderByNameAsc(1)
                .stream()
                .map(c -> new CropCategoryDto(c.getId(), c.getName()))
                .toList();
    }

    public PagedResponse<PestIssueRowDto> getPestIssues(Long categoryId, String ptype, String q, int page, int size) {
        int safePage = Math.max(page, 1);
        int safeSize = Math.max(size, 1);

        // ptype/q normalize
        String normPtype = (ptype == null || ptype.isBlank() || "all".equalsIgnoreCase(ptype)) ? null : ptype;
        String normQ = (q == null || q.trim().isBlank()) ? null : q.trim();

        PageRequest pageable = PageRequest.of(safePage - 1, safeSize);

        Page<PestIssueRepository.PestIssueRowView> result =
                pestRepo.findPestIssues(categoryId, normPtype, normQ, pageable);

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
                safePage,
                safeSize,
                result.getTotalPages() == 0 ? 1 : result.getTotalPages()
        );
    }
}
