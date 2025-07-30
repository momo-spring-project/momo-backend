package com.example.momo.domain.notification.application.fcm;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.momo.domain.notification.application.fcm.dto.FcmMessageDto;
import com.example.momo.domain.notification.application.fcm.dto.FcmTokenRequestDto;
import com.example.momo.domain.notification.application.fcm.sender.FcmSender;
import com.example.momo.domain.notification.domain.FcmToken;
import com.example.momo.domain.notification.domain.FcmTokenRepository;
import com.example.momo.domain.notification.domain.Notification;
import com.example.momo.domain.notification.enums.NotificationType;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.MessagingErrorCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class FcmServiceImpl implements FcmService {

	private final FcmTokenRepository fcmTokenRepository;

	private final List<FcmSender> fcmSenders;

	@SuppressWarnings("checkstyle:NeedBraces")
	@Override
	public void processFcmIfTokenExists(Notification notification) {
		//get 함수 중복 방지
		NotificationType notificationType = notification.getType();
		String content = notification.getContent();

		List<FcmToken> tokens = fcmTokenRepository.findValidTokens(notification.getUserId());
		List<FcmToken> failedList = new ArrayList<>();

		boolean successAtLeastOnce = false;

		//token 이 없을경우 메세지큐 저장
		if (tokens.isEmpty()) {
			//todo : 메세지큐에 전송 실패 메세지 저장
			return;
		}

		for (FcmToken token : tokens) {

			//플랫폼 분리
			FcmSender fcmSender = getOrSkipSender(token, failedList);
			if (fcmSender == null)
				continue;

			//메세지 플랫폼 서비스로 전송
			boolean success = sendMessage(fcmSender, token, notificationType, content, failedList);
			if (success)
				successAtLeastOnce = true;

		}

		// 실패 토큰 DB 삭제
		deleteFailedTokens(failedList);

		//토큰은 있지만 모든 토큰에 전송 실패했을 경우 메세지큐 저장
		if (!successAtLeastOnce) {
			//todo : 메세지큐에 전송 실패 메세지 저장
		}
	}

	private boolean sendMessage(FcmSender sender, FcmToken token,
		NotificationType type, String content,
		List<FcmToken> failedList) {
		try {
			sender.send(FcmMessageDto.builder()
				.token(token.getToken())
				.type(type)
				.content(content)
				.build());

			return true;

		} catch (FirebaseMessagingException e) {
			MessagingErrorCode code = e.getMessagingErrorCode();

			if (code == MessagingErrorCode.INVALID_ARGUMENT || code == MessagingErrorCode.UNREGISTERED) {
				log.info("유효하지 않은 토큰: token={}, code={}", token.getToken(), code);
			} else {
				log.error("FCM 전송 실패 (Firebase 예외): token={}, code={}", token.getToken(), code, e);
			}

		} catch (Exception e) {
			log.error("FCM 전송 실패 (기타 예외): token={}, error={}", token.getToken(), e.getMessage(), e);
		}

		failedList.add(token);
		return false;
	}

	private FcmSender getOrSkipSender(FcmToken token, List<FcmToken> failedList) {
		return fcmSenders.stream()
			.filter(sender -> sender.handles().contains(token.getPlatformType()))
			.findFirst()
			.orElseGet(() -> {
				log.warn("지원하지 않는 플랫폼: {}, token={}", token.getPlatformType(), token.getToken());
				failedList.add(token);
				return null;
			});
	}

	private void deleteFailedTokens(List<FcmToken> failedList) {
		if (!failedList.isEmpty()) {
			fcmTokenRepository.deleteAll(failedList);
			log.info("FCM 실패 토큰 {}건 삭제 완료", failedList.size());
		}
	}

	@Override
	public void createToken(Long userId, FcmTokenRequestDto requestDto) {
		FcmToken token = requestDto.toEntity(userId);
		fcmTokenRepository.save(token);
	}
}
