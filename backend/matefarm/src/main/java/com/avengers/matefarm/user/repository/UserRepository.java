package com.avengers.matefarm.user.repository;



import com.avengers.matefarm.user.dto.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {

    Optional<UserEntity> findByUserAuthId(@Param("userAuthId") String userAuthId);
    // Nickname 중복 검사
    boolean existsByNickname(String nickname);

    // UserAuthId(LoginId) 중복 검사
    boolean existsByUserAuthId(String userAuthId);

    // Email 중복 검사
    boolean existsByEmail(String email);

    Optional<UserEntity> findByEmail(String email);

    // JPA가 Entity 객체를 반환받아 UserEntity -> String 예외가 발생하는 것을 방지하기 위해 JPQL(Entity 기준) 사용.
//    String findUserAuthIdByEmail(email);
    @Query("select u.userAuthId from UserEntity u where u.email = :email")
    String findUserAuthIdByEmail(@Param("email") String email);

    Optional<UserEntity> findUserByEmail(String email);

    // 소셜 로그인 시 사용되는 유저 식별자를 통한 조회
    Optional<UserEntity> findByUserIdentifier(String userIdentifier);
}
