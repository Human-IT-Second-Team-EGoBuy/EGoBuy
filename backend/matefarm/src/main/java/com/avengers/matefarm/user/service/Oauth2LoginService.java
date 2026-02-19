package com.avengers.matefarm.user.service;


import com.avengers.matefarm.common.exception.CommonException;
import com.avengers.matefarm.common.exception.ErrorCode;
import com.avengers.matefarm.security.JwtUtil;
import com.avengers.matefarm.user.dto.UserEntity;
import com.avengers.matefarm.user.dto.enums.SignupPath;
import com.avengers.matefarm.user.dto.oauth2.google.GoogleUserProfile;
import com.avengers.matefarm.user.dto.oauth2.kakao.KakaoUserProfile;
import com.avengers.matefarm.user.dto.oauth2.naver.NaverUserProfile;
import com.avengers.matefarm.user.vo.ResponseOAuthLoginVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import java.net.URLEncoder;
import java.util.*;
import java.util.concurrent.TimeUnit;
import static java.nio.charset.StandardCharsets.UTF_8;
/*
 * Oauth2LoginService  참조 방향 : UserController -> Oauth2LoginService
 *                                Oauth2LoginService -> UserService
 * */
@Slf4j
@Service
public class Oauth2LoginService {

    /* Google 로그인을 위한 Google Value 설정 spring.security.*/
    @Value("${oauth2.client.registration.google.client-id}")
    private String googleClientId;
    @Value("${oauth2.client.registration.google.client-secret}")
    private String googleClientSecret;
    @Value("${oauth2.client.registration.google.redirect-uri}")
    private String googleRedirectUri;
    @Value("${oauth2.client.registration.google.scope}")
    private String googleScope;

    /* Kakao 로그인을 위한 Kakao Value 설정*/
    @Value("${oauth2.client.registration.kakao.client-id}")
    private String kakaoClientId;
    @Value("${oauth2.client.registration.kakao.client-secret}")
    private String kakaoClientSecret;
    @Value("${oauth2.client.registration.kakao.redirect-uri}")
    private String kakaoRedirectUri;

    /* Naver 로그인을 위한 Naver Value 설정*/
    @Value("${oauth2.client.registration.naver.client-id}")
    private String naverClientId;
    @Value("${oauth2.client.registration.naver.client-secret}")
    private String naverClientSecret;
    @Value("${oauth2.client.registration.naver.redirect-uri}")
    private String naverRedirectUri;


    private final StringRedisTemplate stringRedisTemplate;
    private final UserService userService;
    private final JwtUtil jwtUtil;

    public Oauth2LoginService(StringRedisTemplate stringRedisTemplate1,
                              UserService userService,
                              JwtUtil jwtUtil) {
        this.stringRedisTemplate = stringRedisTemplate1;
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }
    /* 카카오 로그인을 위한 메소드 */
    public ResponseOAuthLoginVO kakaoLogin(String code, String state) {
        log.info("2-1 카카오 로그인에서 넘어온 임시 토큰 값 확인 :{}", state);

        // csrf 공격 방지를 위해 Url에서 추출한 state 값 확인.
        String redisStateValue = stringRedisTemplate.opsForValue().get("oauth:state:" + state);

        if (redisStateValue == null) {
            log.info("oauth:state 상태 토큰 검증 실패");
            throw new CommonException(ErrorCode.EXPIRED_TOKEN_ERROR);
        }

        // 2. 토큰 Value 일치 여부 검사.
        if (!redisStateValue.equals("KAKAO")) {
            throw new CommonException(ErrorCode.INVALID_TOKEN_ERROR);
        }

        // 3. 토큰 검사 후, Redis에서 임시 Token 삭제.
        stringRedisTemplate.delete("auth:state" + code);

        // 4. code를 통해 Access Token을 발급받아 사용자의 프로필을 조회.
        // 카카오는 네이버와 다르게 State 값을 카카오 서버에서 교차검증 하지 않으므로 state 값 미사용
        String kakaoAccessToken = getKakaoAccessToken(code);

        // 5. 발급받은 kakaoAccessToken을 통해 사용자 프로필 조회.
        KakaoUserProfile kakaoUserProfile = getKakaoUserProfile(kakaoAccessToken);

        // 6. 회원가입 및 로그인 정보에 활용될 식별자 생성.
        String userIdentifier = SignupPath.KAKAO + "_" + kakaoUserProfile.getId();
        log.info("6. 종료:{}", userIdentifier);

        // 7. 조회한 Profile 정보를 토대로 회원가입 및 로그인 처리
        UserEntity kakaoUser = getOrCreateKakaoLoginUser(kakaoUserProfile,userIdentifier);
        log.info("7. 회원가입 or 로그인 처리 종료:{}", kakaoUser);

        // 8. MateFarm 서비스 이용을 위한 AccessToken 및 RefreshToken 발급.
        String accessToken  = jwtUtil.generateToken(kakaoUser, List.of("USER"));
        String refreshToken = jwtUtil.generateRefreshToken(kakaoUser, List.of("USER"));


        // 9. 토큰 정보를 반환.
        return ResponseOAuthLoginVO.builder()
                .accessToken(accessToken)
                .accessTokenExpiry(new Date(jwtUtil.getAccessTokenExpiration()))
                .refreshToken(refreshToken)
                .refreshTokenExpiry(new Date(jwtUtil.getRefreshTokenExpiration()))
                .userAuthId(kakaoUser.getUserAuthId())
                .build();
    }

