package com.avengers.matefarm.communitypost.repository;

import com.avengers.matefarm.communitypost.dto.CommunityPostEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommunityPostRepository extends JpaRepository<CommunityPostEntity, Long> {
}
