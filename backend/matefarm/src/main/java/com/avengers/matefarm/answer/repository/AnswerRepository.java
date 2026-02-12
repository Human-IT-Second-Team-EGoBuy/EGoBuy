package com.avengers.matefarm.answer.repository;

import com.avengers.matefarm.answer.dto.AnswerEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnswerRepository extends JpaRepository<AnswerEntity, Long> {
}
