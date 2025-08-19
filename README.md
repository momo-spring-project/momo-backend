# MOMO 프로젝트

## 📋 목차

1. [팀원 소개](#1-팀원-소개)
2. [프로젝트 소개](#2-프로젝트-소개)
3. [기술 스택](#3-기술-스택)
4. [아키텍처](#4-아키텍처)
5. [프로젝트 설계](#5-프로젝트-설계)
6. [API 명세서](#6-api-명세서)
7. [도메인 별 문서](#7-도메인-별-문서)
8. [기술적 의사결정](#8-기술적-의사결정)
9. [트러블 슈팅](#9-트러블-슈팅)

---

## 1. 팀원 소개

<div align="center">

<table>
<tr>
<td align="center" width="200" style="border: 2px solid #ddd; padding: 20px; margin: 10px;">
<img src="https://github.com/Mybread2.png" width="120" style="border-radius: 10px;"/>
<br><br>
<strong style="font-size: 18px;">팀장</strong>
<br>
<strong style="color: #0066cc;">차준호</strong>
<br><br>
<strong>담당</strong>
<br>
유저 도메인
<br>
배포 인프라 구축
<br><br>
<a href="https://github.com/Mybread2">
<img src="https://img.shields.io/badge/GitHub-181717?style=flat-square&logo=github&logoColor=white" alt="GitHub">
</a>
<br>
<a href="https://juno0112.tistory.com/category">
<img src="https://img.shields.io/badge/Blog-FF5722?style=flat-square&logo=tistory&logoColor=white" alt="Blog">
</a>
</td>

<td align="center" width="200" style="border: 2px solid #ddd; padding: 20px; margin: 10px;">
<img src="https://github.com/신영님깃허브아이디.png" width="120" style="border-radius: 10px;"/>
<br><br>
<strong style="font-size: 18px;">부팀장</strong>
<br>
<strong style="color: #0066cc;">김신영</strong>
<br><br>
<strong>담당</strong>
<br>
결제 도메인
<br><br><br>
<a href="https://github.com/신영님깃허브아이디">
<img src="https://img.shields.io/badge/GitHub-181717?style=flat-square&logo=github&logoColor=white" alt="GitHub">
</a>
<br>
<a href="신영님블로그주소">
<img src="https://img.shields.io/badge/Blog-FF5722?style=flat-square&logo=blogger&logoColor=white" alt="Blog">
</a>
</td>

<td align="center" width="200" style="border: 2px solid #ddd; padding: 20px; margin: 10px;">
<img src="https://github.com/동원님깃허브아이디.png" width="120" style="border-radius: 10px;"/>
<br><br>
<strong style="font-size: 18px;">팀원</strong>
<br>
<strong style="color: #0066cc;">고동원</strong>
<br><br>
<strong>담당</strong>
<br>
인증 / 인가
<br>
배포 인프라 구축
<br><br>
<a href="https://github.com/동원님깃허브아이디">
<img src="https://img.shields.io/badge/GitHub-181717?style=flat-square&logo=github&logoColor=white" alt="GitHub">
</a>
<br>
<a href="동원님블로그주소">
<img src="https://img.shields.io/badge/Blog-FF5722?style=flat-square&logo=blogger&logoColor=white" alt="Blog">
</a>
</td>
</tr>

<tr>
<td align="center" width="200" style="border: 2px solid #ddd; padding: 20px; margin: 10px;">
<img src="https://github.com/지운님깃허브아이디.png" width="120" style="border-radius: 10px;"/>
<br><br>
<strong style="font-size: 18px;">팀원</strong>
<br>
<strong style="color: #0066cc;">우지운</strong>
<br><br>
<strong>담당</strong>
<br>
알림 도메인
<br><br><br>
<a href="https://github.com/지운님깃허브아이디">
<img src="https://img.shields.io/badge/GitHub-181717?style=flat-square&logo=github&logoColor=white" alt="GitHub">
</a>
<br>
<a href="지운님블로그주소">
<img src="https://img.shields.io/badge/Blog-FF5722?style=flat-square&logo=blogger&logoColor=white" alt="Blog">
</a>
</td>

<td align="center" width="200" style="border: 2px solid #ddd; padding: 20px; margin: 10px;">
<img src="https://github.com/호진님깃허브아이디.png" width="120" style="border-radius: 10px;"/>
<br><br>
<strong style="font-size: 18px;">팀원</strong>
<br>
<strong style="color: #0066cc;">임호진</strong>
<br><br>
<strong>담당</strong>
<br>
모임 참가 로직
<br>
모니터링 구축
<br><br>
<a href="https://github.com/호진님깃허브아이디">
<img src="https://img.shields.io/badge/GitHub-181717?style=flat-square&logo=github&logoColor=white" alt="GitHub">
</a>
<br>
<a href="호진님블로그주소">
<img src="https://img.shields.io/badge/Blog-FF5722?style=flat-square&logo=blogger&logoColor=white" alt="Blog">
</a>
</td>

<td align="center" width="200" style="border: 2px solid #ddd; padding: 20px; margin: 10px;">
<img src="https://github.com/의현님깃허브아이디.png" width="120" style="border-radius: 10px;"/>
<br><br>
<strong style="font-size: 18px;">팀원</strong>
<br>
<strong style="color: #0066cc;">이의현</strong>
<br><br>
<strong>담당</strong>
<br>
모임 도메인
<br><br><br>
<a href="https://github.com/의현님깃허브아이디">
<img src="https://img.shields.io/badge/GitHub-181717?style=flat-square&logo=github&logoColor=white" alt="GitHub">
</a>
<br>
<a href="의현님블로그주소">
<img src="https://img.shields.io/badge/Blog-FF5722?style=flat-square&logo=blogger&logoColor=white" alt="Blog">
</a>
</td>
</tr>
</table>

</div>

---

## 2. 프로젝트 소개

### 개발 기간: 2025.07.17 ~ 2025.08.25

### 왜 MOMO인가?

새로운 사람들과 만나고 싶지만 어디서 어떻게 시작해야 할지 모르는 분들을 위한 **지역 기반 모임 플랫폼**입니다.

### 핵심 가치

- **쉬운 모임 생성**: 몇 번의 클릭으로 모임 개설
- **안전한 만남**: 신뢰도 시스템으로 검증된 사용자
- **편리한 결제**: 토스페이먼츠 연동으로 간편 정산

---

## 3. 기술 스택

<div align="center">

## Framework

<img src="https://img.shields.io/badge/Java-17-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white" alt="Java 17">
<img src="https://img.shields.io/badge/Spring%20Boot-3.5.3-6DB33F?style=for-the-badge&logo=spring&logoColor=white" alt="Spring Boot">

## Libraries

<img src="https://img.shields.io/badge/Spring%20Web-6DB33F?style=for-the-badge&logo=spring&logoColor=white" alt="Spring Web">
<img src="https://img.shields.io/badge/Spring%20WebFlux-6DB33F?style=for-the-badge&logo=spring&logoColor=white" alt="Spring WebFlux">
<img src="https://img.shields.io/badge/Spring%20Data%20JPA-6DB33F?style=for-the-badge&logo=spring&logoColor=white" alt="Spring Data JPA">
<img src="https://img.shields.io/badge/QueryDSL-4285F4?style=for-the-badge&logo=querydsl&logoColor=white" alt="QueryDSL">

## Database & Cache

<img src="https://img.shields.io/badge/MySQL-4479A1?style=for-the-badge&logo=mysql&logoColor=white" alt="MySQL">
<img src="https://img.shields.io/badge/Redis-DC382D?style=for-the-badge&logo=redis&logoColor=white" alt="Redis">
<img src="https://img.shields.io/badge/Redisson-DC382D?style=for-the-badge&logo=redis&logoColor=white" alt="Redisson">
<img src="https://img.shields.io/badge/Flyway-CC0200?style=for-the-badge&logo=flyway&logoColor=white" alt="Flyway">

## Search & Messaging

<img src="https://img.shields.io/badge/Elasticsearch-005571?style=for-the-badge&logo=elasticsearch&logoColor=white" alt="Elasticsearch">
<img src="https://img.shields.io/badge/RabbitMQ-FF6600?style=for-the-badge&logo=rabbitmq&logoColor=white" alt="RabbitMQ">
<img src="https://img.shields.io/badge/WebSocket-010101?style=for-the-badge&logo=websocket&logoColor=white" alt="WebSocket">

## External API

<img src="https://img.shields.io/badge/Toss%20Payments-0064FF?style=for-the-badge&logo=tosspayments&logoColor=white" alt="Toss Payments">
<img src="https://img.shields.io/badge/OAuth2-4285F4?style=for-the-badge&logo=oauth&logoColor=white" alt="OAuth2">
<img src="https://img.shields.io/badge/Firebase-FFCA28?style=for-the-badge&logo=firebase&logoColor=black" alt="FCM">
<img src="https://img.shields.io/badge/Slack%20API-4A154B?style=for-the-badge&logo=slack&logoColor=white" alt="Slack API">

## Security

<img src="https://img.shields.io/badge/Spring%20Security-6DB33F?style=for-the-badge&logo=springsecurity&logoColor=white" alt="Spring Security">
<img src="https://img.shields.io/badge/JWT-000000?style=for-the-badge&logo=jsonwebtokens&logoColor=white" alt="JWT">

## Monitoring & Test

<img src="https://img.shields.io/badge/Spring%20Actuator-6DB33F?style=for-the-badge&logo=spring&logoColor=white" alt="Actuator">
<img src="https://img.shields.io/badge/OpenTelemetry-000000?style=for-the-badge&logo=opentelemetry&logoColor=white" alt="OpenTelemetry">
<img src="https://img.shields.io/badge/Jaeger-66CFE3?style=for-the-badge&logo=jaeger&logoColor=black" alt="Jaeger">
<img src="https://img.shields.io/badge/Prometheus-E6522C?style=for-the-badge&logo=prometheus&logoColor=white" alt="Prometheus">
<img src="https://img.shields.io/badge/Grafana-F46800?style=for-the-badge&logo=grafana&logoColor=white" alt="Grafana">
<img src="https://img.shields.io/badge/Loki-F46800?style=for-the-badge&logo=grafana&logoColor=white" alt="Loki">

## Development Tools

<img src="https://img.shields.io/badge/Notion-000000?style=for-the-badge&logo=notion&logoColor=white" alt="Notion">
<img src="https://img.shields.io/badge/Discord-5865F2?style=for-the-badge&logo=discord&logoColor=white" alt="Discord">
<img src="https://img.shields.io/badge/Miro-050038?style=for-the-badge&logo=miro&logoColor=white" alt="Miro">
<img src="https://img.shields.io/badge/Slack-4A154B?style=for-the-badge&logo=slack&logoColor=white" alt="Slack">
<img src="https://img.shields.io/badge/draw.io-F08705?style=for-the-badge&logo=diagramsdotnet&logoColor=white" alt="draw.io">
<img src="https://img.shields.io/badge/Swagger-85EA2D?style=for-the-badge&logo=swagger&logoColor=black" alt="Swagger">
<img src="https://img.shields.io/badge/Lombok-CA4245?style=for-the-badge&logo=lombok&logoColor=white" alt="Lombok">
<img src="https://img.shields.io/badge/GitHub-181717?style=for-the-badge&logo=github&logoColor=white" alt="GitHub">

## Infra & CI/CD

<img src="https://img.shields.io/badge/AWS%20EC2-FF9900?style=for-the-badge&logo=amazonec2&logoColor=white" alt="AWS EC2">
<img src="https://img.shields.io/badge/AWS%20ECS-FF9900?style=for-the-badge&logo=amazonecs&logoColor=white" alt="AWS ECS">
<img src="https://img.shields.io/badge/AWS%20ECR-FF9900?style=for-the-badge&logo=amazonecr&logoColor=white" alt="AWS ECR">
<img src="https://img.shields.io/badge/AWS%20CloudFront-FF9900?style=for-the-badge&logo=amazoncloudfront&logoColor=white" alt="AWS CloudFront">
<img src="https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white" alt="Docker">
<img src="https://img.shields.io/badge/GitHub%20Actions-2088FF?style=for-the-badge&logo=githubactions&logoColor=white" alt="GitHub Actions">

</div>

---

## 4. 아키텍처

![project_architecture.png](docs/image/project_architecture.png)

[![AWS 월간 비용 예상](https://img.shields.io/badge/AWS_월간_비용_예상-374151?style=for-the-badge&logo=amazonaws&logoColor=white)](https://www.notion.so/teamsparta/AWS-2542dc3ef514806bb743d46620006130)

---

# 5. 프로젝트 설계

## 5.1 와이어프레임

<details>
<summary>📱 MOMO 플랫폼 와이어프레임 보기</summary>

[와이어프레임 HTML 파일](./docs/wireframe/meetup_wireframe.html)

**주요 화면:**

- 메인 화면 (모임 목록)
- 모임 상세 화면
- 모임 생성 화면
- 결제 화면
- 프로필 화면
- 알림 화면

</details>

## 5.2 ERD

![ERD](./docs/image/ERD.png)

## 5.3 패키지 구조

<!-- 패키지 구조 설명 -->

---

## 6. API 명세서

<!-- API 명세서 내용 -->

---

## 7. 도메인 별 문서

[![인증/인가 인수인계 문서](https://img.shields.io/badge/인증/인가_인수인계_문서-F59E0B?style=for-the-badge&logo=notion&logoColor=white)](https://www.notion.so/teamsparta/Momo-2542dc3ef51480d1a123c806a4ffd440)

[![User 도메인 인수인계 문서](https://img.shields.io/badge/User_도메인_인수인계_문서-000000?style=for-the-badge&logo=notion&logoColor=white)](https://www.notion.so/teamsparta/Momo-User-2542dc3ef514807b8a7bff4e6f1e4f27)

[![Meeting 도메인 인수인계 문서](https://img.shields.io/badge/Meeting_도메인_인수인계_문서-4285F4?style=for-the-badge&logo=notion&logoColor=white)](https://www.notion.so/teamsparta/Momo-Meeting-2542dc3ef514809d96bcd5d315f821cb)

[![Category&Participant 인수인계 문서](https://img.shields.io/badge/Category&Participant_인수인계_문서-10B981?style=for-the-badge&logo=notion&logoColor=white)](https://www.notion.so/teamsparta/Momo-Category-Participant-2542dc3ef514805998c1f4ea6427d1ad)

[![Message Hub 인수인계 문서](https://img.shields.io/badge/Message_Hub_인수인계_문서-8B5CF6?style=for-the-badge&logo=notion&logoColor=white)](https://www.notion.so/teamsparta/Momo-Message-Hub-2542dc3ef51480b088a4f5de53ab7d57)

[![Notification 인수인계 문서](https://img.shields.io/badge/Notification_인수인계_문서-EF4444?style=for-the-badge&logo=notion&logoColor=white)](https://www.notion.so/teamsparta/Momo-Notification-2542dc3ef5148082a188ebd5ae0096b2)

---

## 8. 기술적 의사결정

[![Redis 선택 이유](https://img.shields.io/badge/Redis_선택_이유-DC382D?style=for-the-badge&logo=redis&logoColor=white)](https://www.notion.so/teamsparta/Redis-2542dc3ef51480b793b9fec7f7eaa502)

[![MySQL 선택 이유](https://img.shields.io/badge/MySQL_선택_이유-4479A1?style=for-the-badge&logo=mysql&logoColor=white)](https://www.notion.so/teamsparta/MySQL-2542dc3ef51480a2b842c824991e8e7b)

[![Flyway 도입 배경](https://img.shields.io/badge/Flyway_도입_배경-CC0200?style=for-the-badge&logo=flyway&logoColor=white)](https://www.notion.so/teamsparta/Flyway-2542dc3ef514804ebabcfbdda7e5cb1b)

[![모니터링 시스템 구축](https://img.shields.io/badge/모니터링_시스템_구축-E6522C?style=for-the-badge&logo=prometheus&logoColor=white)](https://www.notion.so/teamsparta/2542dc3ef51480a1b63af542db36584d)

[![WebClient 도입 이유](https://img.shields.io/badge/WebClient_도입_이유-6DB33F?style=for-the-badge&logo=spring&logoColor=white)](https://www.notion.so/teamsparta/WebClient-2542dc3ef514801488f1d06351b79436)

[![FCM + SSE 선택 배경](https://img.shields.io/badge/FCM_+_SSE_선택_배경-FFCA28?style=for-the-badge&logo=firebase&logoColor=black)](https://www.notion.so/teamsparta/FCM-SSE-2542dc3ef5148089a524e0231f86c2c1)

[![RabbitMQ 도입 배경](https://img.shields.io/badge/RabbitMQ_도입_배경-FF6600?style=for-the-badge&logo=rabbitmq&logoColor=white)](https://www.notion.so/teamsparta/RabbitMQ-2542dc3ef51480d1957ddfbb4fd5bfc4)

---

## 9. 트러블 슈팅

<!-- 트러블 슈팅 내용 -->