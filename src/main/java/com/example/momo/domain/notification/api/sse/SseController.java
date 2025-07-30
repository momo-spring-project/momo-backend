package com.example.momo.domain.notification.api.sse;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.example.momo.domain.auth.domain.dto.AuthUser;
import com.example.momo.domain.notification.application.sse.SseService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2")
public class SseController {

	private final SseService sseService;

	@GetMapping("/connect")
	public SseEmitter connect(@AuthenticationPrincipal AuthUser authUser) {
		return sseService.connect(authUser.getId());
	}
}
