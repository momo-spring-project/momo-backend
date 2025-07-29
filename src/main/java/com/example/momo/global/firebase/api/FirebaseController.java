package com.example.momo.global.firebase.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.momo.global.common.dto.ApiResponse;
import com.example.momo.global.firebase.application.FirebaseService;
import com.example.momo.global.firebase.application.dto.FirebaseResponseDto;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class FirebaseController {

	private final FirebaseService firebaseService;

	@PostMapping("/api/firebase")
	public ResponseEntity<ApiResponse<Void>> sendToUser(@RequestBody FirebaseResponseDto dto) {
		firebaseService.send(dto);
		return ResponseEntity.ok(ApiResponse.success("전송됨 : " + dto.getContent(), null));
	}
}
