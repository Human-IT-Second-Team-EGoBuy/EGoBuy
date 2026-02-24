package com.avengers.matefarm.user.controller;


import com.avengers.matefarm.common.ResponseDTO;
import com.avengers.matefarm.common.exception.CommonException;
import com.avengers.matefarm.common.exception.ErrorCode;
import com.avengers.matefarm.security.JwtUtil;
import com.avengers.matefarm.security.dto.AuthTokens;
import com.avengers.matefarm.user.dto.UserDTO;
import com.avengers.matefarm.user.dto.request.RequestCodeVerificationDTO;
import com.avengers.matefarm.user.dto.request.RequestLoginedUserPasswordChangeDTO;
import com.avengers.matefarm.user.dto.request.RequestResetPasswordDTO;
import com.avengers.matefarm.user.dto.request.RequestUserRegistVO;
import com.avengers.matefarm.user.dto.response.ResponseUserAuthIdDTO;
import com.avengers.matefarm.user.dto.validate.BooleanResponseDTO;
import com.avengers.matefarm.user.dto.validate.RequestEmailDTO;
import com.avengers.matefarm.user.dto.validate.RequestLoginIdDTO;
import com.avengers.matefarm.user.dto.validate.RequestNicknameDTO;
import com.avengers.matefarm.user.service.EmailService;
import com.avengers.matefarm.user.service.Oauth2LoginService;
import com.avengers.matefarm.user.service.UserService;
import com.avengers.matefarm.user.vo.ResponseOAuthLoginVO;
import com.avengers.matefarm.user.vo.ZustandDataResponseDTO;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@Slf4j
@RestController("CommandUserController")
@RequestMapping("/api/users")
public class CommandUserController {

    public CommandUserController(JwtUtil jwtUtil,
                                 UserService userService,
                                 EmailService emailService,
                                 Oauth2LoginService oauth2LoginService) {

        this.jwtUtil = jwtUtil;
        this.userService = userService;
        this.emailService = emailService;
        this.oauth2LoginService = oauth2LoginService;
    }

    // 소셜 로그인 후 Root 페이지로 리다이렉트 하기 위해 사용
    @Value("${front.root-url}")
    private String frontRootUrl;

    private final JwtUtil jwtUtil;
    private final UserService userService;
    private final EmailService emailService;
    private final Oauth2LoginService oauth2LoginService;

    /* 1. 일반 회원 가입 */
    @PostMapping("/signup/normal")
    public ResponseDTO<UserDTO> registNormalUser(@RequestBody RequestUserRegistVO newUser) {

        UserDTO savedUserDTO = userService.registUser(newUser);

        // ResponseUserVO로 변환하는 대신 UserDTO를 직접 응답으로 사용
        return ResponseDTO.ok(savedUserDTO);
    }


    /* 로그인 & 로그아웃 */
    /* 2-1. 네이버 로그인 URL 전송 API */
    @GetMapping("/oauth2/naver")
    public void redirectToNaver(HttpServletResponse response) throws IOException {

        String url =
                oauth2LoginService.
                        getNaverLoginRedirectUrl();

        response.sendRedirect(url);
    }

    /* 2-2. 네이버 로그인 */
    @GetMapping("/oauth2/naver-login")
    public ResponseDTO<Void> naverLogin(
            @RequestParam String code,
            @RequestParam String state,
            HttpServletResponse response) throws IOException {

        log.info("2-2 네이버 로그인 시작: code:{}, state:{}", code, state);
        // 회원이 네이버 로그인 시 code를 발급받아 사용. 이 정보로 AccessToken / Refresh Token 발급 ( 네이버 API 이용에 사용되는 )
        // 최종적으로 발급받은 Access Token을 이용해 프로필을 조회하여 이름, 전화번호 등 회원가입에 필요한 필수 정보만 추출.
        // 로그인 시 회원가입이 되었는지, 회원인지 판단하여 로직 분기.
        // 단, https://nid.naver.com/oauth2.0/authorize 로 요청 시, reat api 방식이 아닌 쿼리스트링 방식으로 호출 해야함.

        // 1. naverLogin 로직
        ResponseOAuthLoginVO responseOauthLoginVO =
                oauth2LoginService.
                        naverLogin(
                                code,
                                state);

        // 2. 쿠키 설정 (HttpOnly)
        ResponseCookie accessCookie = ResponseCookie.from("accessToken", responseOauthLoginVO.getAccessToken())
                .path("/")
                .httpOnly(true)
                .secure(false) // 로컬 테스트 시 false, 배포(https) 시 true
                .maxAge(3600)
                .sameSite("Lax")
                .build();

        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", responseOauthLoginVO.getRefreshToken())
                .path("/")
                .httpOnly(true)
                .secure(false)
                .maxAge(604800)
                .sameSite("Lax")
                .build();

        // 3. 응답 헤더에 쿠키 추가
        response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());

