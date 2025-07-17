package com.example.momo.global.filter;

import com.example.momo.domain.auth.dto.AuthUser;
import com.example.momo.global.utils.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.List;

@RequiredArgsConstructor
@Slf4j
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String authorization = request.getHeader("Authorization");

        // Authorization 해더 값이 없으면 다음 filter로 넘김
        if (!StringUtils.hasText(authorization) || !authorization.startsWith("Bearer ")) {
            log.info("토큰이 존재하지 않습니다.");
            filterChain.doFilter(request, response);
            return;
        }
        try {

            // 앞에 Bearer 부분 제거 후 순수 토큰만 획득
            String token = jwtUtil.subStringToken(authorization);

            Long userId = jwtUtil.getUserId(token);
            String email = jwtUtil.getEmail(token);
            String nickname = jwtUtil.getNickname(token);
            String role = jwtUtil.getRole(token);

            AuthUser authUser = new AuthUser(userId, email, nickname);

            // 스프링 시큐리티 인증 토큰 생성
            Authentication authToken = new UsernamePasswordAuthenticationToken(
                    authUser,
                    "",
                    List.of(new SimpleGrantedAuthority("ROLE_" + role))
            );

            //ContextHolder 내부 세션에 사용자 등록
            SecurityContextHolder.getContext().setAuthentication(authToken);

            filterChain.doFilter(request, response);
        } catch (ResponseStatusException e) {
            log.error("JWT 예외", e);
            response.sendError(e.getStatusCode().value());
        }
    }
}
