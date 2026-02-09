package com.avengers.matefarm.security;



import com.avengers.matefarm.common.ResponseDTO;
import com.avengers.matefarm.common.exception.CommonException;
import com.avengers.matefarm.user.service.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;

@Slf4j
//필기. JwtFilter가 OncePerRequestFilter를 상속 받는 이유는 doFilterInternal를 오버라이딩 한다.
// (한번만 실행되는 필터)

public class JwtFilter extends OncePerRequestFilter {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    public JwtFilter(UserService userService, JwtUtil jwtUtil) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }

    /*설명. 들고 온(Request Header) 토큰이 유효한지 판별 및 인증(Authentication 객체로 관리)*/
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        log.info("UsernamePasswordAuthenticationFilter보다 먼저 동작하는 필터");

        String authorizationHeader = request.getHeader("Authorization");
        log.info("Authorization header: {}", authorizationHeader);

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String token = authorizationHeader.substring(7);
            log.info("토큰 값: " + token);

            try {
                if (jwtUtil.validateToken(token)) {
                    Authentication authentication = jwtUtil.getAuthentication(token);
                    log.info("JwtFilter를 통과한 유효한 토큰을 통해 security가 관리할 principal 객체: {}", authentication);
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            } catch (CommonException ex) {
                // 예외를 잡고 ResponseDTO 형식으로 응답
                response.setStatus(ex.getErrorCode().getHttpStatus().value());
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");  // 응답 인코딩을 UTF-8로 설정

                ResponseDTO<Object> errorResponse = ResponseDTO.fail(ex);
                ObjectMapper objectMapper = new ObjectMapper();
                String jsonResponse = objectMapper.writeValueAsString(errorResponse);

                response.getWriter().write(jsonResponse);
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}
