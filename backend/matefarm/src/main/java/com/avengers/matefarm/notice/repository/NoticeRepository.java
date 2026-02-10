package com.avengers.matefarm.notice.repository;

import com.avengers.matefarm.notice.dto.NoticeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NoticeRepository extends JpaRepository<NoticeEntity, Long> {
}
