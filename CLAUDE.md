# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

smartpayut-api — Spring Boot 3.3.13 microservices backend for SmartPayUT.

## Build & Run

Requires **Java 21** (Amazon Corretto via SDKMAN). Maven wrapper included.

```bash
# Compile all modules
./mvnw clean compile -DskipTests

# Package all modules
./mvnw clean package -DskipTests

# Run a single module (e.g., identity)
./mvnw -pl identity spring-boot:run

# Run with profile
./mvnw -pl identity spring-boot:run -Dspring-boot.run.profiles=dev

# Docker infrastructure (PostgreSQL, Redis)
docker-compose up -d db redis
```

## Architecture

Multi-module Maven project with 3 modules:

- **share** (`com.kynsof.share`) — Shared library. CQRS bus (IMediator, ICommandBus, IQueryBus), base entities, specifications, exceptions, Kafka/Redis config, API response DTOs, file upload service. All other modules depend on this.
- **gateway** (`com.kynsoft.gateway`) — Spring Cloud Gateway (WebFlux/reactive). Routes requests to microservices, JWT caching with Caffeine, OAuth2 resource server, user info header propagation (X-User-Id, X-User-Name, X-User-Email, X-User-Roles). Contains saga orchestrator for patient registration.
- **identity** (`com.kynsof.identity`) — User management and authentication. Integrates with Keycloak (admin client 24.0.1). CQRS pattern with command/query handlers. PostgreSQL with read/write datasource separation. RabbitMQ consumers. 2FA (TOTP), passwordless login, Google OAuth.

## Key Patterns

- **CQRS**: Commands and queries separated via `IMediator` from share module. Each operation has a Command/Query + Handler + Message/Response. Located under `application/command/` and `application/query/` in each module.
- **Saga Pattern**: `PatientRegistrationOrchestrator` in gateway coordinates distributed transactions across identity and patients services with compensation logic.
- **Read/Write DB Separation**: Identity module has `PostgresDBReadConfiguration` and `PostgresDBWriteConfiguration` for separate datasources.
- **JWT Caching**: Gateway uses `CachingReactiveJwtDecoder` with SHA-256 token hashing, dynamic TTL, and Caffeine cache (max 10K tokens, 5min default TTL).

## Config

All modules use `spring.config.import=configserver:http://${RC_SERVER_CONFIG:localhost}:8081` for centralized config. Local overrides in each module's `src/main/resources/application.properties`.

Environment variables: `RC_SERVER_CONFIG` (config server host), `RC_ACTIVE_PROFILE` (spring profile).

## Infrastructure

- PostgreSQL 15.2 (port 5431)
- Redis (port 6379)
- Keycloak (external identity provider)
- RabbitMQ (event-driven communication in identity)

## Module Dependencies

```
smartpayut-api (parent pom)
├── share (standalone library)
├── gateway (depends on share via parent)
└── identity (depends on share via parent)
```
