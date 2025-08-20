# MOMO 프로젝트

<img src="docs/image/momo_cover_image.png" width="300" alt="MOMO 커버 이미지">

## 📋 목차

1. [팀원 소개](#1-팀원-소개)
2. [프로젝트 소개](#2-프로젝트-소개)
3. [기술 스택](#3-기술-스택)
4. [아키텍처](#4-아키텍처)
5. [프로젝트 설계](#5-프로젝트-설계)
6. [API 명세서](#6-api-명세서)
7. [주요 서비스 플로우](#7-주요-서비스-플로우)
8. [도메인 별 문서](#8-도메인-별-문서)
9. [기술적 의사결정](#9-기술적-의사결정)
10. [트러블 슈팅](#10-트러블-슈팅)

---

## 1. 팀원 소개

<p align="center">


</p>

<p align="center">
<img src="https://github.com/Mybread2.png" width="120" style="border-radius: 10px;"/>
&nbsp;&nbsp;&nbsp;&nbsp;
<img src="https://github.com/ZeroColaa.png" width="120" style="border-radius: 10px;"/>
&nbsp;&nbsp;&nbsp;&nbsp;
<img src="https://github.com/ko-dongwon.png" width="120" style="border-radius: 10px;"/>
</p>

<p align="center">
<strong>차준호 (팀장)</strong>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
<strong>김신영 (부팀장)</strong>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
<strong>고동원 (팀원)</strong>
</p>

<p align="center">
유저 도메인, 배포 인프라
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
결제 도메인
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
인증/인가, 배포 인프라
</p>

<p align="center">
<a href="https://github.com/Mybread2">
<img src="https://img.shields.io/badge/GitHub-181717?style=flat-square&logo=github&logoColor=white" alt="GitHub">
</a>
<a href="https://juno0112.tistory.com/category">
<img src="https://img.shields.io/badge/Blog-FF5722?style=flat-square&logo=tistory&logoColor=white" alt="Blog">
</a>
&nbsp;&nbsp;&nbsp;&nbsp;
<a href="https://github.com/ZeroColaa">
<img src="https://img.shields.io/badge/GitHub-181717?style=flat-square&logo=github&logoColor=white" alt="GitHub">
</a>
<a href="https://velog.io/@eggtart21">
<img src="https://img.shields.io/badge/Blog-FF5722?style=flat-square&logo=blogger&logoColor=white" alt="Blog">
</a>
&nbsp;&nbsp;&nbsp;&nbsp;
<a href="https://github.com/ko-dongwon">
<img src="https://img.shields.io/badge/GitHub-181717?style=flat-square&logo=github&logoColor=white" alt="GitHub">
</a>
<a href="https://velog.io/@kodongwon/posts">
<img src="https://img.shields.io/badge/Blog-FF5722?style=flat-square&logo=blogger&logoColor=white" alt="Blog">
</a>
</p>

<br>

<p align="center">
<img src="https://github.com/Zyooon.png" width="120" style="border-radius: 10px;"/>
&nbsp;&nbsp;&nbsp;&nbsp;
<img src="https://github.com/hojin915.png" width="120" style="border-radius: 10px;"/>
&nbsp;&nbsp;&nbsp;&nbsp;
<img src="https://github.com/leeuihyun.png" width="120" style="border-radius: 10px;"/>
</p>

<p align="center">
<strong>우지운 (팀원)</strong>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
<strong>임호진 (팀원)</strong>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
<strong>이의현 (팀원)</strong>
</p>

<p align="center">
알림 도메인
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
모임 참가, 모니터링
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
모임 도메인
</p>

<p align="center">
<a href="https://github.com/Zyooon">
<img src="https://img.shields.io/badge/GitHub-181717?style=flat-square&logo=github&logoColor=white" alt="GitHub">
</a>
<a href="https://velog.io/@wcw7373">
<img src="https://img.shields.io/badge/Blog-FF5722?style=flat-square&logo=blogger&logoColor=white" alt="Blog">
</a>
&nbsp;&nbsp;&nbsp;&nbsp;
<a href="https://github.com/hojin915">
<img src="https://img.shields.io/badge/GitHub-181717?style=flat-square&logo=github&logoColor=white" alt="GitHub">
</a>
<a href="https://velog.io/@hojin915">
<img src="https://img.shields.io/badge/Blog-FF5722?style=flat-square&logo=blogger&logoColor=white" alt="Blog">
</a>
&nbsp;&nbsp;&nbsp;&nbsp;
<a href="https://github.com/leeuihyun">
<img src="https://img.shields.io/badge/GitHub-181717?style=flat-square&logo=github&logoColor=white" alt="GitHub">
</a>
<a href="https://fecpp.tistory.com/">
<img src="https://img.shields.io/badge/Blog-FF5722?style=flat-square&logo=blogger&logoColor=white" alt="Blog">
</a>
</p>

---

## 2. 프로젝트 소개

### 개발 기간: 2025.07.17 ~ 2025.08.25

### 왜 MOMO인가?

새로운 사람들과 만나고 싶지만 어디서 어떻게 시작해야 할지 모르는 분들을 위한 **지역 기반 모임 플랫폼**입니다.

### 핵심 가치

- **쉬운 모임 생성**: 몇 번의 클릭으로 모임 개설
- **안전한 만남**: 신뢰도 시스템으로 검증된 사용자
- **편리한 결제**: 토스페이먼츠 연동으로 간편 정산
- **알림 시스템**: 관심 모임, 주변 모임 생성 등 다양한 알림을 통해 모임을 참석, 관리

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

[![와이어프레임](https://img.shields.io/badge/와이어프레임-F59E0B?style=for-the-badge&logo=notion&logoColor=white)](https://www.notion.so/teamsparta/Momo-2542dc3ef514803cb6bbe25d8e1fe994)

## 5.2 ERD

![ERD](./docs/image/ERD.png)

## 5.3 패키지 구조

[![패키지 구조](https://img.shields.io/badge/패키지_구조-6DB33F?style=for-the-badge&logo=notion&logoColor=white)](https://www.notion.so/teamsparta/Momo-2542dc3ef51480f79a40de05adca944f)

---

## 6. API 명세서

[![Auth 도메인 API](https://img.shields.io/badge/Auth_도메인_API-F59E0B?style=for-the-badge&logo=notion&logoColor=white)](https://www.notion.so/teamsparta/Auth-2552dc3ef51480029e58fe75a68a5e40)

[![User 도메인 API](https://img.shields.io/badge/User_도메인_API-000000?style=for-the-badge&logo=notion&logoColor=white)](https://www.notion.so/teamsparta/User-2552dc3ef51480f8be53c0ea5f030c39)

[![Meeting 도메인 API](https://img.shields.io/badge/Meeting_도메인_API-4285F4?style=for-the-badge&logo=notion&logoColor=white)](https://www.notion.so/teamsparta/Meeting-2552dc3ef51480388e11c603c67b342c)

[![Payment 도메인 API](https://img.shields.io/badge/Payment_도메인_API-10B981?style=for-the-badge&logo=notion&logoColor=white)](https://www.notion.so/teamsparta/Payment-2552dc3ef51480de88adcdcafad08900)

[![Notification 도메인 API](https://img.shields.io/badge/Notification_도메인_API-EF4444?style=for-the-badge&logo=notion&logoColor=white)](https://www.notion.so/teamsparta/Notification-2552dc3ef51480d6bd1ec203c01830eb)

[![Category 도메인 API](https://img.shields.io/badge/Category_도메인_API-8B5CF6?style=for-the-badge&logo=notion&logoColor=white)](https://www.notion.so/teamsparta/Category-2552dc3ef514807ead77cfecd2005a1d)


---

## 7. 주요 서비스 플로우

### 모임 참가

1. 사용자 모임 참가 신청
   사용자가 모임에 참가 신청하면 자격/정원/시간을 검증합니다.
2. 결제 요청 이벤트 기록/발행
   유료 모임이면 결제 정보를 생성하고 PG로 결제 승인을 시도합니다. (무료면 즉시 참가 완료)
3. 결과 반영
   결제가 성공하면 참가가 확정되고, 실패하면 신청이 취소 됩니다.
4. 알림 및 기록
   결과를 사용자/호스트에게 알림으로 보내고, 이력을 저장/발행 합니다.

---

![모임 참가 흐름도](./docs/image/participants_create_image.png)
![결제 상세 흐름도](./docs/image/payment_create_image.png)

---

### 모임 생성

1. 모임 등록
   호스트가 모임 정보를 입력하고 모임을 생성합니다.
2. 알림 준비
   create 이벤트가 메세지 허브로 전달되고
   메세지 허브에서 대상 사용자를 조회합니다.
   이후 메세지를 가공/조합 합니다.
3. 알림 발송
   알림 서비스에서 푸시/알림을 전송합니다.

---

![모임 생성 흐름도](./docs/image/meeting_create_image.png)

---

## 8. 도메인 별 문서

[![인증/인가 문서](https://img.shields.io/badge/인증/인가_문서-F59E0B?style=for-the-badge&logo=notion&logoColor=white)](https://www.notion.so/teamsparta/Momo-2542dc3ef51480d1a123c806a4ffd440)

[![User 도메인 문서](https://img.shields.io/badge/User_도메인_문서-000000?style=for-the-badge&logo=notion&logoColor=white)](https://www.notion.so/teamsparta/Momo-User-2542dc3ef514807b8a7bff4e6f1e4f27)

[![Meeting 도메인 문서](https://img.shields.io/badge/Meeting_도메인_문서-4285F4?style=for-the-badge&logo=notion&logoColor=white)](https://www.notion.so/teamsparta/Momo-Meeting-2542dc3ef514809d96bcd5d315f821cb)

[![Category&Participant 문서](https://img.shields.io/badge/Category&Participant_문서-10B981?style=for-the-badge&logo=notion&logoColor=white)](https://www.notion.so/teamsparta/Momo-Category-Participant-2542dc3ef514805998c1f4ea6427d1ad)

[![Message Hub 문서](https://img.shields.io/badge/Message_Hub_문서-8B5CF6?style=for-the-badge&logo=notion&logoColor=white)](https://www.notion.so/teamsparta/Momo-Message-Hub-2542dc3ef51480b088a4f5de53ab7d57)

[![Notification 문서](https://img.shields.io/badge/Notification_문서-EF4444?style=for-the-badge&logo=notion&logoColor=white)](https://www.notion.so/teamsparta/Momo-Notification-2542dc3ef5148082a188ebd5ae0096b2)

---

## 9. 기술적 의사결정

[![Redis 선택 이유](https://img.shields.io/badge/Redis_선택_이유-DC382D?style=for-the-badge&logo=redis&logoColor=white)](https://www.notion.so/teamsparta/Redis-2542dc3ef51480b793b9fec7f7eaa502)

[![MySQL 선택 이유](https://img.shields.io/badge/MySQL_선택_이유-4479A1?style=for-the-badge&logo=mysql&logoColor=white)](https://www.notion.so/teamsparta/MySQL-2542dc3ef51480a2b842c824991e8e7b)

[![Flyway 도입 배경](https://img.shields.io/badge/Flyway_도입_배경-CC0200?style=for-the-badge&logo=flyway&logoColor=white)](https://www.notion.so/teamsparta/Flyway-2542dc3ef514804ebabcfbdda7e5cb1b)

[![모니터링 시스템 구축](https://img.shields.io/badge/모니터링_시스템_구축-E6522C?style=for-the-badge&logo=prometheus&logoColor=white)](https://www.notion.so/teamsparta/2542dc3ef51480a1b63af542db36584d)

[![WebClient 도입 이유](https://img.shields.io/badge/WebClient_도입_이유-6DB33F?style=for-the-badge&logo=spring&logoColor=white)](https://www.notion.so/teamsparta/WebClient-2542dc3ef514801488f1d06351b79436)

[![FCM + SSE 선택 배경](https://img.shields.io/badge/FCM_+_SSE_선택_배경-FFCA28?style=for-the-badge&logo=firebase&logoColor=black)](https://www.notion.so/teamsparta/FCM-SSE-2542dc3ef5148089a524e0231f86c2c1)

[![RabbitMQ 도입 배경](https://img.shields.io/badge/RabbitMQ_도입_배경-FF6600?style=for-the-badge&logo=rabbitmq&logoColor=white)](https://www.notion.so/teamsparta/RabbitMQ-2542dc3ef51480d1957ddfbb4fd5bfc4)

[![다중 JWT](https://img.shields.io/badge/다중_JWT-000000?style=for-the-badge&logo=jsonwebtokens&logoColor=white)](https://www.notion.so/teamsparta/JWT-2542dc3ef514806ca31dc919f74a45b0)

[![AWS ECS + EC2 다중인스턴스 + ECR](https://img.shields.io/badge/AWS_ECS_+_EC2_다중인스턴스_+_ECR-FF9900?style=for-the-badge&logo=amazonecs&logoColor=white)](https://www.notion.so/teamsparta/AWS-ECS-EC2-ECR-2542dc3ef51480fda5c4e996502f5bf2)

[![NAT Gateway + Bastion](https://img.shields.io/badge/NAT_Gateway_+_Bastion-232F3E?style=for-the-badge&logo=amazonaws&logoColor=white)](https://www.notion.so/teamsparta/NAT-Gateway-Bastion-2552dc3ef51480c5afd7ef54a50af0f3)

[![CloudFront](https://img.shields.io/badge/CloudFront-FF9900?style=for-the-badge&logo=amazoncloudfront&logoColor=white)](https://www.notion.so/teamsparta/CloudFront-2552dc3ef51480e3bba9c13ccf1b46f0)

---

## 10. 트러블 슈팅

<!-- 트러블 슈팅 내용 -->