        // 4. 로그인 성공 시 ("/") 루트 페이지로 리다이렉트
        response.sendRedirect(frontRootUrl);
        return ResponseDTO.ok(null);
    }

    /* 2-3. 카카오 로그인 URL 전송 API */
    @GetMapping("/oauth2/kakao")
    public void redirectToKakao(HttpServletResponse response) throws IOException {

        String url =
                oauth2LoginService.
                        getKakaoLoginRedirectUrl();

        response.sendRedirect(url);
    }

    /* 2-4. 카카오 로그인 */
    @GetMapping("/oauth2/kakao-login")
    public ResponseDTO<ResponseOAuthLoginVO> kakaoLogin(
            @RequestParam String code,
            @RequestParam String state
    ) {

        ResponseOAuthLoginVO responseOauthLoginVO =
                oauth2LoginService.
                        kakaoLogin(
                                code,
                                state);

        return ResponseDTO.ok(responseOauthLoginVO);
    }

    /* 2-5. 구글 로그인 URL 전송 API */
    @GetMapping("/oauth2/google")
    public void redirectToGoogle(HttpServletResponse response) throws IOException {

        String url =
                oauth2LoginService.
                        getGoogleLoginRedirectUrl();

        response.sendRedirect(url);
    }

    /* 2-6. 구글 로그인  */
    @GetMapping("/oauth2/google-login")
    public ResponseDTO<ResponseOAuthLoginVO> googleLogin(
            @RequestParam String code,
            @RequestParam String state
    ) {
        ResponseOAuthLoginVO responseOAuthLoginVO =
                oauth2LoginService.
                        googleLogin(
                                code,
                                state);

        return ResponseDTO.ok(responseOAuthLoginVO);

    }


    /* 2-7. 로그아웃 */
    @PostMapping("/auth/logout")
    public ResponseDTO<Void> logout(HttpServletResponse response) {

        // Cookie 삭제 (만료시간 0으로 설정)
        ResponseCookie accessCookie = ResponseCookie.from("accessToken", "")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(0)
                .sameSite("Strict")
                .build();

        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(0)
                .sameSite("Strict")
                .build();

        response.addHeader("Set-Cookie", accessCookie.toString());
        response.addHeader("Set-Cookie", refreshCookie.toString());

        return ResponseDTO.ok(null);
    }
    // Token 을 Cookie 에서 제거.


    /* 중복 검사 */
    /* 3. 이메일 중복 검사 */
    @GetMapping("validate/email")
    public ResponseDTO<BooleanResponseDTO> validationCheckByEmail(
            @RequestBody RequestEmailDTO requestEmailDTO) {

        BooleanResponseDTO booleanResponseDTO = userService.
                checkValidationByEmailForDuplicate(
                        requestEmailDTO.getEmail());

        return ResponseDTO.ok(booleanResponseDTO);
    }

    /* 4. 아이디 중복 검사 */
    @GetMapping("/validate/authId")
    public ResponseDTO<BooleanResponseDTO> validationCheckByUserAuthId(
            @RequestBody RequestLoginIdDTO requestLoginIdDTO) {

        BooleanResponseDTO booleanResponseDTO = userService.
                checkValidationByUserAuthIdForDuplicate(
                        requestLoginIdDTO.getUserAuthId());

        return ResponseDTO.ok(booleanResponseDTO);

    }
    /* 5. 닉네임 중복 검사 */
    @GetMapping("/validate/nickname")
    public ResponseDTO<BooleanResponseDTO> validationCheckByNickname(
            @RequestBody RequestNicknameDTO requestNickname) {

        BooleanResponseDTO booleanResponseDTO = userService.
                checkValidationByNicknameForDuplicate(
                        requestNickname.getNickname());

        return ResponseDTO.ok(booleanResponseDTO);

    }

    /* Token */
    /* 6.Refresh Token으로 AccessToken 재발급 (추후 RefreshToken도 함께 재발급 받도록 수정할 예정 )*/
    @PostMapping("/api/auth/refresh")
    public ResponseDTO<Void> refreshToken(
            HttpServletRequest request,
            HttpServletResponse response) {

        // Cookie에서 RefreshToken 읽기
        String refreshToken = null;
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("refreshToken".equals(cookie.getName())) {
                    refreshToken = cookie.getValue();
                    break;
                }
            }
        }

        if (refreshToken == null) {
            throw new CommonException(ErrorCode.INVALID_TOKEN_ERROR);
        }

        // 새로운 AccessToken 발급
        AuthTokens newTokens = jwtUtil.refreshAccessToken(refreshToken);

        // 새 AccessToken을 Cookie로 설정
        ResponseCookie accessCookie = ResponseCookie.from("accessToken", newTokens.getAccessToken())
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(3600) // 1시간
                .sameSite("Strict")
                .build();

        response.addHeader("Set-Cookie", accessCookie.toString());

        return ResponseDTO.ok(null);
    }


    /* 상태 관리 */
    /* 7. 회원 상태 변경 ( Active ) */
    @PatchMapping("/{userId}/active")
    public ResponseDTO<String> activateUser(
            @PathVariable("userId") Long userId) {

        userService.
                changeUserStatusToActive(userId);

        return ResponseDTO.ok("유저 상태가 Active로 변경되었습니다.");

    }

    /* 8. 회원 상태 변경 ( inActive - 보류 )*/

    /* 9. 회원 상태 변경 ( 회원 탈퇴 - SoftDelete ) */
    @PatchMapping("/{userId}/delete")
    public ResponseDTO<String> deleteUser(
            @PathVariable("userId") Long userId) {

        userService.
                changeUserStatusToDelete(userId);

        return ResponseDTO.ok("회원 탈퇴가 성공적으로 완료되었습니다.");
    }

    /* 9. 회원 블랙리스트 등록 */
