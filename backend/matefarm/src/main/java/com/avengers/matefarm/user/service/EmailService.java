package com.avengers.matefarm.user.service;

import com.avengers.matefarm.common.exception.CommonException;
import com.avengers.matefarm.common.exception.ErrorCode;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.validation.constraints.Email;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.security.SecureRandom;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Validated  // Email 형식 검증을 Spring 에 위임
@Service
public class EmailService {

    private final StringRedisTemplate stringRedisTemplate;
    private final JavaMailSender mailSender;

    /* Redis TTl, Cooldown 설정. */
    /* email:Cooldown
       email:key 구조.
    * */
    private final long VERIFICATION_CODE_TTL = 5; // 5분
    private final long COOLDOWN_SECONDS = 30; // 30초
    private final long VERIFICATION_SUCCESS_TTL = 30; // 10분
    private final long RESET_PASSWORD_TOKEN_TTL = 30; // 30분


    public EmailService(StringRedisTemplate stringRedisTemplate1,
                         JavaMailSender mailSender) {

        this.stringRedisTemplate = stringRedisTemplate1;
        this.mailSender = mailSender;
    }

    @Async  // 메일 발송 비동기 처리
    public void sendVerificationCode(@Email String email) {     // @Email 로 Spring 에게 Email 형식 검증 위임
        /* @Validated 와 @Email 를 통해 이메일 형식을 검사.
         * 조건에 맞지 않으면 ConstraintViolationException 예외 발생.
         * 해당 예외는 GlobalExceptionHandler 가 전역으로 잡아서 처리.
        * */


        // 1. Cooldown 체크
        if (isCooldown(email)) {
            throw new CommonException(ErrorCode.TOO_MANY_REQUESTS); // 재전송 시간 30초 설정
        }

        // 2. 인증 코드 생성 및 저장
        String code = generateVerificationCode();   // 인증코드 생성
        sendVerificationCodeToRedis(email, code);   // 인증코드 저장
        saveCooldownTimestamp(email);               // cooldown 저장

        // 3. 메일 구성 및 전송
        try {
            // HTML 형식의 이메일 전송을 위한 MImeMessage 객체 생성.
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            // Email Title 정의
            helper.setTo(email);
            helper.setSubject("[MateFarm] 이메일 인증번호 안내");

            // HTML Template 가져오기. 및 저장
            String mailContent = HtmlContentForSignupVerification(code);
            helper.setText(mailContent, true);

            // Email 전송
            mailSender.send(message);
            log.info("인증 메일 발송 성공: {}", email);

        } catch (MessagingException e) {
            log.error("메일 발송 중 오류 발생: {}", e.getMessage());
        }
    }

    /* Redis에 코드 저장 */
    private void sendVerificationCodeToRedis(String email, String code) {
        stringRedisTemplate.opsForValue().set(email, code, VERIFICATION_CODE_TTL, TimeUnit.MINUTES);
    }

