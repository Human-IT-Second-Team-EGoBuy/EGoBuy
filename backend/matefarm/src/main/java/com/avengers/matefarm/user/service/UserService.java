package com.avengers.matefarm.user.service;

import com.avengers.matefarm.common.exception.CommonException;
import com.avengers.matefarm.common.exception.ErrorCode;
import com.avengers.matefarm.user.dto.UserDTO;
import com.avengers.matefarm.user.dto.UserEntity;
import com.avengers.matefarm.user.dto.enums.ActiveStatus;
import com.avengers.matefarm.user.dto.enums.SignupPath;
import com.avengers.matefarm.user.dto.enums.UserRole;
import com.avengers.matefarm.user.dto.oauth2.google.GoogleUserProfile;
import com.avengers.matefarm.user.dto.oauth2.kakao.KakaoUserProfile;
import com.avengers.matefarm.user.dto.oauth2.naver.NaverUserProfile;
import com.avengers.matefarm.user.dto.request.RequestLoginedUserPasswordChangeDTO;
import com.avengers.matefarm.user.dto.request.RequestUserRegistVO;
import com.avengers.matefarm.user.dto.response.ResponseUserAuthIdDTO;
import com.avengers.matefarm.user.dto.validate.BooleanResponseDTO;
import com.avengers.matefarm.user.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;


@Slf4j
@Service
// 설명. UserDetailsService 인터페이스 구현체
public class UserService implements UserDetailsService {
    private final EmailService emailService;
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    public UserService(EmailService emailService,
                       UserRepository userRepository,
                       BCryptPasswordEncoder bCryptPasswordEncoder) {

        this.emailService = emailService;
        this.userRepository = userRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    /* 설명. 로그인 시 security가 자동으로 호출하는 메소드 */
    @Override
    public UserDetails loadUserByUsername(String userAuthId) throws UsernameNotFoundException {
        // 1. userAuthId를 기준으로 사용자 조회
        UserEntity loginUser = userRepository.findByUserAuthId(userAuthId)
                .orElseThrow(() -> new CommonException(ErrorCode.UNAUTHORIZED_ACCESS));

        // 2. 비밀번호 처리 (소셜 로그인 시 비밀번호가 없을 경우 기본값 설정)
        String encryptedPwd = loginUser.getEncryptedPwd();
        if (encryptedPwd == null) {
            encryptedPwd = "{noop}";  // 비밀번호가 없을 경우 기본값 설정
        }

        // 3. 권한 정보를 userRole 필드에서 가져와서 변환
        List<GrantedAuthority> grantedAuthorities = Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_" + loginUser.getUserRole().name()) // "ROLE_ADMIN" 또는 "ROLE_ENTERPRISE"
        );

        // 4. UserDetails 객체 반환
        return new User(loginUser.getUserAuthId(), encryptedPwd,
                true, true, true, true,
                grantedAuthorities);
    }

    // 설명. userAuthId로 사용자 조회
    public UserEntity findByUserAuthId(String userAuthId) {
        return userRepository.findByUserAuthId(userAuthId)
                .orElseThrow( () -> new CommonException(ErrorCode.NOT_FOUND_USER));
    }

    /* 설명. 일반 회원가입 메소드 */
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public UserDTO registUser(RequestUserRegistVO newUser) {

        // 1. 동일한 UserAuthId가 존재하는지 확인 (중복 검증)
        userRepository.findByUserAuthId(newUser.getUserAuthId())
                .ifPresent(user -> {
                    throw new CommonException(ErrorCode.EXIST_USER);
                });

//        // 2. 이메일 인증 여부 확인 (이메일이 있을 때만)
//        if (newUser.getEmail() != null && !newUser.getEmail().isEmpty()) {
//            String emailVerificationStatus = stringRedisTemplate.opsForValue().get(newUser.getEmail());
//            if (!"True".equals(emailVerificationStatus)) {
//                throw new CommonException(ErrorCode.EMAIL_VERIFICATION_REQUIRED);
//            }
//        }

        // 3. 닉네임 중복 검증
        if (newUser.getNickname() == null || newUser.getNickname().isEmpty()) {
            throw new CommonException(ErrorCode.INVALID_INPUT_NICKNAME);
        }

//        userRepository.findByNickname(newUser.getNickname())
//                .ifPresent(user -> {
//                    throw new CommonException(ErrorCode.DUPLICATE_NICKNAME_EXISTS);
//                });

        // 4. 기본 프로필 이미지 설정 (추후 S3로 교체 가능)
//        String defaultProfileImageUrl = "https://yoribogobucket.s3.ap-northeast-2.amazonaws.com/default_profile.png";

        // 5. UserEntity 생성
        UserEntity userEntity = new UserEntity();
        userEntity.setUserAuthId(newUser.getUserAuthId());
        userEntity.setUserName(newUser.getUserName());
        userEntity.setEmail(newUser.getEmail());
        userEntity.setSignupPath(SignupPath.NORMAL);
        userEntity.setCreatedAt(LocalDateTime.now().withNano(0));
//        userEntity.setAcceptStatus(AcceptStatus.Y);
        userEntity.setUserStatus(ActiveStatus.ACTIVE);
        userEntity.setNickname(newUser.getNickname());
//        userEntity.setProfileImage(defaultProfileImageUrl);
//        userEntity.setUserLikes(0L);
        userEntity.setUserIdentifier("NORMAL_" + newUser.getUserAuthId());
        userEntity.setUserRole(UserRole.USER);  // 일반 사용자로 설정

        // 6. Tier 설정 (브론즈 티어, ID = 1)
//        Tier bronzeTier = new Tier();
//        bronzeTier.setTierId(1L);  // 티어 ID 설정
//        userEntity.setTier(bronzeTier);

        // 7. 비밀번호 암호화
        userEntity.setEncryptedPwd(bCryptPasswordEncoder.encode(newUser.getPassword()));

        // 8. Entity 저장 후 반환된 Entity 가져오기
        UserEntity savedEntity = userRepository.save(userEntity);

        // 9. 회원가입 성공 후 Redis에서 이메일 인증 키 삭제
//        if (newUser.getEmail() != null && !newUser.getEmail().isEmpty()) {
//            stringRedisTemplate.delete(newUser.getEmail());
//        }

        // 10. 저장된 Entity를 DTO로 변환하여 반환
        return convertToUserDTO(savedEntity);
    }