    /* Kakao 유저 로그인 Or 회원가입 메소드 */
    private UserEntity getOrCreateKakaoLoginUser(KakaoUserProfile kakaoUserProfile, String userIdentifier) {

        // 사용자 조회 ( Optional을 직접 받아 로직 분기 )
        // 사용자 존재 -> UsserEntity 객체 반환
        // 사용자 없으면 -> orElseGet() 을 통해 객체 생성 후 반환.
        return userService.findOptionalByUserIdentifier(userIdentifier)
                // orElseGet() : 값이 존재하지 않을 경우 대체할 값 생성
                .orElseGet(() -> userService.createKakaoLoginUser(kakaoUserProfile ,userIdentifier));
    }

    /* Kakao 로그인 유저의 Profile을 반환받는 메소드 */
    private KakaoUserProfile getKakaoUserProfile(String kakaoAccessToken) {
        log.info("카카오 유저 프로필 반환 메소드 시작");
        // 1. 헤더 설정 (Authorization: Bearer {Token})
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(kakaoAccessToken);
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<Void> request = new HttpEntity<>(headers);

        RestTemplate restTemplate = new RestTemplate();

        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                "https://kapi.kakao.com/v2/user/me",
                HttpMethod.GET,
                request,
                (Class<Map<String, Object>>) (Class<?>) Map.class);


        Map<String, Object> userInfo = response.getBody();
        log.info("KakaouserInfo: {}", userInfo);

        Map<String, Object> kakaoAccount = (Map<String, Object>) userInfo.get("kakao_account");
        log.info("KakaoAccount: {}", kakaoAccount);

        Map<String, Object> UserProfile = (Map<String, Object>)kakaoAccount.get("profile");     // userInfo{ kakaoAccount{{profile{nickname:eaxmple}}} 구조

