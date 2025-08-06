package com.example.momo.global.rabbitMQ.dto.messagehub;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = AlarmMessageType.EVENT_TYPE)
public interface DomainAlarmMessage {
}