    /* 6자리의 인증코드 발행 */
    public String generateVerificationCode() {

        // 불변객체인 String을 대신하여 StringBuilder 사용.
        StringBuilder sb = new StringBuilder();
        SecureRandom random = new SecureRandom(); // 보안을 위해 SecureRandom 사용

        for (int i = 0; i < 6; i++) {
            // 0~9 사이의 숫자를 하나씩 뽑아서 붙임
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }

    //필기. 이메일 전송 쿨다운 체크
    private boolean isCooldown(String email) {
        String lastSentTime = stringRedisTemplate.opsForValue().get(email + ":cooldown"); // 쿨다운 키

        if (lastSentTime == null) {
            return false;
        }

        long lastSentTimestamp = Long.parseLong(lastSentTime);
        long currentTimestamp = System.currentTimeMillis() / 1000; // 초 단위

        log.error("재전송 가능 남은 시간: {}", currentTimestamp);

        return currentTimestamp - lastSentTimestamp < COOLDOWN_SECONDS;
    }

    // Redis에 쿨다운 시간 저장
    private void saveCooldownTimestamp(String email) {
        long currentTimestamp = System.currentTimeMillis() / 1000; // 초 단위
        stringRedisTemplate.opsForValue()
                .set(email + ":cooldown", String.valueOf(currentTimestamp), COOLDOWN_SECONDS, TimeUnit.SECONDS); // TTL 30초
    }

    /* 생성된 인증코드 검증 */
    public void verificationCode(String email, String verificationCode) {

        // 회원이 입력한 Code
        String redisCode = stringRedisTemplate.opsForValue().get(email);

        // 인증코드 만료 or 미발급
        if (redisCode == null) {
            throw new CommonException(ErrorCode.INVALID_INPUT_VALUE);
        }

        // 인증코드 불일치
        if(!redisCode.equals(verificationCode)) {
            throw new CommonException(ErrorCode.INVALID_INPUT_VALUE);
        }

        // email:verify 를 Redis 에 저장. TTL 설정 - 추후 userService 에서 회원가입 후 해당 verify 도 삭제  처리.
        stringRedisTemplate.opsForValue()
                .set(email + ":verified","true",  VERIFICATION_SUCCESS_TTL, TimeUnit.MINUTES); // TTL 30분
                log.info("이메일 인증코드 검증 성공 {}",verificationCode );

        // code 재사용 방지를 위해 인증 후 코드 삭제.
        stringRedisTemplate.delete(email);
        log.info("인증코드 검증 후 키값 삭제 완료{} ", email);
    }

    /* UserService 에서 위임된 userAuthId 전송용인 Service 계층에만 존재하는 API */
    public void sendUserAuthId(String email, String verificationCode, String userAuthId) {
        // 순환 참조 문제를 방지하기 위하여 userAuthId를 받아서 사용

        // 검증 진행
        verificationCode(email, verificationCode);

        // 메일 구성 및 전송
        try {
            // HTML 형식의 이메일 전송을 위한 MImeMessage 객체 생성.
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            // Email Title 정의
            helper.setTo(email);
            helper.setSubject("[MateFarm] 회원 아이디 안내");

            // HTML Template 가져오기. 및 저장
            String mailContent = HtmlContentForSendingUserAuthId(userAuthId);
            helper.setText(mailContent, true);

            // Email 전송
            mailSender.send(message);
            log.info("회원 아이디 메일 발송 성공 : {}", userAuthId);

        } catch (MessagingException e) {
            log.error("메일 발송 중 오류 발생 : {}", e.getMessage());
        }

    }

    /* UserService 에서 위임된 UserPasswordReset Url 전송용인 Service 계층에만 존재하는 API */
    public void sendUserPasswordResetUrl(String email, String verificationCode) {

        // 1. Cooldown 체크
        if (isCooldown(email)) {
            throw new CommonException(ErrorCode.TOO_MANY_REQUESTS); // 재전송 시간 30초 설정
        }
        // Cooldown 설정
        saveCooldownTimestamp(email);

        //검증 및 임시 Token 발급.
        verificationCode(email, verificationCode);
        String resetToken = UUID.randomUUID().toString(); // Redis에 저장될 Token

        /* 쿼리 파라미터 방식으로 임시 발급된 Token을 Url 뒤에 붙여 사용자에게 전달. Url : http://exampleUrl.com/reset-password*/
        /* 프론트 다 만들고 URL 설정하면 도메인 주소까지 다 바꿔서 URL 재설정 하기.*/
        String resetUrl = "https://matefarm.com/auth/password-reset?token=" + resetToken;
        stringRedisTemplate.opsForValue()
                .set("reset:password:"+resetToken , email,  RESET_PASSWORD_TOKEN_TTL, TimeUnit.MINUTES); // TTL 30분

        log.info("비밀번호 재전송 Token Redis에 발급 성공: {}",resetUrl );


        // 메일 구성 및 전송
        try {
            // HTML 형식의 이메일 전송을 위한 MImeMessage 객체 생성.
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            // Email Title 정의
            helper.setTo(email);
            helper.setSubject("[MateFarm] 비밀번호 재설정 안내");

            // HTML Template 가져오기. 및 저장
            String mailContent = HtmlContentForPasswordReset(resetUrl);
            helper.setText(mailContent, true);

            // Email 전송
            mailSender.send(message);
            log.info("비밀번호 재전송 메일 발송 성공 : {}", resetUrl);

        } catch (MessagingException e) {
            log.error("비밀번호 재전송 메일 발송 중 오류 발생 : {}", e.getMessage());
        }
    }

    /* UserService 에서 위임된 ResetToken 검증용인 Service 계층에만 존재하는 API */
    public String verificationPwdResetCode(String resetToken) {
        // 전달받은 Token을 reset:password:token으로 유저 email을 추출.
        String clientTokenValue = stringRedisTemplate.opsForValue().get("reset:password:"+resetToken); // 쿨다운 키

        // Token의 유효 기간이 만료 or 존재하지 않음.
        if(clientTokenValue == null) {
            throw new CommonException(ErrorCode.INVALID_TOKEN_ERROR);
        }

        log.info("Client에서 보낸 비밀번호 재설정을 위한 Token 유효성 검증 통과");

        // Token 재사용 방지를 위해 삭제.
        stringRedisTemplate.delete("reset:password"+resetToken);

        return clientTokenValue;
    }

    /** 메일 발송에 사용될 Email Templates **/
    /* 회원가입 시 사용되는 HTMl Template*/
    private String HtmlContentForSignupVerification(String code) {
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
            </head>
            <body style="margin: 0; padding: 0; font-family: 'Apple SD Gothic Neo', 'Malgun Gothic', sans-serif;">
                <div style="width: 100%%; max-width: 600px; margin: 0 auto; padding: 40px 20px; box-sizing: border-box;">
                    <div style="background-color: #ffffff; border: 1px solid #e1e4e8; border-radius: 12px; padding: 40px; text-align: center; box-shadow: 0 4px 12px rgba(0,0,0,0.05);">
                        <h1 style="color: #1a73e8; font-size: 24px; margin-bottom: 24px; font-weight: 700;">MateFarm</h1>
                        <div style="height: 1px; background-color: #f1f3f4; margin-bottom: 30px;"></div>
                        <p style="font-size: 16px; color: #3c4043; line-height: 1.6; margin-bottom: 30px;">
                            안녕하세요!<br>
                            서비스 이용을 위한 이메일 인증을 진행해 주세요.<br>
                            아래의 <strong>6자리 인증번호</strong>를 인증 화면에 입력해 주시기 바랍니다.
                        </p>
                        <div style="background-color: #f8f9fa; border-radius: 8px; padding: 25px; margin-bottom: 30px;">
                            <span style="font-size: 14px; color: #70757a; display: block; margin-bottom: 10px; font-weight: 600; letter-spacing: 1px; text-transform: uppercase;">Verification Code</span>
                            <div style="font-size: 36px; font-weight: 800; color: #1a73e8; letter-spacing: 10px; margin-left: 10px;">
                                %s
                            </div>
                        </div>
                        <p style="font-size: 14px; color: #d93025; margin-bottom: 40px;">
                            * 본 인증번호는 발송 후 <strong>5분간</strong>만 유효합니다.
                        </p>
                        <div style="height: 1px; background-color: #f1f3f4; margin-bottom: 24px;"></div>
                        <p style="font-size: 12px; color: #70757a; line-height: 1.5; margin: 0;">
                            본 메일은 시스템에 의해 자동으로 발송되었습니다.<br>
                            만약 인증을 요청하지 않으셨다면 이 메일을 무시해 주세요.<br><br>
                            © 2026 MateFarm. All rights reserved.
                        </p>
                    </div>
                </div>
            </body>
            </html>
            """, code);
    }

    /* 아이디 찾기 시에 사용되는 HTMl Template*/
    private String HtmlContentForSendingUserAuthId(String userAuthId) {
        return String.format("""
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="UTF-8">
        </head>
        <body style="margin: 0; padding: 0; font-family: 'Apple SD Gothic Neo', 'Malgun Gothic', sans-serif;">
            <div style="width: 100%%; max-width: 600px; margin: 0 auto; padding: 40px 20px; box-sizing: border-box;">
                <div style="background-color: #ffffff; border: 1px solid #e1e4e8; border-radius: 12px; padding: 40px; text-align: center; box-shadow: 0 4px 12px rgba(0,0,0,0.05);">
                    <h1 style="color: #1a73e8; font-size: 24px; margin-bottom: 24px; font-weight: 700;">MateFarm</h1>
                    <div style="height: 1px; background-color: #f1f3f4; margin-bottom: 30px;"></div>
                    <p style="font-size: 16px; color: #3c4043; line-height: 1.6; margin-bottom: 30px;">
                        안녕하세요!<br>
                        요청하신 <strong>아이디 찾기</strong> 결과 안내드립니다.<br>
                        가입하신 계정의 정보는 아래와 같습니다.
                    </p>
                    <div style="background-color: #f8f9fa; border-radius: 8px; padding: 25px; margin-bottom: 30px;">
                        <span style="font-size: 14px; color: #70757a; display: block; margin-bottom: 10px; font-weight: 600; letter-spacing: 1px; text-transform: uppercase;">Login ID</span>
                        <div style="font-size: 28px; font-weight: 800; color: #1a73e8; margin-top: 10px;">
                            %s
                        </div>
                    </div>
                    <p style="font-size: 14px; color: #3c4043; margin-bottom: 40px;">
                        정보보호를 위해 아이디의 일부만 표시될 수 있습니다.<br>
                        보안을 위해 로그인 후 비밀번호를 주기적으로 변경해 주세요.
                    </p>
                    <div style="height: 1px; background-color: #f1f3f4; margin-bottom: 24px;"></div>
                    <p style="font-size: 12px; color: #70757a; line-height: 1.5; margin: 0;">
                        본 메일은 시스템에 의해 자동으로 발송되었습니다.<br>
                        만약 아이디 찾기를 요청하지 않으셨다면 본 메일을 무시해 주세요.<br><br>
                        © 2026 MateFarm. All rights reserved.
                    </p>
                </div>
            </div>
        </body>
        </html>
        """, userAuthId);
    }

    /* 비밀번호 재설정 시에 사용되는 HTML Template */
    private String HtmlContentForPasswordReset(String resetPasswordUrl) {
        return String.format("""
    <!DOCTYPE html>
    <html>
    <head>
        <meta charset="UTF-8">
    </head>
    <body style="margin: 0; padding: 0; font-family: 'Apple SD Gothic Neo', 'Malgun Gothic', sans-serif;">
        <div style="width: 100%%; max-width: 600px; margin: 0 auto; padding: 40px 20px; box-sizing: border-box;">
            <div style="background-color: #ffffff; border: 1px solid #e1e4e8; border-radius: 12px; padding: 40px; text-align: center; box-shadow: 0 4px 12px rgba(0,0,0,0.05);">

                <h1 style="color: #1a73e8; font-size: 24px; margin-bottom: 24px; font-weight: 700;">
                    MateFarm
                </h1>

                <div style="height: 1px; background-color: #f1f3f4; margin-bottom: 30px;"></div>

                <p style="font-size: 16px; color: #3c4043; line-height: 1.6; margin-bottom: 30px;">
                    안녕하세요!<br>
                    요청하신 <strong>비밀번호 재설정</strong> 안내드립니다.<br>
                    아래 버튼을 클릭하여 비밀번호를 재설정해 주세요.
                </p>

                <!-- 비밀번호 재설정 버튼 -->
                <a href="%s"
                   style="
                       display: inline-block;
                       padding: 16px 32px;
                       background-color: #1a73e8;
                       color: #ffffff;
                       text-decoration: none;
                       border-radius: 8px;
                       font-size: 16px;
                       font-weight: 600;
                       margin-bottom: 30px;
                   ">
                    비밀번호 재설정
                </a>

                <p style="font-size: 14px; color: #d93025; margin-bottom: 40px;">
                    * 본 링크는 보안을 위해 <strong>일정 시간 동안만 유효</strong>합니다.<br>
                    * 링크가 만료되었을 경우 다시 비밀번호 찾기를 진행해 주세요.
                </p>

                <div style="height: 1px; background-color: #f1f3f4; margin-bottom: 24px;"></div>

                <p style="font-size: 12px; color: #70757a; line-height: 1.5; margin: 0;">
                    본 메일은 시스템에 의해 자동으로 발송되었습니다.<br>
                    만약 비밀번호 재설정을 요청하지 않으셨다면 이 메일을 무시해 주세요.<br><br>
                    © 2026 MateFarm. All rights reserved.
                </p>

            </div>
        </div>
    </body>
    </html>
    """, resetPasswordUrl);
    }

}



/*
 google Id : mingeunstmp@gmail.com
 pwd : smtpmingeun!
 */