# RabbitMQ 네이밍 컨벤션 & 패키지 구조 가이드

## 0. 목차

- [1. 목적](#1-목적)
- [2. 네이밍 규칙](#2-네이밍-규칙)
- [3. 예시 표](#3-예시-표)
- [4. 확장 규칙](#4-확장-규칙)
- [5. java-상수-정의-예시](#5-java-상수-정의-예시)
- [6. 메시지 처리 클래스 구조](#6-메시지-처리-클래스-구조)
- [7. 역할별 설명](#7-역할별-설명)
- [8. 이벤트 공통 dto 구조 eventwrappert](#8-이벤트-공통-dto-구조-eventwrappert)
- [9. event type 이름 상수 정의](#9-event-type-이름-상수-정의)
- [10. 최종 정리](#10-최종-정리)

---

## 1. 목적

RabbitMQ 기반 메시징 시스템에서 Exchange, Routing Key, Queue의 네이밍 규칙과 패키지 구조를 통일하여 **팀 전체의 유지보수성과 가독성을 높이는 것**이 목적입니다.

---

## 2. 네이밍 규칙

- **Exchange**: `publisher-service.domain.events`

  예: `momo.user.events`

- **Routing Key**: `domain.event.key`

  예: `user.withdrawn.key`, `order.paid.key`

- **Queue**: `consumer-service.domain.event.queue`

  예: `notification.user.withdrawn.queue`, `analytics.order.paid.queue`

---

## 3. 예시 표

| **항목**                | **예시**                               |
|-----------------------|--------------------------------------|
| **Exchange**          | momo.user.events                     |
| **Routing Key**       | user.withdrawn.key, user.created.key |
| **Queue (Notify)**    | notification.user.withdrawn.queue    |
| **Queue (Analytics)** | analytics.user.created.queue         |

---

## 4. 확장 규칙

- 여러 이벤트를 하나의 Queue로 수신하고 싶을 경우:
    - Routing Key: `user.*`
    - Queue: `notification.user.all.queue`
- Dead Letter Queue (DLQ) 네이밍:
    - `<queue-name>.dlq`
    - 예: `message-hub.queue.dlq`

---

## 5. Java 상수 정의 예시

```java
// RabbitExchangeNames.java
public class RabbitExchangeNames {
	public static final String USER_EVENTS = "momo.user.events";
}

// RoutingKeys.java
public class RoutingKeys {
	public static final String USER_WITHDRAWN_KEY = "user.withdrawn.key";
}

// QueueNames.java
public class QueueNames {
	public static final String NOTIFICATION_QUEUE = "notification.queue";
	public static final String PARTICIPANT_DLQ = "participant.dlq";
}
```

---

## 6. 메시지 처리 클래스 구조

RabbitMQ 이벤트 처리는 도메인 단위로 다음과 같은 구조를 따릅니다:

```
com.example.momo
├── user
│   └── event
│       └── rabbitmq
│           ├── config
│           │   ├── UserRabbitConfig.java
│           ├── producer
│           │   └── UserEventProducer.java
│           └── consumer
│               └── UserEventConsumer.java
│
├── auth
│   └── event
│       └── rabbitmq
│           ├── config
│           │   └── AuthRabbitConfig.java
│           ├── producer
│           │   └── AuthEventProducer.java
│           └── consumer
│               └── AuthEventConsumer.java
│
├── meeting
│   └── event
│       └── rabbitmq
│           ├── config
│           ├── producer
│           └── consumer
│
└── global
    └── rabbitmq
        ├── dto
        │   ├── UserEventMessage.java
        ├── constant
		    │   ├── EventTypeNames.java
        │   ├── RabbitExchangeNames.java
        │   ├── RoutingKeys.java
        │   └── QueueNames.java
        └── config
            └── GlobalRabbitConfig.java
```

---

## 7. 역할별 설명

| 패키지      | 설명                                             |
|----------|------------------------------------------------|
| config   | Queue, Exchange, Binding, ListenerContainer 설정 |
| producer | 이벤트 발행 (`RabbitTemplate`)                      |
| consumer | 이벤트 수신 (`@RabbitListener`)                     |
| dto      | 이벤트 메시지 구조 정의 (공통 사용)                          |

---

## 8. 이벤트 공통 DTO 구조 (`EventWrapper<T>`)

모든 도메인 이벤트를 공통 포맷으로 감싸서 전달하기 위해 사용합니다.

```java
public record EventWrapper<T>(
	String uuId, // 이벤트 고유 ID (UUID)
	String type, // 이벤트 타입 (예: payment.paid, meeting.created)
	T data       // 이벤트 데이터 (도메인별 DTO)
) {
}
```

> ✅ 위치: com.example.momo.global.rabbitmq.dto.common.EventWrapper
>

---

## 9. Event Type 이름 상수 정의

이벤트의 성격과 출처를 나타내는 문자열이며, 이 값을 기준으로 매핑 처리를 해줍니다.

```java
// 형식 : Domain.Event
public class EventTypeNames {
	public static final String USER_WITHDRAWN = "user.withdrawn";
	public static final String MEETING_CREATE = "meeting.create";
	public static final String PAYMENT_COMPLETED = "payment.completed";
}
```

> ✅ 위치: com.example.momo.global.rabbitmq.constant.EventTypeNames
>

---

## 10. 최종 정리

- **네이밍 규칙**
    - Exchange → `publisher-service.domain.events` (예: `momo.user.events`)
    - Routing Key → `domain.event.key` (예: `user.withdrawn.key`)
    - Queue → `consumer-service.domain.event.queue` (예: `participant.payment.succeed.queue`)
- **상수 정의 위치 (`global.rabbitmq.constant`)**
    - `RabbitExchangeNames.java` → Exchange 이름
    - `RoutingKeys.java` → Routing Key
    - `QueueNames.java` → Queue
    - `EventTypeNames.java` → 이벤트 타입
- **공통 DTO 구조**
    - `EventWrapper<T>` (uuid, type, data)
    - 위치: `global.rabbitmq.dto.common`
- **패키지 구조 원칙**
    - 도메인 단위로 `event.rabbitmq.config / producer / consumer` 구성
    - 공통 설정은 `global.rabbitmq.config`
    - 공통 DTO/상수는 `global.rabbitmq.dto`와 `global.rabbitmq.constant`