    /* Duplication Check */
    /* Nickname 중복 체크 메소드 */
    public BooleanResponseDTO checkValidationByNicknameForDuplicate(String nickname) {

        return new BooleanResponseDTO(userRepository.existsByNickname(nickname));
    }

    /* UserAuthID 중복 체크 메소드 */
    public BooleanResponseDTO checkValidationByUserAuthIdForDuplicate(String userAuthId) {

        return new BooleanResponseDTO(userRepository.existsByUserAuthId(userAuthId));
    }

    /* Email 중복 체크 메소드 */
    public BooleanResponseDTO checkValidationByEmailForDuplicate(String email) {

        return new BooleanResponseDTO(userRepository.existsByEmail(email));
    }

    @Transactional
    public void changeUserStatusToDelete(Long userId) {
        // user Check
        UserEntity userEntity = userRepository.findById(userId)
                .orElseThrow(() -> new CommonException(ErrorCode.NOT_FOUND_USER));  // NullPointException 방지를 위해 추가

        userEntity.deleteUser();
        userRepository.save(userEntity);
    }

    /* 닉네임 변경 메소드 */
    public void changeUserNickname(Long userId, String nickname) {
        UserEntity userEntity = userRepository.findById(userId)
                .orElseThrow(()-> new CommonException(ErrorCode.NOT_FOUND_USER));   // NullPointException 방지를 위해 추가

        // 닉네임 중복 체크
        if (userRepository.existsByNickname(nickname)) {
            throw new CommonException(ErrorCode.DUPLICATE_NICKNAME);
        } else {
            userEntity.setNickname(nickname);
            userRepository.save(userEntity);
        }
    }


    // UserInfo 반환 메소드
    public UserDTO getUserInfoByUserId(Long userId) {
        UserEntity userEntity = userRepository.findById(userId)
                .orElseThrow(() -> new CommonException(ErrorCode.NOT_FOUND_USER));

        return convertToUserDTO(userEntity);
    }

    /* UserStatus -> Active 메소드 */
    public void changeUserStatusToActive(Long userId) {
        UserEntity userEntity = userRepository.findById(userId)
                .orElseThrow(() -> new CommonException(ErrorCode.NOT_FOUND_USER));

        userEntity.setUserStatus(ActiveStatus.ACTIVE);
        userRepository.save(userEntity);
    }

    /* UserStatus -> Blacked 메소드*/
    public void changeUserStatusToBlacked(Long userId) {
        UserEntity userEntity = userRepository.findById(userId)
                .orElseThrow(() -> new CommonException(ErrorCode.NOT_FOUND_USER));

        userEntity.setUserStatus(ActiveStatus.BLACKED);
        userRepository.save(userEntity);
    }

