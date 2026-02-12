package com.avengers.matefarm.inquiry.repository;

import com.avengers.matefarm.inquiry.dto.InquiryEntity;
import com.avengers.matefarm.user.dto.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InquiryRepository extends JpaRepository<InquiryEntity, Long> {

    // where writer_id = * 인 row만 반환하도록 Entity 추가
    Page<InquiryEntity> findAllByWriterId(UserEntity userEntity, Pageable pageable);
}
