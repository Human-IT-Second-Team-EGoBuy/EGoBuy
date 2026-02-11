package com.avengers.matefarm.inquiry.repository;

import com.avengers.matefarm.inquiry.dto.InquiryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InquiryRepository extends JpaRepository<InquiryEntity, Long> {
}
