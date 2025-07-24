package com.example.momo.domain.messegehub.application;

import java.util.Locale;

import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import com.example.momo.domain.messegehub.enums.MessageKey;

import lombok.RequiredArgsConstructor;

//직접적인 메세지 생성
@Component
@RequiredArgsConstructor
public class MessageFormatUtil {

	private final MessageSource messageSource;

	public String buildCreateMessage(String categoryName) {
		return getMessage(MessageKey.CREATE.key(), categoryName);
	}

	public String buildUpdateMessage(String meetingName) {
		return getMessage(MessageKey.UPDATE.key(), meetingName);
	}

	public String buildDeleteMessage(String meetingName) {
		return getMessage(MessageKey.DELETE.key(), meetingName);
	}

	public String buildJoinMessage(String participantNickname) {
		return getMessage(MessageKey.JOIN.key(), participantNickname);
	}

	public String buildCancelMessage(String participantNickname) {
		return getMessage(MessageKey.CANCEL.key(), participantNickname);
	}

	private String getMessage(String code, Object... args) {
		return messageSource.getMessage(code, args, Locale.getDefault());
	}
}
