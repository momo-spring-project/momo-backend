package com.example.momo.domain.notification.presentation.sse;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.example.momo.domain.notification.application.sse.SseService;
import com.example.momo.domain.notification.application.sse.dto.SseMessageDto;
import com.example.momo.global.common.dto.AuthUser;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/sse")
public class SseController {

	private final SseService sseService;

	@GetMapping("/connect")
	public SseEmitter connect(@AuthenticationPrincipal AuthUser authUser) {

		return sseService.connect(authUser.getId());
	}

	//전송 확인용 테스트 코드
	@PostMapping("/send")
	public boolean send(@RequestBody SseMessageDto dto) {

		return sseService.sendIfConnected(dto);
	}
}
