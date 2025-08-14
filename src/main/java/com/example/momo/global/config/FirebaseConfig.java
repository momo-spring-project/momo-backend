package com.example.momo.global.config;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class FirebaseConfig {
	/**
	 * 환경변수 FIREBASE_CREDENTIALS(한 줄 JSON, \\n 포함)를 읽어
	 * FirebaseApp을 1회만 초기화한다.
	 */
	@Bean
	public FirebaseApp firebaseApp() throws Exception {
		String credentials = System.getenv("FIREBASE_CREDENTIALS");
		if (credentials == null || credentials.isBlank()) {
			log.error("FIREBASE_CREDENTIALS 환경변수가 비어있습니다.");
			return null;
		}

		// .env 에서 이스케이프된 개행(\\n) → 실제 개행으로 복원
		String restored = credentials.replace("\\n", "\n");

		try (InputStream in = new ByteArrayInputStream(restored.getBytes(StandardCharsets.UTF_8))) {
			FirebaseOptions options = FirebaseOptions.builder()
				.setCredentials(GoogleCredentials.fromStream(in))
				.build();

			// 중복 초기화 방지
			if (FirebaseApp.getApps().isEmpty()) {
				return FirebaseApp.initializeApp(options);
			} else {
				return FirebaseApp.getInstance();
			}
		}
	}

	/**
	 * FirebaseMessaging 빈도 함께 제공 (주입받아 바로 사용 가능)
	 */
	@Bean
	public FirebaseMessaging firebaseMessaging(FirebaseApp app) {
		return FirebaseMessaging.getInstance(app);
	}
}
