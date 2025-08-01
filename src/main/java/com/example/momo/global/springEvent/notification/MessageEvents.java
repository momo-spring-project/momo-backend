package com.example.momo.global.springEvent.notification;

import java.util.List;

/**
 * 메세지 허브에서 발생하는 이벤트를 정의합니다.
 */
public record MessageEvents(List<Long> userIdList, Long targetId, String typeName, String content) {
}
