# 클래스 명명 규칙 컨벤션

## 0. 목차

- [1. 기본 명명 규칙](#1-기본-명명-규칙)
- [2. Entity 클래스 네이밍](#2-entity-클래스-네이밍)
- [3. 예외 클래스 네이밍](#3-예외-클래스-네이밍)
- [4. 기타 역할별 클래스명 네이밍](#4-기타-역할별-클래스명-네이밍)
- [5. Enum 클래스 네이밍](#5-enum-클래스-네이밍)
- [6. 공통 응답 객체 네이밍](#6-공통-응답-객체-네이밍)
- [7. 규칙 제외 클래스 기준](#7-규칙-제외-클래스-기준)

## 1. 기본 명명 규칙

- **형식**: `PascalCase` 사용 (명사로 시작, 단어마다 대문자로 시작)
- **규칙**:
    - 클래스 이름은 해당 클래스의 **역할과 책임이 명확히 드러나는 명사형**으로 작성
    - 역할에 따라 아래와 같은 접미사를 명시적으로 붙임

| **역할**    | **접미사**                            | **예시**                         | **이유**                                          |
|-----------|------------------------------------|--------------------------------|-------------------------------------------------|
| **컨트롤러**  | `Controller`                       | `UserController`               | HTTP 요청을 처리하는 컨트롤러 역할 명확화                       |
| **서비스**   | `Service`                          | `OrderService`                 | 비즈니스 로직을 담당하는 클래스 명확화                           |
| **리포지토리** | `Repository`                       | `UserRepository`               | DB 접근 책임을 가진 클래스 명확화                            |
| **설정**    | `Config`                           | `JwtConfig`, `WebSocketConfig` | 환경 설정 클래스임을 명확히 드러냄                             |
| **DTO**   | `RequestDto`, `ResponseDto`, `Dto` | `LoginRequestDto`              | DTO와 엔티티 혼동 방지 <br> 전송용 객체 명확화                  |
| **구현체**   | `Impl`                             | `UserServiceImpl`              | 인터페이스 구현체임을 명확히 하며,<br> Spring에서 자동 인식되도록 하기 위함 |
| **이벤트**   | `Event`                            | `NotificationEvent`            | 이벤트 전용 트리거 객체 <br>    DTO 와 혼동 방지               |  

---

## 2. Entity 클래스 네이밍

- 모든 Entity는 `PascalCase` 형식의 **단수형 명사**로 작성
- 접미사로 `Entity`는 붙이지 않음 (명확성과 중복 방지)
- 도메인 이름이 중복되는 경우 의미 있는 접두어를 붙여 구분

    - 예: `User`, `Order`, `Concert`, `VenueSeat`, `ReservedSeat`

- 두 개의 도메인 개념이 결합된 관계성 Entity인 경우, 의미가 명확한 복합 명사 조합으로 작성

    - 예: `MeetingParticipant`, `OrderItem`, `TeamMember`

- 테이블명은 기본적으로 클래스명을 소문자 변환한 이름 사용 (JPA 기본 전략 따름)
- 단, 공통 필드 제공을 위한 추상 엔티티에는 예외적으로 명명 허용

    - 예: `BaseEntity`, `BaseCreateEntity`

- **DTO ↔ Entity 변환은 DTO 클래스 내부에 작성하며, `from()`과 `toEntity()` 메서드로 명명 통일**

---

## 3. 예외 클래스 네이밍

- 모든 도메인별 에러 코드는 `XxxErrorCode` 형태의 Enum으로 정의
    - 예: `UserErrorCode`, `MeetingErrorCode`
- 커스텀 예외 클래스는 `XxxException` 형태로 명명
    - 예: `UserException`, `PaymentException`

---

## 4. 기타 역할별 클래스명 네이밍

- 아래 3가지는 공통적으로 사용하는 접미사
- 이 외의 접미사는 도입 목적과 책임을 팀 내에서 명확히 협의 후 사용

| **접미사**        | **역할 설명**                                                                                     | **예시**                                                             |
|----------------|-----------------------------------------------------------------------------------------------|--------------------------------------------------------------------|
| **`Util`**     | 상태 없이 범용적으로 사용 가능한 도우미 메서드<br>도메인 독립적<br>변환·포맷·파싱 등 순수 기능만 포함                                 | `DateUtil`, `StringUtil`, `MaskingUtil`                            |
| **`Provider`** | 특정 객체(예: 토큰, 설정 값 등)를 생성<br>외부 리소스를 제공하는 클래스<br>내부 상태보다는 생성 책임에 초점                            | `JwtTokenProvider`, `ClockProvider`, `RandomCodeProvider`          |
| **`Handler`**  | 이벤트, 요청, 예외 등 외부 입력에 대한 **처리/흐름 제어**<br>직접적인 비즈니스 로직은 포함하지 않음<br>주로 **데이터 분기, 라우팅, 위임 처리** 역할 | `GlobalExceptionHandler`, `WebSocketHandler`, `AuthFailureHandler` |

---

## 5. Enum 클래스 네이밍

- Enum 클래스는 `PascalCase` 형식으로 작성하며, **도메인 + 의미 명사 접미사** 형태로 명명
- 접미사는 `Status`, `Type`, `Role`, `Step`, `Category` 등 역할에 맞게 선택
- Enum 내부 값은 항상 `UPPER_SNAKE_CASE`로 작성

| **예시 클래스명**         | **용도** | **내부 값 예시**                |
|---------------------|--------|----------------------------|
| **`MeetingStatus`** | 회의 상태  | `PENDING`, `CONFIRMED`     |
| **`UserRole`**      | 사용자 역할 | `ADMIN`, `USER`, `GUEST`   |
| **`TicketType`**    | 티켓 종류  | `VIP`, `NORMAL`, `STUDENT` |

---

## 6. 공통 응답 객체 네이밍

- 모든 API 응답을 공통 포맷으로 감싸기 위한 래퍼 클래스는 `ApiResponse`로 통일
    - 제네릭 타입을 활용해 다양한 데이터 타입을 포괄
    - `status`, `message`, `data` 필드 기본 구성 권장
- 예: `ApiResponse<UserResponseDto>`, `ApiResponse<Void>`

---

## 7. 규칙 제외 클래스 기준

- 아래와 같은 클래스는 Spring 프레임워크나 라이브러리 구조에서 자연스럽게 유입되는 역할 기반 클래스
- 다만, **이름만으로 클래스의 책임이 명확하게 드러나야 하며**, 불명확하거나 모호한 이름은 지양해야 함.

| **접미사**           | **사용 예시**                           | **설명**                       |
|-------------------|-------------------------------------|------------------------------|
| **`Interceptor`** | `JwtHandshakeInterceptor`           | Web/WebSocket 요청 가로채기용 클래스   |
| **`Scheduler`**   | `MeetingPaymentScheduler`           | 스케줄러 클래스                     |
| **`Initializer`** | `CategoryInitializer`               | 초기 데이터 설정 클래스                |
| **`Filter`**      | `AuthFilter`, `LoggingFilter`       | 서블릿 요청 필터링 기능 담당             |
| **`Listener`**    | `NotificationEventListener`         | Spring Event 또는 외부 이벤트 수신 처리 |
| **`EntryPoint`**  | `JwtAuthenticationEntryPoint`       | Spring Security 인터페이스 구현체    |
| **`Client`**      | `TossPaymentsClient`, `SlackClient` | 외부 API 요청 및 응답 처리 책임 표현      |
| **`Sender`**      | `AndroidFcmSender`                  | 외부 메세지 전송 처리                 |

> 이들 클래스는 명확한 역할 기반으로 구조적으로 사용되기 때문에 ,
> 규칙 강제보다는 역할 명확성을 우선함.
>