//    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{userId}/black")
    public ResponseDTO<String> blackUser(
            @PathVariable("userId") Long userId) {

        userService.
                changeUserStatusToBlacked(userId);

        return ResponseDTO.ok("해당 회원을 블랙리스트에 등록하였습니다.");

    }

    /* 조회 */
    /* 10. 회원 정보 조회 */
    @GetMapping("/{userId}/userProfile")
    public ResponseDTO<UserDTO> getUser(@PathVariable("userId") Long userId) {

        UserDTO userDTO =
                userService.
                        getUserInfoByUserId(userId);

        return ResponseDTO.ok(userDTO);
    }

    /* 11.  회원 정보 조회 ( Zustand 에 user 정보 관리용으로 사용 ) */
    @GetMapping("/userProfile")
    public ResponseDTO<ZustandDataResponseDTO> getUserProfile(
    ) {
        String userAuthId = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("userAuthId :{}", userAuthId);

        // SecurityContextHolder 를 통해 유저의 정보를 가져올 수 없는 경우
        if (userAuthId == null) {
            throw new CommonException(ErrorCode.NOT_FOUND_AUTHENTICATION);
        }

        ZustandDataResponseDTO responseDTO =
                userService.
                        getZustandData(userAuthId);

        return ResponseDTO.ok(responseDTO);
    }

    /* 12. Email 로 userAuthId 조회 */
    @GetMapping("/find-authid/email")
    public ResponseDTO<ResponseUserAuthIdDTO> getUserAuthId(
            @RequestBody RequestEmailDTO requestEmailDTO
    ) {

        ResponseUserAuthIdDTO userAuthId =
                userService.
                        findUserAuthId(
                                requestEmailDTO.getEmail());

        return ResponseDTO.ok(userAuthId);
    }


    /* SMTP-Redis */
    /* 13. 회원가입 시 인증번호 이메일 전송 API */
    @PostMapping("/email-verification")
    public ResponseDTO<Void> createVerificationCode(
            @RequestBody RequestEmailDTO requestEmailDTO) {

        emailService.
                sendVerificationCode(
                        requestEmailDTO.getEmail());

        return ResponseDTO.ok(null);
    }


    /* 14. 인증번호 검증 API - URL에 Code 노출을 방지하기 위해 PostMapping 사용. */
    @PostMapping("/code-verification")
    public ResponseDTO<Void> verifyCode(
            @RequestBody RequestCodeVerificationDTO requestCodeVerificationDTO) {

        emailService.
                verificationCode(
                        requestCodeVerificationDTO.getEmail(),
                        requestCodeVerificationDTO.getVerificationCode());

        return ResponseDTO.ok(null);
    }

    /** 아이디 찾기 & 비밀번호 재설정 **/
    /* 15-1. 아이디&비밀번호 찾기 Step01 - email 로 userAuthId 검증 후 Verification Code 발송 (Oauth2 를 통한 회원가입을 한 유저는 사용 불가.) */
    @PostMapping("/find-authid")
    public ResponseDTO<Void> checkUserAuthId(
            @RequestBody RequestEmailDTO requestEmailDTO
    ) {

        userService.
                findUserAuthIdByEmail(
                        requestEmailDTO.getEmail());

        return ResponseDTO.ok(null);
    }

    /* 15-2, 아이디 찾기 Step02 - Redis에 있는 Verification Code 검증 후 아이디 Eamil로 발송 */
    @PostMapping("/send-authid")
    public ResponseDTO<Void> sendUserAuthId(
            @RequestBody RequestCodeVerificationDTO requestCodeVerificationDTO) {

        ResponseUserAuthIdDTO responseUserAuthIdDTO = userService.
                findUserAuthId(requestCodeVerificationDTO.getEmail());
        log.info(" responseUserAuthIdDTO 가 값을 잘 반환받았는지 확인 {}", responseUserAuthIdDTO);

        String userAuthId = responseUserAuthIdDTO.getUserAuthId();

        emailService.
                sendUserAuthId(
                        requestCodeVerificationDTO.getEmail(),
                        requestCodeVerificationDTO.getVerificationCode(),
                        userAuthId);

        return ResponseDTO.ok(null);
    }

    /* 15-3, 비로그인 사용자 비밀번호 재설정 step01 - VerificationCode 검증 및 일회성 Token 발급 후 비밀번호 재설정 URL 발송 */
    @PostMapping("/check-send-reset-url")
    public ResponseDTO<Void> SendPwdResetUrl(
            @RequestBody RequestCodeVerificationDTO requestCodeVerificationDTO) {

        emailService.
                sendUserPasswordResetUrl(
                        requestCodeVerificationDTO.getEmail(),
                        requestCodeVerificationDTO.getVerificationCode());

        return ResponseDTO.ok(null);

    }
    /* 15-4. 비로그인 사용자 비밀번호 재설정 step02 - Token 유효성 검증 후 비밀번호 재설정을 위한 API */
    @PostMapping("/reset-pwd")
    public ResponseDTO<Void> resetPassword(
            @RequestBody RequestResetPasswordDTO requestResetPasswordDTO
    ) {

        // 이메일 - Redis 에서 토큰 검증을 위한 정보
        userService.
                resetUserPassword(
                        requestResetPasswordDTO.getNewPassword(),
                        requestResetPasswordDTO.getConfirmPassword(),
                        requestResetPasswordDTO.getResetToken());

        return ResponseDTO.ok(null);

    }

    /* 16. 이메일 변경 */


    /** 회원 정보 변경 **/
    /* 17. 로그인 한 회원 비밀번호 변경 */
    @PatchMapping("{userId}/password")
    public ResponseDTO<String> updateLoginedUserPassword(
            @PathVariable("userId") Long userId,
            @RequestBody RequestLoginedUserPasswordChangeDTO requestLoginedUserPasswordChangeDTO
    ) {

        userService.
                changeUserPassword(
                        userId,
                        requestLoginedUserPasswordChangeDTO);

        return ResponseDTO.ok("비밀번호가 성공적으로 변경되었습니다.");
    }

    /* 18. 닉네임 변경 */
    @PatchMapping("/{userId}/nickname")
    public ResponseDTO<String> updateNickname(
            @PathVariable("userId") Long userId,
            @RequestBody RequestNicknameDTO requestNickname) {

        userService.
                changeUserNickname(
                        userId,
                        requestNickname.getNickname());
        return ResponseDTO.ok("닉네임이 성공적으로 변경되었습니다.");

    }

    /* 주소 변경 */

    /* 전화번호 변경 */

}