    /* 로그인 한 유저의 패스워드 변경 메소드 */
    public void changeUserPassword(Long userId, RequestLoginedUserPasswordChangeDTO requestLoginedUserPasswordChangeDTO) {
        UserEntity userEntity = userRepository.findById(userId)
                .orElseThrow(() -> new CommonException(ErrorCode.NOT_FOUND_USER));

        /* 설명.
        5라는 값을 넣었을 때 aaa라는 해시값이 나왔다고 가정해보자,
        해싱을 하기 전, Salting 값으로 111을 추가해서 최종적으로 hash(5111) 로 해시값을 뽑는 것이고,
        OldPassword가 똑같이 5라고 가정한다면, 비밀번호 중복 검사는
        다시 해싱된 값을 DB에서 꺼내와서 Salt값을 추출, 사용자가 입력한 OldPassword에 Salting을 진행.

        따라서 똑같은 값을 해시 함수에 넣음. hash(5111) , 그리고 이 결과를 DB에서 추출한 해시값과 비교함.
        이러한 과정을 추상화한게 matches 메소드.

        matches() 메소드는 PasswordEncoder 인터페이스에 정의되며,
        AbstractValidatingPasswordEncoder 라는 인터페이스 하위 구현체가 내부 로직을 구현하며
        BCryptPasswordEncoder 클래스가 AbstractValidatingPasswordEncoder 를 상속받아 사용하는 구조이다.
         *
        * */

        /* matches(rawPassword, encodedPassword) 함수로 동일한 비밀번호로 변경 여부 검사 */

        // OldPassword가 기존의 비밀번호와 일치하지 않으면 보내는 에러.
        if (!bCryptPasswordEncoder.matches(
                requestLoginedUserPasswordChangeDTO.getCurrentPassword(),
                userEntity.getEncryptedPwd())) {
            throw new CommonException(ErrorCode.INVALID_PASSWORD);
        }

        // OldPassword가 일치하는 경우 NewPassword로 비밀번호 변경 진행.
        if (bCryptPasswordEncoder.matches(
                requestLoginedUserPasswordChangeDTO.getNewPassword(),
                userEntity.getEncryptedPwd())) {
            throw new CommonException(ErrorCode.DUPLICATE_PASSWORD);
        }

        // 모든 검증을 마친 뒤, bCrypt 암호화를 통해 Entity에 저장 후 Save
        String encodedNewPassword = bCryptPasswordEncoder.encode(
                requestLoginedUserPasswordChangeDTO.getNewPassword());

        userEntity.setEncryptedPwd(encodedNewPassword);
        userRepository.save(userEntity);

    }

    /* 아이디 & 비밀번호 찾기 시, 입력한 Email로 UserAuthId 조회 및 Redis에 Verification Code 생성하는 메소드. */
    public void findUserAuthIdByEmail(String email) {
        // userAuthId 조회
        UserEntity userEntity = userRepository.findByEmail(email)
                .orElseThrow(() -> new CommonException(ErrorCode.NOT_FOUND_USER));

        // VerificationCode 생성
        emailService.sendVerificationCode(email);

    }

    /* Email 로 UserAuthId 찾는 메소드 */
    public ResponseUserAuthIdDTO findUserAuthId(String email) {

        String userAuthId = userRepository.findUserAuthIdByEmail(email);
        log.info("userAuthId가 넘어오는지 확인 : {}", userAuthId);
        if (userAuthId == null) {
            throw new CommonException(ErrorCode.NOT_FOUND_USER);
        }

        ResponseUserAuthIdDTO responseDTO = new ResponseUserAuthIdDTO(userAuthId);
        log.info("responseDTO에 userAuthId가 잘 담겼는지 확인 : {}", responseDTO.getUserAuthId());
        return responseDTO;
    }

    public void resetUserPassword(String newPassword, String confirmPassword, String resetToken) {

        // EmailService에 Token 검증 로직 위임
        String email = emailService.verificationPwdResetCode(resetToken);

        UserEntity userEntity  = userRepository.findUserByEmail(email)
                .orElseThrow(() -> new CommonException(ErrorCode.NOT_FOUND_USER));

        String originPassword = userEntity.getEncryptedPwd();

        /* 비밀번호 재설정
         * 1. 입력한 비밀번호가 서로 일치하는지.
         * 2. 바꾸려는 비밀번호가 현재 비밀번호와 동일한지
         * 3. (예정) 우리 사이트의 비밀번호 변경 정책을 준수하는지
         * 4. 수정.
        * */

        if (!newPassword.equals(confirmPassword)) {
            throw new CommonException(ErrorCode.NOT_MATCHES_PASSWORD);
        }

        if (bCryptPasswordEncoder.matches(originPassword, newPassword)) {
            throw new CommonException(ErrorCode.INVALID_PASSWORD);
        }

        // 모든 검증을 마친 뒤, bCrypt 암호화를 통해 Entity에 저장 후 Save
        String encodedNewPassword = bCryptPasswordEncoder.encode(newPassword);

        userEntity.setEncryptedPwd(encodedNewPassword);
        userRepository.save(userEntity);
    }

