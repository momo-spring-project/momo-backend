package com.example.momo.global.sse.api;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.example.momo.domain.auth.dto.AuthUser;
import com.example.momo.global.sse.application.SseService;

import lombok.RequiredArgsConstructor;

//SSE 기본구조. 현재는 사용 X. 추후 사용 고려
@RestController
@RequiredArgsConstructor
@RequestMapping("/sse")
public class SseController {

	private final SseService sseService;

	@GetMapping("/connect")
	public SseEmitter connect(@AuthenticationPrincipal AuthUser authUser) {
		return sseService.connect(authUser.getId());
	}
}
