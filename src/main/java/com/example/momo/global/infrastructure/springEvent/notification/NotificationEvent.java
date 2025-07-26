package com.example.momo.global.infrastructure.springEvent.notification;

import java.util.List;

//Event 생성 시 전달받는 DTO
public record NotificationEvent(List<Long> userIdList, Long targetId, String typeName, String content) {
}
