package com.avengers.matefarm.security;

import com.avengers.matefarm.user.service.UserService;

import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
public class WebSecurity {


    // 비밀번호 암호화를 위한 Encoder
    private BCryptPasswordEncoder bCryptPasswordEncoder;
    // UserDetailsService 구현체 (DB에서 사용자 조회)
    private UserService userService;
    // application.yml 환경 변수 접근
    private Environment env;
    // JWT 생성 및 검증 유틸
    private JwtUtil jwtUtil;


    @Autowired
    public WebSecurity(BCryptPasswordEncoder bCryptPasswordEncoder,
                       UserService userService,
                       Environment env,
                       JwtUtil jwtUtil) {
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.userService = userService;
        this.env = env;
        this.jwtUtil = jwtUtil;
    }


    @Bean
    protected SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // CORS 활성화 및 CSRF 비활성화
        http.cors( cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf( csrf -> csrf.disable())
            .formLogin( form -> form.disable());    // Spring Security 에서 기본으로 제공해주는 Session 방식 로그인 Form 비활성화

        // 필기. AuthenticationManager 설정 ( AuthenticationManager는 Spring이 내부적으로 AuthenticationProvider에게 인증 권한 위임.
        AuthenticationManagerBuilder authenticationManagerBuilder =
                http.getSharedObject(AuthenticationManagerBuilder.class);
        // 필기. userService -> userDetailsService 구현체. DB에서 유저 조회 및 비밀번호 비교 가능
        authenticationManagerBuilder
                .userDetailsService(userService)    // userDetailsService 에서 Interface에 있는 loadBy~ 오버라이딩 후 구현해야함.
                .passwordEncoder(bCryptPasswordEncoder);    // config에 해당 암호화 설정 추가, @Bean 등록 필요

        // AuthenticationManager 생성
        AuthenticationManager authenticationManager = authenticationManagerBuilder.build();

        // 요청 API 권한 설정 (추후 역할에 따라 Customizing )
        http.authorizeHttpRequests(auth -> auth
            .requestMatchers("/login", "/signup").permitAll()
            .requestMatchers("/api/ai-chat/vision/**").permitAll()
            .requestMatchers("/api/ai-chat/**").authenticated()
            //.requestMatchers("/**").permitAll()     // security 적용 시점에 수정
            .anyRequest().permitAll()             // permitAll() 설정 외 모든 api 인증 필요
        )

        .exceptionHandling(e -> e
        .authenticationEntryPoint((req, res, ex) -> {
            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        })
        .accessDeniedHandler((req, res, ex) -> {
            res.setStatus(HttpServletResponse.SC_FORBIDDEN);
        })
    )

        .authenticationManager(authenticationManager)   // 인증 매니저 등록
        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))   /* 설명. session 방식을 사용하지 않음(JWT Token 방식 사용 시 설정할 내용) */
        .addFilter(getAuthenticationFilter(authenticationManager))
        .addFilterBefore(new JwtFilter(userService, jwtUtil), UsernamePasswordAuthenticationFilter.class);  // api 요청 시 JWT 검증 하도록 필터에 추가

        return http.build();
    }


    // 필기. 커스텀 로그인 필터 생성

    private AuthenticationFilter getAuthenticationFilter(AuthenticationManager authenticationManager) {
        AuthenticationFilter authenticationFilter = new AuthenticationFilter(authenticationManager, bCryptPasswordEncoder, userService, env);
        authenticationFilter.setAuthenticationFailureHandler(authenticationFailureHandler());
        return authenticationFilter;
    }



    @Bean
    public AuthenticationFailureHandler authenticationFailureHandler() {
        return new CustomAuthenticationFailureHandler();
    }

    // 필기. CORS 설정
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowCredentials(true);    // Credentials Allow
                configuration.setAllowedOrigins(List.of("https://matefarm.click")); //  배포 후 도메인 변경
//        configuration.setAllowedOrigins(List.of("http://localhost:3000")); // Allow frontend ( React localhost )
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));   // 허용할 HTTP Method 지정
        configuration.setAllowedHeaders(List.of("*")); // Allow all headers

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration); // 모든 경로에 대해 설정 적용
        return source;
    }


}
