package com.example.momo.domain.messagehub.application.util;

import java.util.Locale;

import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import com.example.momo.domain.messagehub.enums.MessageKey;

import lombok.RequiredArgsConstructor;

/**
 * 메시지 키와 파라미터를 기반으로 실제 알림 메시지를 생성하는 유틸리티 클래스입니다.
 *
 * <p>
 * {@link MessageKey}를 통해 메시지 코드와 템플릿을 조회하고,
 * {@link MessageSource}를 이용해 최종 문자열로 변환합니다.
 * 도메인 이벤트에 따른 알림 메시지 포맷 생성에 사용됩니다.
 */
@Component
@RequiredArgsConstructor
public class MessageFormatUtil {

	private final MessageSource messageSource;

	public String buildCreateMessage(String categoryName) {
		return getMessage(MessageKey.MEETING_CREATED.key(), categoryName);
	}

	public String buildUpdateMessage(String meetingName) {
		return getMessage(MessageKey.MEETING_UPDATED.key(), meetingName);
	}

	public String buildDeleteMessage(String meetingName) {
		return getMessage(MessageKey.MEETING_DELETED.key(), meetingName);
	}

	public String buildJoinMessage(String participantNickname) {
		return getMessage(MessageKey.MEETING_JOINED.key(), participantNickname);
	}

	public String buildCancelMessage(String participantNickname) {
		return getMessage(MessageKey.MEETING_CANCELED.key(), participantNickname);
	}

	public String buildFollowedMessage(String nickname) {
		return getMessage(MessageKey.FOLLOWED.key(), nickname);
	}

	public String buildPaidMessage() {
		return getMessage(MessageKey.PAID.key());
	}

	public String buildRefundedMessage() {
		return getMessage(MessageKey.REFUNDED.key());
	}

	private String getMessage(String code, Object... args) {
		return messageSource.getMessage(code, args, Locale.getDefault());
	}
}
