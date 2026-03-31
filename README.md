# 🚀 Project Catalyst

> Elite reusable boilerplate with robust payment system (Stripe) and authentication - Built to GrayHair Standard

[![CI](https://github.com/AngelMansilla/Catalyst-Saas-Boilerplate/actions/workflows/ci.yml/badge.svg)](https://github.com/AngelMansilla/Catalyst-Saas-Boilerplate/actions/workflows/ci.yml)
[![Java](https://img.shields.io/badge/Java-25-orange.svg)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.1-green.svg)](https://spring.io/projects/spring-boot)
[![Angular](https://img.shields.io/badge/Angular-21-red.svg)](https://angular.dev/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue.svg)](https://www.postgresql.org/)
[![License](https://img.shields.io/badge/License-Proprietary-red.svg)](LICENSE)

## 📖 Overview

Project Catalyst is a production-ready foundation for SaaS applications. It demonstrates senior-level engineering by implementing a scalable, secure, and event-driven architecture from day one.

### 🌟 Engineering Highlights

- **Clean Architecture**: Strict adherence to Hexagonal Architecture (Ports & Adapters) to isolate domain logic from external frameworks.
- **Modern Concurrency**: Leveraging **Java 25 LTS Virtual Threads** for superior performance and resource efficiency.
- **Observability & Tracing**: Distributed tracing with **Correlation IDs** and structured **JSON logging** for enterprise monitoring (ELK/Datadog ready).
- **Event-Driven Resilience**: Asynchronous communication via **Apache Kafka (KRaft mode)** for high availability and decoupled services.
- **GDPR Compliance**: Native support for the "Right to Erasure" with automated user data deletion workflows.

### Key Features

- 💳 **Stripe Integration**: Complete payment and subscription management
- 🔐 **Robust Authentication**: OIDC/OAuth2 Integration + JWT with Spring Security
- 🏗️ **Hexagonal Architecture**: Clean, maintainable, and testable code
- 📨 **Event-Driven**: Apache Kafka for asynchronous processing (Registration, Payments, Deletion)
- ⚡ **High Performance**: Java 25 LTS Virtual Threads + Redis caching
- 🎨 **Modern UI**: Angular 21 + Angular Material + Signals State Management
- �️ **GDPR Compliant**: Built-in account deletion and data privacy flows
- 📈 **Observable**: Correlation IDs tracing and structured JSON logging
- 🧪 **Well Tested**: Comprehensive test coverage with JUnit, Vitest, and Playwright

## 🛠️ Tech Stack

### Backend
- **Java 25 LTS** with Virtual Threads (Premier Support until 2030)
- **Spring Boot 3.4.1** (Framework)
- **Maven** (Build tool)
- **PostgreSQL 16** (Database)
- **Redis 7** (Caching)
- **Apache Kafka 3.6** (Message queue)

### Frontend
- **Angular 21** (Signals, Standalone Components, Hydration)
- **TypeScript** (Type safety)
- **Angular Material** (Component library)
- **Tailwind CSS** (Styling)
- **OIDC / JWT** (Authentication)

### Infrastructure
- **Docker** & **Docker Compose**
- **LocalStack** (AWS S3 simulation)
- **Dokploy** (Deployment)

### Testing
- **JUnit 5** (Backend unit tests)
- **Testcontainers** (Integration tests)
- **Karma / Jest** (Frontend unit tests)
- **Playwright** (E2E tests)

## 🚀 Quick Start

1. **Clone & Setup**:
   ```bash
   git clone <repository-url>
   cd Catalys
   ```

2. **Launch Infrastructure**:
   Follow the [Infrastructure Guide](infra/README.md) to start PostgreSQL, Kafka, and other services.

3. **Backend Setup**:
   (Phase 2 in progress...)

## 📁 Project Structure

```
Catalys/
├── backend/                    # Java backend (Maven multi-module)
│   ├── shared-kernel/         # Observability, Security, Correlation Tracing
│   ├── user-service/          # Identity and Profile management
│   ├── notification-service/   # Dynamic email dispatch via Kafka
│   └── payment-service/       # Core Billing and Stripe integration
│       ├── domain/            # Business logic (framework-agnostic)
│       ├── application/       # Use cases and ports
│       └── infrastructure/    # Adapters (Spring, DB, Stripe, Kafka)
├── frontend/                   # Angular 21 frontend (Planned)
│   ├── src/app/core/          # Global services, guards, interceptors
│   ├── src/app/features/      # Feature modules (Lazy loaded)
│   └── src/app/shared/        # Reusable components, directives, pipes
├── infra/                      # Infrastructure configuration
│   ├── docker-compose.yml     # All services
│   ├── postgres/              # Database init scripts
│   └── localstack/            # S3 init scripts
└── README.md
```

## 🏗️ Architecture

### Hexagonal Architecture (Backend)

```
┌─────────────────────────────────────────┐
│           Application Layer             │
│         (Use Cases & Ports)             │
├─────────────────────────────────────────┤
│            Domain Layer                 │
│      (Business Logic - Pure Java)       │
├─────────────────────────────────────────┤
│        Infrastructure Layer             │
│  (Adapters: Spring, DB, Stripe, Kafka) │
└─────────────────────────────────────────┘
```

### Feature-Based Organization (Frontend — Planned)

```
features/
└── payment/
    ├── components/    # Angular components
    ├── services/      # API call services
    ├── models/        # TypeScript types/interfaces
    ├── guards/        # Route guards
    └── pipes/         # Custom pipes
```

## 🗄️ Database Schema

The system uses a multi-schema PostgreSQL architecture for strict data isolation. See the [Infrastructure Manual](infra/README.md#database-schema) for the detailed entity relationship overview.

## 🔐 Security

- JWT tokens for authentication
- Spring Security on backend
- OAuth2 / JWT on frontend
- CSRF Protection & Secure Cookies
- Rate limiting
- Input validation and sanitization
- Encrypted sensitive data
- Audit logging

## 🧪 Testing

### Backend Tests
```bash
cd backend
mvn test
```

### Frontend Tests (Planned)
```bash
cd frontend
ng test
```

### E2E Tests (Planned)
```bash
cd frontend
ng e2e
```

## 📄 License

This project is proprietary. All rights reserved.

---

**Note**: This is V0.1 focusing on Payments & Authentication. Future versions will expand functionality.


