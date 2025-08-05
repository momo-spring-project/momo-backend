# 📨 RabbitMQ 네이밍 및 패키지 구조 컨벤션

## 1. 목적

RabbitMQ 기반 메시징 시스템에서 `Exchange`, `Routing Key`, `Queue`의 명명 규칙과 메시지 처리 구조를 통일하여 팀 전체의 유지보수성과 가독성을 높입니다.

---

## 2. 네이밍 규칙

- **Exchange**: `publisher-service.domain.events`  
  예) `momo.user.events`

- **Routing Key**: `domain.event`  
  예) `user.withdrawn`, `order.paid`

- **Queue**: `consumer-service.domain.event.queue`  
  예) `notification.user.withdrawn.queue`, `analytics.order.paid.queue`

---

## 3. 예시

| 항목             | 예시                                  |
|----------------|-------------------------------------|
| Exchange       | `momo.user.events`                  |
| Routing Key    | `user.withdrawn`, `user.created`    |
| Queue (Notify) | `notification.user.withdrawn.queue` |

---

## 4. 확장 규칙

- 여러 이벤트를 하나의 Queue로 묶고 싶은 경우:
    - Routing Key: `user.*`
    - Queue 이름: `notification.user.all.queue`

- Dead Letter Queue(DLQ):
    - 규칙: `<queue-name>.dlq`
    - 예: `notification.user.withdrawn.queue.dlq`

---

## 5. Java 상수 정의 예시

```java
// RabbitExchangeNames.java
public class RabbitExchangeNames {
	public static final String USER_EVENTS = "momo.user.events";
	public static final String ORDER_EVENTS = "momo.order.events";
}

// RoutingKeys.java
public class RoutingKeys {
	public static final String USER_CREATED = "user.created";
	public static final String USER_WITHDRAWN = "user.withdrawn";
}

// QueueNames.java
public class QueueNames {
	public static final String NOTIFICATION_USER_WITHDRAWN = "notification.user.withdrawn.queue";
	public static final String ANALYTICS_USER_CREATED = "analytics.user.created.queue";
	public static final String DLQ_SUFFIX = ".dlq";
}
```

---

## 6. 메시지 처리 클래스 구조 및 패키징 컨벤션

RabbitMQ 이벤트 처리는 도메인 단위로 다음과 같은 패키지 구조를 따릅니다.

### 📁 패키지 구조

```
src
└── main
    └── java
        └── com.example.momo
            └── user
                └── event
                    └── rabbitmq
                        ├── config      # Queue, Exchange, Binding, ListenerContainer 설정
                        ├── producer    # 메시지를 발행하는 클래스
                        └── consumer    # 메시지를 구독하고 처리하는 클래스
```

### 📦 각 패키지의 역할

| 패키지        | 설명                                                                  |
|------------|---------------------------------------------------------------------|
| `config`   | Queue, Exchange, Binding, `SimpleRabbitListenerContainerFactory` 설정 |
| `producer` | 이벤트 발행 (RabbitTemplate 기반)                                          |
| `consumer` | 이벤트 수신 및 처리 (`@RabbitListener`)                                     |

### 🧾 예시 클래스 (user 도메인 기준)

```
user
└── event
    └── rabbitmq
        ├── config
        │   ├── UserRabbitConfig.java
        │   └── UserRabbitListenerContainerFactoryConfig.java
        ├── producer
        │   └── UserEventProducer.java
        └── consumer
            └── UserEventConsumer.java
```

---

## ✅ 요약

- 네이밍은 일관성과 가독성을 고려해 도메인 중심으로 구성
- 각 역할별 클래스를 도메인 내부에서 책임 분리
- 상수와 패키지 구조도 컨벤션에 따라 통일해서 협업 효율성 극대화

---

## 📚 전체 예시 패키지 구조 (도메인별 포함)

```
com.example.momo
├── user
│   └── event
│       └── rabbitmq
│           ├── config
│           │   ├── UserRabbitConfig.java
│           │   └── UserRabbitListenerContainerFactoryConfig.java
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
        │   ├── RabbitExchangeNames.java
        │   ├── RoutingKeys.java
        │   └── QueueNames.java
        └── config
            └── GlobalRabbitConfig.java
```

> 📌 도메인별로 동일한 구조를 유지하며, 공통 상수/설정은 `global.rabbitmq` 패키지에서 관리합니다.


---

## 7. 이벤트 DTO 구조 및 위치

RabbitMQ 이벤트로 주고받는 메시지는 공통 구조로 설계하고, 다음과 같은 위치에 배치합니다:

```
com.example.momo.global.event.dto
└── UserEventMessage.java
```

### 📦 구성 예시

```java
public record UserEventMessage(
	String eventId,
	String eventType,
	LocalDateTime timestamp,
	String source,
	@JsonTypeInfo(
		use = JsonTypeInfo.Id.CLASS,
		include = JsonTypeInfo.As.PROPERTY,
		property = "@class"
	)
	Object data
) {
	public record UserWithdrawnData(...) {
	}

	public record UserRegisteredData(...) {
	}

	public record UserFollowedData(...) {
	}

	public record UserRatedData(...) {
	}

	public static UserEventMessage createWithdrawn(...) { ...}
}
```

> 📌 다양한 이벤트를 하나의 메시지 구조로 통합하고 `@JsonTypeInfo`를 활용해 역직렬화 시 타입 유지를 지원합니다.

---

## 8. Queue 이름 상수 정의

모든 Queue 이름은 상수 클래스로 정의하여, 오타 방지와 변경 용이성을 확보합니다.

### 📁 위치 예시

```
com.example.momo.global.rabbitmq.constant
└── QueueNames.java
```

### 📦 예시 코드

```java
public class QueueNames {
	public static final String NOTIFICATION_USER_WITHDRAWN = "notification.user.withdrawn.queue";
	public static final String ANALYTICS_USER_REGISTERED = "analytics.user.registered.queue";
	public static final String DLQ_SUFFIX = ".dlq";
}
```

> 📌 Queue 명도 통일된 컨벤션을 따라 상수로 선언하여 여러 서비스에서 공통으로 사용합니다.
