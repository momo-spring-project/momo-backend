package com.example.momo.domain.notification.application.fcm;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.momo.domain.notification.application.fcm.dto.FcmCreateRequestDto;
import com.example.momo.domain.notification.application.fcm.dto.FcmMessageDto;
import com.example.momo.domain.notification.application.fcm.sender.FcmSender;
import com.example.momo.domain.notification.domain.FcmToken;
import com.example.momo.domain.notification.domain.FcmTokenRepository;
import com.example.momo.domain.notification.enums.SendStatus;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.MessagingErrorCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class FcmServiceImpl implements FcmService {

	private final FcmTokenRepository fcmTokenRepository;

	private final List<FcmSender> fcmSenders;

	//전달 받은 토큰을 userId 와 DB 저장
	@Override
	public void createToken(Long userId, FcmCreateRequestDto requestDto) {

		Optional<FcmToken> existingToken = fcmTokenRepository.findByUserIdAndDeviceId(userId, requestDto.deviceId());
		if (existingToken.isPresent()) {
			// 기존 디바이스의 토큰 갱신
			FcmToken findToken = existingToken.get();
			findToken.updateToken(requestDto.token());
		} else {
			// 새로운 디바이스 등록
			FcmToken token = requestDto.toEntity(userId);
			fcmTokenRepository.save(token);
		}
	}

	//FCM 전체 흐름 처리
	@Override
	public boolean processFcmIfTokenExists(FcmMessageDto messageDto) {

		//userId 로 토큰 리스트 생성 -> 유저가 가지고 있는 모든 토큰에 전송
		List<FcmToken> tokens = fcmTokenRepository.findValidTokens(messageDto.getUserId());

		//token 이 없을경우 메세지큐 저장
		if (tokens.isEmpty()) {
			log.warn("FCM 전송 실패 - 유효한 토큰이 없습니다. : userId={}, content={}", messageDto.getUserId(),
				messageDto.getContent());
			return false;
		}

		//실패한 토큰 삭제 예정 리스트
		List<FcmToken> tokensToDelete = new ArrayList<>();

		//모두 전송 실패시 메세지큐 생성 예정
		boolean successAtLeastOnce = false;

		for (FcmToken fcmToken : tokens) {

			//플랫폼 분리(Android, IOS, WEB...)
			FcmSender fcmSender = getOrSkipSender(fcmToken, tokensToDelete);
			if (fcmSender == null)
				continue;

			messageDto.updateToken(fcmToken.getToken());

			//메세지 플랫폼 서비스로 전송 후 토큰 유효성 체크
			SendStatus sendStatus = sendMessage(fcmSender, messageDto);

			//실패시 처리 안함
			if (sendStatus == SendStatus.FAILURE) {
				continue;
			}

			//하나라도 성공 시 메세지큐 저장 X
			switch (sendStatus) {
				case SUCCESS -> successAtLeastOnce = true;
				case DELETE_TOKEN -> tokensToDelete.add(fcmToken);
			}
		}

		// 실패 토큰 DB 삭제
		deleteFailedTokens(tokensToDelete);

		//토큰은 있지만 모든 토큰에 전송 실패했을 경우 메세지큐 저장
		if (!successAtLeastOnce) {
			log.warn("FCM 전송 모두 실패: userId={}, content={}", messageDto.getUserId(), messageDto.getContent());
			return false;
		}
		return true;
	}

	@Override
	public long deleteToken(Long userId, String deviceId) {

		return fcmTokenRepository.deleteToken(userId, deviceId);
	}

	//메세지 전송 후 예외 처리
	private SendStatus sendMessage(FcmSender sender, FcmMessageDto messageDto) {
		try {
			//플랫폼 Sender 에 send 메서드로 전송
			sender.send(messageDto);
			return SendStatus.SUCCESS;

		} catch (FirebaseMessagingException e) {
			MessagingErrorCode code = e.getMessagingErrorCode();

			if (code == MessagingErrorCode.INVALID_ARGUMENT
				|| code == MessagingErrorCode.UNREGISTERED
				|| code == MessagingErrorCode.SENDER_ID_MISMATCH) {
				log.warn("유효하지 않은 토큰: token={}, code={}", messageDto.getToken(), code);
				return SendStatus.DELETE_TOKEN;
			} else {
				log.warn("FCM 전송 실패 (Firebase 예외): token={}, code={}", messageDto.getToken(), code, e);
				return SendStatus.FAILURE;
			}

		} catch (Exception e) {
			log.warn("FCM 전송 실패 (기타 예외): token={}, error={}", messageDto.getToken(), e.getMessage(), e);
			return SendStatus.FAILURE;
		}
	}

	//지원하는 플랫폼 서비스 생성
	private FcmSender getOrSkipSender(FcmToken token, List<FcmToken> failedList) {
		return fcmSenders.stream()
			.filter(sender -> sender.handles() == token.getPlatformType())
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

}