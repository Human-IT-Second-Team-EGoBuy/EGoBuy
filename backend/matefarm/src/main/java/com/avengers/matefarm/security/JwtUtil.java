package com.avengers.matefarm.security;


import com.avengers.matefarm.common.exception.CommonException;
import com.avengers.matefarm.common.exception.ErrorCode;
import com.avengers.matefarm.security.dto.AuthTokens;
import com.avengers.matefarm.user.dto.UserEntity;
import com.avengers.matefarm.user.service.UserService;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.*;
import java.util.stream.Collectors;


@Slf4j
@Getter
@Component
public class JwtUtil {

    private final Key secretKey;
    private final long accessExpirationTime;
    private final long refreshExpirationTime;
    private final UserService userService;

    public JwtUtil(
            @Value("${token.secret}") String secretKey,
            @Value("${token.access-expiration-time}") long accessExpirationTime,
            @Value("${token.refresh-expiration-time}") long refreshExpirationTime,
            UserService userService
    ) {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
        this.accessExpirationTime = accessExpirationTime;
        this.refreshExpirationTime = refreshExpirationTime;
        this.userService = userService;
    }

    // 설명. 리프레시 토큰으로 액세스 토큰 재발급하는 로직 처리
    public AuthTokens refreshAccessToken(String refreshToken) {
        // 1. 리프레시 토큰의 유효성 검사
        if (!validateToken(refreshToken)) {
            throw new CommonException(ErrorCode.EXPIRED_TOKEN_ERROR);
        }

        // 2. 리프레시 토큰에서 사용자 정보 추출
        String userAuthId = getUserId(refreshToken);  // 이제 userAuthId로 사용자를 식별
        UserEntity user = userService.findByUserAuthId(userAuthId);  // 변경된 메서드 호출

        if (user == null) {
            throw new CommonException(ErrorCode.NOT_FOUND_USER);
        }

        // 3. 새로운 액세스 토큰 생성
        String role = user.getUserRole().name();  // 역할 가져오기

        String newAccessToken = generateToken(user, Collections.singletonList(role));

        // 리프레시 토큰은 그대로 유지
        return new AuthTokens(
                newAccessToken,
                refreshToken,  // 기존 리프레시 토큰 유지
                "Bearer",
                getAccessTokenExpiration(),
                getRefreshTokenExpiration(),
                userAuthId  // 이제 userAuthId로 반환
        );
    }

    // 설명. Token 검증 메소드
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token);
            return true;
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            log.info("Invalid JWT Token {}", e.getMessage());
            throw new CommonException(ErrorCode.INVALID_TOKEN_ERROR);
        } catch (ExpiredJwtException e) {
            log.info("Expired JWT Token {}", e.getMessage());
            throw new CommonException(ErrorCode.EXPIRED_TOKEN_ERROR);
        } catch (UnsupportedJwtException e) {
            log.info("Unsupported JWT Token {}", e.getMessage());
            throw new CommonException(ErrorCode.TOKEN_UNSUPPORTED_ERROR);
        } catch (IllegalArgumentException e) {
            log.info("JWT Token claims empty {}", e.getMessage());
            throw new CommonException(ErrorCode.TOKEN_MALFORMED_ERROR);
        }
    }

    // 설명. Token에서 인증 객체 추출
    public Authentication getAuthentication(String token) {
        String userAuthId = this.getUserId(token);  // 이제 userAuthId로 변경
        UserDetails userDetails = userService.loadUserByUsername(userAuthId);  // 사용자 조회

        Claims claims = parseClaims(token);
        log.info("넘어온 AccessToken claims 확인: {}", claims);

        String authClaim = claims.get("auth") != null ? claims.get("auth").toString() : null;
        if (authClaim == null || authClaim.trim().isEmpty()) {
            throw new RuntimeException("권한 정보가 없는 토큰입니다.");
        }

        Collection<? extends GrantedAuthority> authorities
                = Arrays.stream(authClaim.replace("[", "").replace("]", "").split(", "))
                .map(String::trim)
                .filter(role -> !role.isEmpty()) // 빈 문자열 제거
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());

        return new UsernamePasswordAuthenticationToken(userDetails, "", authorities);
    }

    // 설명. Token에서 Claims 추출
    public Claims parseClaims(String token) {
        return Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token).getBody();
    }

    // 설명. Token에서 userAuthId 추출 (이전의 getUserId는 userIdentifier였음)
    public String getUserId(String token) {
        return parseClaims(token).getSubject();  // 이제 userAuthId가 subject에 저장됨
    }

    // 설명. 액세스 토큰 생성 메소드
    public String generateToken(UserEntity user, List<String> roles) {
        return Jwts.builder()
                .setSubject(user.getUserAuthId()) // 사용자 식별자 변경 (userAuthId 사용)
                .claim("email", user.getEmail()) // 이메일 클레임 추가
                .claim("auth", roles) // 역할 정보 클레임 추가
                .claim("userAuthId", user.getUserAuthId()) // user_auth_id 클레임 추가
                .setIssuedAt(new Date()) // 발행 시간 설정
                .setExpiration(new Date(System.currentTimeMillis() + accessExpirationTime)) // 액세스 토큰 만료 시간 설정
                .signWith(secretKey, SignatureAlgorithm.HS512) // 서명 알고리즘과 시크릿 키 설정
                .compact();
    }

    // 설명. 리프레시 토큰 생성 메소드
    public String generateRefreshToken(UserEntity user, List<String> roles) {
        return Jwts.builder()
                .setSubject(user.getUserAuthId()) // 사용자 식별자 변경 (userAuthId 사용)
                .claim("email", user.getEmail()) // 이메일 클레임 추가
                .claim("auth", roles) // 역할 정보 클레임 추가
                .claim("userAuthId", user.getUserAuthId()) // user_auth_id 클레임 추가
                .setIssuedAt(new Date()) // 발행 시간 설정
                .setExpiration(new Date(System.currentTimeMillis() + refreshExpirationTime)) // 리프레시 토큰 만료 시간 설정
                .signWith(secretKey, SignatureAlgorithm.HS512) // 서명 알고리즘과 시크릿 키 설정
                .compact();
    }


    // 설명. 액세스 토큰 만료 시간 가져오기
    public long getAccessTokenExpiration() {
        return System.currentTimeMillis() + accessExpirationTime;
    }

    // 설명. 리프레시 토큰 만료 시간 가져오기
    public long getRefreshTokenExpiration() {
        return System.currentTimeMillis() + refreshExpirationTime;
    }
}

