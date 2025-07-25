package com.example.momo.global.infrastructure.springEvent;

import java.util.List;

//Event 생성 시 전달받는 DTO
public record NotificationEvent(List<Long> userIdList, Long meetingId, String typeName, String content) {
}
