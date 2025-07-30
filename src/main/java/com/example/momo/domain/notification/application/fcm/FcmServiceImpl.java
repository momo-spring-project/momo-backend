package com.example.momo.domain.notification.application.fcm;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.momo.domain.notification.application.fcm.dto.FcmMessageDto;
import com.example.momo.domain.notification.application.fcm.dto.FcmTokenRequestDto;
import com.example.momo.domain.notification.application.fcm.sender.FcmSender;
import com.example.momo.domain.notification.domain.FcmToken;
import com.example.momo.domain.notification.domain.FcmTokenRepository;
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
	public void processFcmIfTokenExists(FcmMessageDto messageDto) {

		//userId 로 토큰 리스트 생성 -> 유저가 가지고 있는 모든 토큰에 전송
		List<FcmToken> tokens = fcmTokenRepository.findValidTokens(messageDto.getUserId());

		//token 이 없을경우 메세지큐 저장
		if (tokens.isEmpty()) {
			//todo : 메세지큐에 전송 실패 메세지 저장
			log.info("유효한 토큰이 없습니다.: userId={}, content={}", messageDto.getUserId(), messageDto.getContent());
			return;
		}

		//전송 실패 리스트 생성 -> 실패한 토큰 삭제 예정
		List<FcmToken> failedList = new ArrayList<>();

		//모두 전송 실패시 메세지큐 생성 예정
		boolean successAtLeastOnce = false;

		for (FcmToken fcmToken : tokens) {

			//플랫폼 분리(Android, IOS, WEB...)
			FcmSender fcmSender = getOrSkipSender(fcmToken, failedList);
			if (fcmSender == null)
				continue;

			messageDto.updateToken(fcmToken.getToken());

			//메세지 플랫폼 서비스로 전송 후 성공/실패 반환
			boolean success = sendMessage(fcmSender, messageDto);

			//하나라도 성공 시 메세지큐 저장 X
			if (success) {
				successAtLeastOnce = true;
			} else {
				failedList.add(fcmToken);
			}
		}

		// 실패 토큰 DB 삭제
		deleteFailedTokens(failedList);

		//토큰은 있지만 모든 토큰에 전송 실패했을 경우 메세지큐 저장
		if (!successAtLeastOnce) {
			//todo : 메세지큐에 전송 실패 메세지 저장
			log.info("FCM 전송 모두 실패: userId={}, content={}", messageDto.getUserId(), messageDto.getContent());
		}
	}

	private boolean sendMessage(FcmSender sender, FcmMessageDto messageDto) {
		try {
			//플랫폼 Sender 에 send 메서드로 전송
			sender.send(messageDto);
			return true;

		} catch (FirebaseMessagingException e) {
			MessagingErrorCode code = e.getMessagingErrorCode();

			if (code == MessagingErrorCode.INVALID_ARGUMENT || code == MessagingErrorCode.UNREGISTERED) {
				log.info("유효하지 않은 토큰: token={}, code={}", messageDto.getToken(), code);
			} else {
				log.error("FCM 전송 실패 (Firebase 예외): token={}, code={}", messageDto.getToken(), code, e);
			}

		} catch (Exception e) {
			log.error("FCM 전송 실패 (기타 예외): token={}, error={}", messageDto.getToken(), e.getMessage(), e);
		}
		return false;
	}

	//지원하는 플랫폼 서비스 생성
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

	//전송 실패 토큰 리스트 삭제
	private void deleteFailedTokens(List<FcmToken> failedList) {
		if (!failedList.isEmpty()) {
			fcmTokenRepository.deleteAll(failedList);
			log.info("FCM 실패 토큰 {}건 삭제 완료", failedList.size());
		}
	}

	//전달 받은 토큰을 userId 와 DB 저장
	@Override
	public void createToken(Long userId, FcmTokenRequestDto requestDto) {
		FcmToken token = requestDto.toEntity(userId);
		fcmTokenRepository.save(token);
	}
}
