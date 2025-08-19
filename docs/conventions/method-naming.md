# 메서드명 규칙 컨벤션

## 0. 목차

- [1. 기본 규칙](#1-기본-규칙)
- [2. 계층별 네이밍 요약](#2-계층별-네이밍-요약)
- [3. controller 메서드](#3-controller-메서드)
- [4. repository 메서드 네이밍-spring-data-jpa](#4-repository-메서드-네이밍-spring-data-jpa)
- [5. boolean 변수 네이밍](#5-boolean-변수-네이밍)
- [6. 인증계정-관련-메서드-네이밍](#6-인증계정-관련-메서드-네이밍)
- [7. 컬렉션-변수-네이밍](#7-컬렉션-변수-네이밍)
- [8. 도메인-행위형-메서드-네이밍](#8-도메인-행위형-메서드-네이밍)
- [9. 명시적-행위형-메서드-네이밍](#9-명시적-행위형-메서드-네이밍)
- [10. 주의사항](#10-주의사항)

## 1. 기본 규칙

- **형식**: **`camelCase`** 사용 (첫 단어 소문자, 이후 단어는 대문자)
- **규칙**:
    - 메서드 이름은 **기능을 설명하는 동사 중심**으로 작성
    - 팀 내 일관성 유지를 위해 동작별 접두어를 아래처럼 통일하여 사용
    - 모든 네이밍에서 축약어 사용을 지양 (예외 : `DTO`, `API`, `JWT`, `URL`, `ID` 등)

| **동작 목적**   | **접두어**     | **예시**                | **이유**                  |
|-------------|-------------|-----------------------|-------------------------|
| **조회**      | `get`       | `getUserById()`       | 단건 조회 의도를 명확히 표현        |
| **조회 (복수)** | `get + 복수형` | `getUsers()`          | 목록 반환 시 명확하게 구분         |
| **생성**      | `create`    | `createOrder()`       | 비즈니스 개체 생성을 명확히 표현      |
| **수정**      | `update`    | `updateUserProfile()` | 기존 데이터 수정임을 명확히 표현      |
| **삭제**      | `delete`    | `deleteUser()`        | 삭제 동작 명확화               |
| **검증**      | `validate`  | `validateToken()`     | 검증 용도의 책임을 명확히 표현       |
| **처리**      | `process`   | `processPayment()`    | 일련의 행위 처리 책임 표현         |
| **핸들링**     | `handle`    | `handleEvent()`       | 이벤트 등 외부 입력 처리 시 명확히 표현 |
| **계산**      | `calculate` | `calculateDiscount()` | 계산 로직에 특화된 책임 표현        |
| **조합**      | `build`     | `buildMessage()`      | 구조화된 메시지 조합 및 생성시 적절    |

---

## 2. 계층별 네이밍 요약

| 계층         | 네이밍 기준           | 예시                                        |
|------------|------------------|-------------------------------------------|
| Controller | REST API 행위 중심   | `createUser()`, `getConcert()`            |
| Service    | 도메인 로직 중심, 필드 기반 | `createUser()`, `getUserById()`           |
| Repository | 영속화 책임 중심        | `save()`, `findByEmail()`, `deleteById()` |

---

## 3. Controller 메서드

- REST API 동작에 따라 아래처럼 네이밍 통일

| **HTTP 메서드**  | **동작** | **메서드명 예시**    | **이유**               |
|---------------|--------|----------------|----------------------|
| **GET**       | 조회     | `getUser()`    | 단건 조회 명확화            |
| **POST**      | 생성     | `createUser()` | 리소스 생성 의도 명확화        |
| **PUT/PATCH** | 수정     | `updateUser()` | 전체 or 일부 수정 구분 없이 적용 |
| **DELETE**    | 삭제     | `deleteUser()` | 삭제 동작 명확화            |

---

## 4. Repository 메서드 네이밍 (Spring Data JPA)

- `findBy`, `existsBy`, `deleteBy` 등 JPA 파생 쿼리 메서드는 Spring Data JPA의 규칙에 따라 작성
    - 예: `findByEmail()`, `existsByUserIdAndStatus()`, `deleteByMeetingId()`
- 파생 메서드는 도메인 필드명을 기반으로 하며, 별도의 팀 컨벤션 없이 JPA 명명 규칙을 그대로 따른다

---

## 5. Boolean 변수 네이밍

- prefix는 의미에 맞게 아래 3가지만 허용하고, 상황에 따라 명확히 구분

| prefix | **의미** | **예시**                         |
|--------|--------|--------------------------------|
| is     | 상태 여부  | `isActive`, `isDeleted`        |
| has    | 소유 여부  | `hasPermission`, `hasNextPage` |
| can    | 가능 여부  | `canEdit`, `canDelete`         |

---

## 6. 인증/계정 관련 메서드 네이밍

- `login`, `logout`, `signup`, `withdraw`, `reissue` 규칙 제외
- 일반적인 CRUD와 달리 **고유의 의미가 명확한 도메인 동작**이므로 예외적으로 메서드명으로 허용함
- **Controller 계층**에서는 요청 동작 그대로 사용 가능
- **Service 계층**에서는 도메인을 명시하여 의미를 더 분명히 하는 것을 권장
    - 예: `loginUser()`, `logoutUser()`, `signupUser()`, `withdrawUser()`, `reissueToken()`

---

## 7. 컬렉션 변수 네이밍

- **항상 복수형으로 작성**, 의미 있는 접미사 포함 권장
- 예시: `users`, `orderList`, `commentResponses`, `notificationMessages`

---

## 8. 도메인 행위형 메서드 네이밍

| **동사**        | **용도**              |
|---------------|---------------------|
| **`confirm`** | 상태 확정 (예: 결제, 예약 등) |
| **`cancel`**  | 취소 (예: 주문, 신청 등)    |
| **`approve`** | 승인                  |
| **`reject`**  | 거절                  |
| **`apply`**   | 신청, 적용 등            |
| **`publish`** | 게시, 노출 등            |
| **`connect`** | 연결                  |
| **`send`**    | 전송                  |

## 9. 명시적 행위형 메서드 네이밍

| **동사**         | **용도**    |
|----------------|-----------|
| **`follow`**   | 다른 유저 팔로우 |
| **`unfollow`** | 팔로우 취소    |
| **`like`**     | 좋아요       |
| **`bookmark`** | 북마크 저장    |
| **`report`**   | 신고        |

## 10. 주의사항

- 위 규칙에 포함되지 않은 네이밍은 반드시 **팀원 간 합의**를 거치고,

  **명확한 의미**를 드러낼 수 있도록 작성한다.