    /* userIdentifier로 사용자 조회 */
    public UserEntity findByUserIdentifier(String userIdentifier) {
        return userRepository.findByUserIdentifier(userIdentifier)
                .orElseThrow(() -> new CommonException(ErrorCode.NOT_FOUND_USER));

    }
    /* 소셜 로그인의 회원가입 및 로그인 로직에 전용으로 사용되는 Optional 자체를 반환하는 메소드 */
    public Optional<UserEntity> findOptionalByUserIdentifier(String userIdentifier) {
        return userRepository.findByUserIdentifier(userIdentifier);
    }

    // UserEntity -> UserDTO 변환 메서드
    private UserDTO convertToUserDTO(UserEntity userEntity) {
        return UserDTO.builder()
                .userId(userEntity.getUserId())
                .userName(userEntity.getUserName())
                .password(userEntity.getEncryptedPwd())
                .nickname(userEntity.getNickname())
                .email(userEntity.getEmail())
                .userAuthId(userEntity.getUserAuthId())
                .userStatus(userEntity.getUserStatus())
                .createdAt(userEntity.getCreatedAt())
                .deletedAt(userEntity.getDeletedAt())
                .signupPath(userEntity.getSignupPath())
                .userRole(userEntity.getUserRole())
                .userIdentifier(userEntity.getUserIdentifier())
                .build();
    }

    /* 네이버 회원가입 로직. */
    public UserEntity createNaverLoginUser(NaverUserProfile userProfile, String userIdentifier) {

        UserEntity newUser = new UserEntity();

        newUser.setUserAuthId(userProfile.getId()); // 네이버에서 제공하는 네이버 사용자 식별자 값
        newUser.setUserIdentifier(userIdentifier);
        newUser.setUserName(userProfile.getName());
        newUser.setNickname(userProfile.getNickname());
        newUser.setEmail(userProfile.getEmail());
        newUser.setPhoneNumber(userProfile.getMobile());
        newUser.setUserIdentifier(userIdentifier);
        newUser.setCreatedAt(LocalDateTime.now().withNano(0));
        newUser.setUserStatus(ActiveStatus.ACTIVE);
        newUser.setSignupPath(SignupPath.NAVER);
        newUser.setUserRole(UserRole.USER);

        userRepository.save(newUser);
        log.info("네이버 로그인 유저 회원가입 완료:{}", newUser);
        return newUser;

    }

    /* 카카오 회원가입 로직 */
    public UserEntity createKakaoLoginUser(KakaoUserProfile kakaoUserProfile, String userIdentifier) {

        UserEntity newUser = new UserEntity();

        newUser.setUserAuthId(kakaoUserProfile.getId()); // 네이버에서 제공하는 네이버 사용자 식별자 값
        newUser.setUserIdentifier(userIdentifier);
        newUser.setUserName(kakaoUserProfile.getName());
        newUser.setNickname(kakaoUserProfile.getNickname());
        newUser.setEmail(kakaoUserProfile.getEmail());
        newUser.setPhoneNumber(kakaoUserProfile.getMobile());
        newUser.setUserIdentifier(userIdentifier);
        newUser.setCreatedAt(LocalDateTime.now().withNano(0));
        newUser.setUserStatus(ActiveStatus.ACTIVE);
        newUser.setSignupPath(SignupPath.KAKAO);
        newUser.setUserRole(UserRole.USER);

        userRepository.save(newUser);
        log.info("카카오 로그인 유저 회원가입 완료:{}", newUser);
        return newUser;
    }

    /* 네이버 회원가입 로직 */
    public UserEntity createGoogleLoginUser(GoogleUserProfile googleUserProfile, String userIdentifier) {

        UserEntity newUser = new UserEntity();

        newUser.setUserAuthId(googleUserProfile.getId()); // 구글에서 제공하는 구글 사용자 식별자 값 ( sub )
        newUser.setUserIdentifier(userIdentifier);
        newUser.setUserName(googleUserProfile.getName());
        newUser.setNickname(googleUserProfile.getNickname());
        newUser.setEmail(googleUserProfile.getEmail());
//        newUser.setPhoneNumber(googleUserProfile.getMobile());        // 구글은 Phone_number가 null일 가는성이 높아 별도로 입력받는 것으로 함.
        newUser.setUserIdentifier(userIdentifier);
        newUser.setCreatedAt(LocalDateTime.now().withNano(0));
        newUser.setUserStatus(ActiveStatus.ACTIVE);
        newUser.setSignupPath(SignupPath.GOOGLE);
        newUser.setUserRole(UserRole.USER);

        userRepository.save(newUser);
        log.info("구글 로그인 유저 회원가입 완료:{}", newUser);
        return newUser;
    }

    /* */
    public UserEntity findUserById(Long userId) {

        return userRepository.findById(userId)
                .orElseThrow(() -> new CommonException(ErrorCode.NOT_FOUND_USER));
    }
}