        return new KakaoUserProfile(
                userInfo.get("id").toString(),
                (String)kakaoAccount.get("name"),
                (String)UserProfile.get("nickname"),
                (String) kakaoAccount.get("account_email"),
                (String) kakaoAccount.get("phone_number")
        );

    }

    /* 카카오 사용자의 프로필 조회를 위한 Access Token 발급 메소드 */
    private String getKakaoAccessToken(String code) {
        log.info("Kakao Access Token 발급 메소드 시작");

        // Token 요청 Url 및 값 정의
        String url = "https://kauth.kakao.com/oauth/token";
//        String redirectUrl = "http://localhost:8080/api/users/oauth2/kakao-login";

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");        // authorization_code 로 고정.
        params.add("client_id", kakaoClientId);                // 추후 Properties로 별도 관리
        params.add("client_secret", kakaoClientSecret );       // 추후 Properties로 별도 관리
        params.add("code", code);                              // AccessToken을 발급받을 일회성 Code
        params.add("redirect_uri", kakaoRedirectUri);          // Kakao로부터 인가 Code를 받은 Url : Naver가 State값을 통해 서버에서 교차검증 하는 것 처럼 카카오는 인가 코드를 받은 Url을 통해 교차 검증을 진행함.


        // Http 요청 헤더 정의.
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);          // Kakao가 요구하는 데이터 전송 타입인 application/x-www-form-urlencoded 로 전송을 위해 Form 형태로 요청에 따른 정의

        // Header와 Param(요청 본문)을 하나의 Entity로 묶음.
        HttpEntity<MultiValueMap<String, String>> request =
                new HttpEntity<>(params, headers);

        // Spring에서 제공하는 동기식 HTTP 클라이언트. 외부 API 와 통신.
        RestTemplate restTemplate = new RestTemplate();

        // 외부 API 요청에 따른 응답 Entity
        ResponseEntity<Map> response =
                restTemplate.postForEntity(url, request, Map.class);

        // 토큰을 가져올 객체의 로그 확인
        log.info("Kakao Access Token 발급 완료 : {}", response);

        // HttpStatus 상태 및 null 여부를 통해 AccessToken 리턴.
        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            return (String)response.getBody().get("access_token");          // Object 타입이므로 캐스팅 하여 사용.
        } else {
            throw new RuntimeException("카카오 액세스 토큰을 받아올 수 없습니다.");
        }
    }

    /* 네이버 로그인을 위한 메소드 */
    public ResponseOAuthLoginVO naverLogin(String code, String state) {
        log.info("2-1 네이버 로그인에서 넘어온 임시 토큰 값 확인 :{}", state);
        // csrf 공격 방지를 위해 state 값 확인.
        String redisStateValue = stringRedisTemplate.opsForValue().get("oauth:state:" + state);

        // 1. 토큰 유효성 검사
        if (redisStateValue == null) {
            log.info("oauth:state 상태 토큰 검증 실패");
            throw new CommonException(ErrorCode.EXPIRED_TOKEN_ERROR);
        }

        // 2. 토큰 Value 일치 여부 검사.
        if (!redisStateValue.equals("NAVER")) {
            throw new CommonException(ErrorCode.INVALID_TOKEN_ERROR);
        }

        // 3. 토큰 검사 후, Redis에서 임시 Token 삭제.
        stringRedisTemplate.delete("auth:state" + code);

        // 4. code를 통해 Access Token을 발급받아 사용자의 프로필을 조회.
        String naverAccessToken = getNaverAccessToken(code, state);
        log.info("4. 종료:{}", naverAccessToken);

        // 5. 발급받은 naverAccessToken을 통해 사용자 프로필 조회.
        NaverUserProfile NaverUserProfile = getNaverUserProfile(naverAccessToken);
        log.info("5. 종료:{}", NaverUserProfile);

        // 6. 회원가입 및 로그인 정보에 활용될 식별자 생성.
        String userIdentifier = SignupPath.NAVER + "_" + NaverUserProfile.getId();
        log.info("6. 종료:{}", userIdentifier);

        // 7. 조회한 Profile 정보를 토대로 회원가입 및 로그인 처리
        UserEntity naverUser = getOrCreateNaverLoginUser(NaverUserProfile,userIdentifier);
        log.info("7. 회원가입 or 로그인 처리 종료:{}", naverUser);

        // 8. MateFarm 서비스 이용을 위한 AccessToken 및 RefreshToken 발급.
        String accessToken  = jwtUtil.generateToken(naverUser, List.of("USER"));
        String refreshToken = jwtUtil.generateRefreshToken(naverUser, List.of("USER"));

        // 9. 토큰 정보를 반환.
        return ResponseOAuthLoginVO.builder()
                .accessToken(accessToken)
                .accessTokenExpiry(new Date(jwtUtil.getAccessTokenExpiration()))
                .refreshToken(refreshToken)
                .refreshTokenExpiry(new Date(jwtUtil.getRefreshTokenExpiration()))
                .userAuthId(naverUser.getUserAuthId())
                .build();


    }

    /* Naver 로그인 유저 로그인 Or 회원가입 메소드 */
    private UserEntity getOrCreateNaverLoginUser(NaverUserProfile userProfile, String userIdentifier) {

        // 사용자 조회 ( Optional을 직접 받아 로직 분기 )
        // 사용자 존재 -> UserEntity 객체 반환
        // 사용자 없으면 -> orElseGet() 을 통해 객체 생성 후 반환.
        return userService.findOptionalByUserIdentifier(userIdentifier)
                // orElseGet() : 값이 존재하지 않을 경우 대체할 값 생성
                .orElseGet(() -> userService.createNaverLoginUser(userProfile ,userIdentifier));
    }

    /* Naver 로그인 유저의 Profile을 반환받는 메소드 */
    private NaverUserProfile getNaverUserProfile(String accessToken) {

        // 발급받은 토큰을 요청 헤더에 넣어 사용자 정보 요청
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity<Void> request = new HttpEntity<>(headers);

        RestTemplate restTemplate = new RestTemplate();

        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                "https://openapi.naver.com/v1/nid/me",
                HttpMethod.GET,
                request,
                (Class<Map<String, Object>>) (Class<?>) Map.class);


        Map<String, Object> userInfo = (Map<String, Object>) response.getBody().get("response");

        return new NaverUserProfile(
                userInfo.get("id").toString(),
                (String)userInfo.get("name"),
                (String)userInfo.get("nickname"),
                (String) userInfo.get("email"),
                (String) userInfo.get("mobile")  // 네이버에서는 실명이 name 속성에 담김
        );
    }

    /* 네이버 사용자의 프로필 조회를 위한 Access Token 발급 메소드 */
    private String getNaverAccessToken(String code, String state) {
        log.info("Naver Access Token 발급 메소드 시작");

        // Token 요청 Url 및 값 정의
        String url = "https://nid.naver.com/oauth2.0/token";
//        String redirectUrl = "http://localhost:8080/api/users/oauth2/naver-login";

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");     // authorization_code 로 고정.
        params.add("client_id", naverClientId);             // 추후 Properties로 별도 관리
        params.add("client_secret", naverClientSecret );    // 추후 Properties로 별도 관리
        params.add("code", code);                           // AccessToken을 발급받을 일회성 Code
        params.add("state", state);                         // 로그인의 연속성을 보장하기 위한 용도로, 네이버 서버에서 사용되는 임시 Token 값.


        // Http 요청을 보내기 위한 준비.
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> request =
                new HttpEntity<>(params, headers);

        RestTemplate restTemplate = new RestTemplate();

        ResponseEntity<Map> response =
                restTemplate.postForEntity(url, request, Map.class);

        // 토큰을 가져올 객체의 로그 확인
        log.info("Access Token 발급 완료 : {}", response);

        // HttpStatus 상태 및 null 여부를 통해 AccessToken 리턴.
        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            return (String)response.getBody().get("access_token");          // Object 타입이므로 캐스팅 하여 사용.
        } else {
            throw new RuntimeException("네이버 액세스 토큰을 받아올 수 없습니다.");
        }
    }

    /* 네이버 로그인 요청 시 보낼 URL 정의. */
    public String getNaverLoginRedirectUrl() {

        // state 값을 Redis에 임시 보관. TTL( 5분 )
        String state = UUID.randomUUID().toString();
//        String redirectUrl = "http://localhost:8080/api/users/oauth2/naver-login";

        // Value는 존재 유무만 확인하면 되므로 유의미한 값일 필요 없음.
        stringRedisTemplate.opsForValue()
                .set("oauth:state:" + state, "NAVER", 5, TimeUnit.MINUTES);
        log.info("2-1 네이버 로그인 State값 (임시토큰) 생성 완료:{}", state);

        return "https://nid.naver.com/oauth2.0/authorize"
                + "?response_type=code"
                + "&client_id=" + naverClientId
                + "&redirect_uri=" + URLEncoder.encode(naverRedirectUri, UTF_8) // CallBack Url
                + "&state=" + state;

    }

    /* 카카오 로그인 요청 시 보낼 URL 정의. */
    public String getKakaoLoginRedirectUrl() {

        // state 값을 Redis에 임시 보관. TTL( 5분 )
        String state = UUID.randomUUID().toString();
//        String redirectUrl = "http://localhost:8080/api/users/oauth2/kakao-login";

        // Value는 존재 유무만 확인하면 되므로 유의미한 값일 필요 없음.
        stringRedisTemplate.opsForValue()
                .set("oauth:state:" + state, "KAKAO", 5, TimeUnit.MINUTES);
        log.info("2-3 카카오 로그인 State값 (임시토큰) 생성 완료:{}", state);

        return "https://kauth.kakao.com/oauth/authorize"
                + "?response_type=code"
                + "&client_id=" + kakaoClientId                                 // Kakao Rest API Key
                + "&redirect_uri=" + URLEncoder.encode(kakaoRedirectUri, UTF_8) // CallBack Url
                + "&state=" + state;
    }


    /* 구글 로그인 요청 시 보낼 URL 정의. */
    public String getGoogleLoginRedirectUrl() {

        // state 값을 Redis에 임시 보관. TTL( 5분 )
        String state = UUID.randomUUID().toString();
//        String redirectUrl = "http://localhost:8080/api/users/oauth2/google-login";

        // Value는 존재 유무만 확인하면 되므로 유의미한 값일 필요 없음.
        stringRedisTemplate.opsForValue()
                .set("oauth:state:" + state, "GOOGLE", 5, TimeUnit.MINUTES);
        log.info("2-5 구글 로그인 State값 (임시토큰) 생성 완료:{}", state);

        return "https://accounts.google.com/o/oauth2/v2/auth"
                + "?response_type=code"
                + "&client_id=" + googleClientId                // Google Client ID
                + "&redirect_uri=" + URLEncoder.encode(googleRedirectUri, UTF_8)     // CallBack Url
                + "&state=" + state
                + "&scope=" + googleScope;        // openid 는 Id_Token을 발급받기 위해 반드시 포함. 나머지는 요청할 값. 원본 Scope : openid%20email%20profile

    }

    /* 구글 로그인을 위한 메소드 */
    public ResponseOAuthLoginVO googleLogin(String code, String state) {

        log.info("2-5 구글 로그인에서 넘어온 임시 토큰 값 확인 :{}", state);
        // csrf 공격 방지를 위해 state 값 확인.
        String redisStateValue = stringRedisTemplate.opsForValue().get("oauth:state:" + state);

        // 1. 토큰 유효성 검사
        if (redisStateValue == null) {
            log.info("oauth:state 상태 토큰 검증 실패");
            throw new CommonException(ErrorCode.EXPIRED_TOKEN_ERROR);
        }

        // 2. 토큰 Value 일치 여부 검사.
        if (!redisStateValue.equals("GOOGLE")) {
            throw new CommonException(ErrorCode.INVALID_TOKEN_ERROR);
        }

        // 3. 토큰 검사 후, Redis에서 임시 Token 삭제.
        stringRedisTemplate.delete("auth:state" + code);

        // 4. code를 통해 AccessToken을 발급받아 사용자의 프로필을 조회.
        // 구글은 Id_Token 사용도 가능.
        String googleAccessToken = getGoogleAccessToken(code);

        // 5. 발급받은 googleAccessToken을 통해 사용자 프로필 조회.
        GoogleUserProfile googleUserProfile = getGoogleUserProfile(googleAccessToken);

        // 6. 회원가입 및 로그인 정보에 활용될 식별자 생성.
        String userIdentifier = SignupPath.GOOGLE + "_" + googleUserProfile.getId();
        log.info("6. 종료:{}", userIdentifier);
//
//        // 7. 조회한 Profile 정보를 토대로 회원가입 및 로그인 처리
        UserEntity googleUser = getOrCreateGoogleLoginUser(googleUserProfile,userIdentifier);
        log.info("7. 회원가입 or 로그인 처리 종료:{}", googleUser);
//
//        // 8. MateFarm 서비스 이용을 위한 AccessToken 및 RefreshToken 발급.
        String accessToken  = jwtUtil.generateToken(googleUser, List.of("USER"));
        String refreshToken = jwtUtil.generateRefreshToken(googleUser, List.of("USER"));


        // 9. 토큰 정보를 반환.
        return ResponseOAuthLoginVO.builder()
                .accessToken(accessToken)
                .accessTokenExpiry(new Date(jwtUtil.getAccessTokenExpiration()))
                .refreshToken(refreshToken)
                .refreshTokenExpiry(new Date(jwtUtil.getRefreshTokenExpiration()))
                .userAuthId(googleUser.getUserAuthId())
                .build();
    }

    /* Naver 로그인 유저 로그인 Or 회원가입 메소드 */
    private UserEntity getOrCreateGoogleLoginUser(GoogleUserProfile googleUserProfile, String userIdentifier) {

        // 사용자 조회 ( Optional을 직접 받아 로직 분기 )
        // 사용자 존재 -> UsserEntity 객체 반환
        // 사용자 없으면 -> orElseGet() 을 통해 객체 생성 후 반환.
        return userService.findOptionalByUserIdentifier(userIdentifier)
                // orElseGet() : 값이 존재하지 않을 경우 대체할 값 생성
                .orElseGet(() -> userService.createGoogleLoginUser(googleUserProfile ,userIdentifier));
    }


    /* Google 로그인 유저의 Profile을 반환받는 메소드 */
    private GoogleUserProfile getGoogleUserProfile(String googleAccessToken) {

        // 발급받은 토큰을 요청 헤더에 넣어 사용자 정보 요청
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(googleAccessToken);       // Http 요청 시 헤더에 Bearer 토큰 정보 입력 필수.

        HttpEntity<Void> request = new HttpEntity<>(headers);

        RestTemplate restTemplate = new RestTemplate();

        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                "https://www.googleapis.com/oauth2/v3/userinfo",
                HttpMethod.GET,
                request,
                (Class<Map<String, Object>>) (Class<?>) Map.class);

        Map<String, Object> userInfo = response.getBody();

