package com.example.momo.global.config;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;


import com.example.momo.domain.auth.service.OAuth2UserService;
import com.example.momo.global.filter.JwtAccessDeniedHandler;
import com.example.momo.global.filter.JwtAuthenticationEntryPoint;
import com.example.momo.global.filter.JwtFilter;
import com.example.momo.global.filter.OAuth2SuccessHandler;
import com.example.momo.global.utils.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
	private final JwtUtil jwtUtil;
	private final JwtAccessDeniedHandler jwtAccessDeniedHandler;
	private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
	private final ObjectMapper objectMapper;
	private final OAuth2UserService oAuth2UserService;
	private final OAuth2SuccessHandler oAuth2SuccessHandler;

	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		return new CorsConfigurationSource() {
			@Override
			public CorsConfiguration getCorsConfiguration(HttpServletRequest request) {
				CorsConfiguration configuration = new CorsConfiguration();
				configuration.setAllowedOrigins(Collections.singletonList("http://localhost:3000")); //허용할 Origin 등록
				configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS",
					"PATCH")); // GET, POST, PUT 등 허용할 요청 메소드 등록
				configuration.setAllowCredentials(true);
				configuration.setAllowedHeaders(List.of("Authorization")); //클라이언트가 요청에 어떤 헤더를 실어 보낼 수 있는지 설정
				configuration.setMaxAge(3600L); //pre-filght 요청의 결과를 얼마동안 캐시해두는 허용할지 시간 지정 (초단위)
				configuration.setExposedHeaders(
					Collections.singletonList("Authorization")); // 클라이언트가 응답에서 어떤 헤더를 읽어올 수 있는지 설정
				return configuration;
			}
		};
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
		return httpSecurity
			.cors(cors -> cors.configurationSource(corsConfigurationSource()))
			.csrf(AbstractHttpConfigurer::disable)
			.formLogin(AbstractHttpConfigurer::disable)
			.httpBasic(AbstractHttpConfigurer::disable)
			.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
			.oauth2Login(oauth2 -> oauth2
				.userInfoEndpoint(userInfoEndpointConfig -> userInfoEndpointConfig
					.userService(oAuth2UserService))
				.successHandler(oAuth2SuccessHandler))
			.addFilterAt(new JwtFilter(jwtUtil, objectMapper), UsernamePasswordAuthenticationFilter.class)
			.authorizeHttpRequests(auth -> auth
				// 인증이 필요없는 공개 엔드포인트
				.requestMatchers(HttpMethod.POST, "/api/v1/categories").hasRole("ADMIN")
				.requestMatchers(HttpMethod.PATCH, "/api/v1/categories").hasRole("ADMIN")
				.requestMatchers(
					"/api/v1/auth/register",
					"/api/v1/auth/login",
					"/api/v1/auth/reissue",
					"/api/v1/categories/**"
				).permitAll()
				.anyRequest().authenticated()
			)
			.exceptionHandling(configure -> configure
				.authenticationEntryPoint(jwtAuthenticationEntryPoint)
				.accessDeniedHandler(jwtAccessDeniedHandler)
			)
			.build();
	}

	@Bean
	public BCryptPasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
}
