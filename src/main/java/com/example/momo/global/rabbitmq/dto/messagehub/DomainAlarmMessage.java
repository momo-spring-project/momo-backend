package com.example.momo.global.rabbitmq.dto.messagehub;

import com.example.momo.global.rabbitmq.constant.EventTypeNames;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = EventTypeNames.EVENT_TYPE)
public interface DomainAlarmMessage {
}