//        if (userInfo == null) {
//            throw new RuntimeException("구글 응답 바디가 비어있습니다.");
//        }

        return new GoogleUserProfile(
                userInfo.get("sub").toString(),
                (String)userInfo.get("name"),
                (String)userInfo.get("given_name"),       // google 은 사용자의 이름을 nickname 초기 값으로 사용
                (String) userInfo.get("email")
//                (String) userInfo.get("mobile")
        );
    }

    /* 구글 사용자의 프로필 조회를 위한 Access Token 발급 메소드 */
    private String getGoogleAccessToken(String code) {

        log.info("Google Access Token 발급 메소드 시작");

        // Token 요청 Url 및 값 정의
        String url = "https://oauth2.googleapis.com/token";
//        String redirectUrl = "http://localhost:8080/api/users/oauth2/google-login";

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");     // authorization_code 로 고정.
        params.add("client_id", googleClientId);
        params.add("client_secret", googleClientSecret );
        params.add("code", code);                           // AccessToken을 발급받을 일회성 Code
        params.add("redirect_uri", googleRedirectUri);      // CallBack Url


        // Http 요청 헤더 정의.
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);          // Google이 요구하는 데이터 전송 타입인 application/x-www-form-urlencoded 로 전송을 위해 Form 형태로 요청에 따른 정의

        // Header와 Param(요청 본문)을 하나의 Entity로 묶음.
        HttpEntity<MultiValueMap<String, String>> request =
                new HttpEntity<>(params, headers);

        // Spring에서 제공하는 동기식 HTTP 클라이언트. 외부 API 와 통신.
        RestTemplate restTemplate = new RestTemplate();

        // 외부 API 요청에 따른 응답 Entity
        ResponseEntity<Map> response =
                restTemplate.postForEntity(url, request, Map.class);

        // 토큰을 가져올 객체의 로그 확인
        log.info("Google Access Token 발급 완료 : {}", response);

        // HttpStatus 상태 및 null 여부를 통해 AccessToken 리턴.
        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            return (String)response.getBody().get("access_token");          // Object 타입이므로 캐스팅 하여 사용.
        } else {
            throw new RuntimeException("구글 액세스 토큰을 받아올 수 없습니다.");
        }

    }


}

/**
  *  커스텀 OAuth2 로그인 서비스
 * *  이 서비스를 사용하는 이유 :
  * 스프링 시큐리티의 기본 OAuth2 Client 라이브러리를 사용하지 않고,
  * RestTemplate을 이용해 직접 액세스 토큰과 프로필을 조회하는 수동 방식을 선택함.
  *
 * *  주의사항 :
  * application.yml 설정 시 'spring.security.oauth2.client' 경로를 그대로 사용하면
  * 스프링 부트가 '자동 OAuth2 설정'을 시도하다가 빈 생성 에러(UnsatisfiedDependencyException)를 발생시킴.
  * 따라서 yml에서는 'oauth2.google...' 처럼 커스텀 경로를 사용하여
  * 스프링의 자동 설정(Auto-configuration) 기능이 활성화되지 않도록 해야 함..
  **/
