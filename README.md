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

<!-- 팀원 소개 내용 -->

---

## 2. 프로젝트 소개

<!-- 프로젝트 소개 내용 -->

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

<!-- 아키텍처 내용 -->

---

## 5. 프로젝트 설계

<!-- 프로젝트 설계 내용 -->

---

## 6. API 명세서

<!-- API 명세서 내용 -->

---

## 7. 도메인 별 문서

<!-- 도메인 별 문서 내용 -->

---

## 8. 기술적 의사결정

<!-- 기술적 의사결정 내용 -->

---

## 9. 트러블 슈팅

<!-- 트러블 슈팅 내용 -->