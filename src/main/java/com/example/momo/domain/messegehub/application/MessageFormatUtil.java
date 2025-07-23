package com.example.momo.domain.messegehub.application;

import org.springframework.stereotype.Component;

//직접적인 메세지 생성
@Component
public class MessageFormatUtil {

	public String buildCreateMessage(String categoryName) {
		return String.format("관심 지역에 새로운 %s 모임이 추가되었습니다.", categoryName);
	}

	public String buildUpdateMessage(String meetingName) {
		return String.format("%s 모임에 변경사항이 있습니다.", meetingName);
	}

	public String buildDeleteMessage(String meetingName) {
		return String.format("%s 모임이 취소 되었습니다.", meetingName);
	}

	public String buildJoinMessage(String participantNickname) {
		return String.format("%s 님이 모임에 참석했습니다.", participantNickname);
	}

	public String buildCancelMessage(String participantNickname) {
		return String.format("%s 님이 모임 참석을 취소했습니다.", participantNickname);
	}
}
