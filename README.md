# 🚀 Project Catalyst

> Elite reusable boilerplate with robust payment system (Stripe) and authentication - Built to GrayHair Standard

[![Java](https://img.shields.io/badge/Java-25-orange.svg)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.1-green.svg)](https://spring.io/projects/spring-boot)
[![Next.js](https://img.shields.io/badge/Next.js-15-black.svg)](https://nextjs.org/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue.svg)](https://www.postgresql.org/)
[![License](https://img.shields.io/badge/License-Proprietary-red.svg)](LICENSE)

## 📖 Overview

Project Catalyst is a production-ready foundation for SaaS applications. It demonstrates senior-level engineering by implementing a scalable, secure, and event-driven architecture from day one.

### 🌟 Engineering Highlights

- **Clean Architecture**: Strict adherence to Hexagonal Architecture (Ports & Adapters) to isolate domain logic from external frameworks.
- **Modern Concurrency**: Leveraging **Java 25 LTS Virtual Threads** for superior performance and resource efficiency.
- **Enterprise Security**: Layered security model with Spring Security, JWT validation, and multi-schema database isolation.
- **Event-Driven Resilience**: Asynchronous communication via **Apache Kafka (KRaft mode)** for high availability and decoupled services.
- **Observability**: Built-in audit logging and health monitoring for all infrastructure components.

### Key Features

- 💳 **Stripe Integration**: Complete payment and subscription management
- 🔐 **Robust Authentication**: Social login with NextAuth.js + JWT with Spring Security
- 🏗️ **Hexagonal Architecture**: Clean, maintainable, and testable code
- 📨 **Event-Driven**: Apache Kafka for asynchronous processing
- ⚡ **High Performance**: Java 25 LTS Virtual Threads + Redis caching
- 🎨 **Modern UI**: Next.js 15 + Shadcn/UI + Tailwind CSS
- 🐳 **Containerized**: Docker-ready infrastructure
- 🧪 **Well Tested**: Comprehensive test coverage with JUnit, Vitest, and Playwright

## 🛠️ Tech Stack

### Backend
- **Java 25 LTS** with Virtual Threads (Premier Support until 2030)
- **Spring Boot 4.0.1** (Framework)
- **Maven** (Build tool)
- **PostgreSQL 16** (Database)
- **Redis 7** (Caching)
- **Apache Kafka 3.6** (Message queue)

### Frontend
- **Next.js 15** with App Router
- **TypeScript** (Type safety)
- **Tailwind CSS** (Styling)
- **Shadcn/UI** (Components)
- **Tiptap** (Rich text editor)
- **NextAuth.js** (Authentication)

### Infrastructure
- **Docker** & **Docker Compose**
- **LocalStack** (AWS S3 simulation)
- **Dokploy** (Deployment)

### Testing
- **JUnit 5** (Backend unit tests)
- **Testcontainers** (Integration tests)
- **Vitest** (Frontend unit tests)
- **Playwright** (E2E tests)

## 🚀 Quick Start

1. **Clone & Setup**:
   ```bash
   git clone <repository-url>
   cd catalyst-pay
   ```

2. **Launch Infrastructure**:
   Follow the [Infrastructure Guide](infra/README.md) to start PostgreSQL, Kafka, and other services.

3. **Backend Setup**:
   (Phase 2 in progress...)

## 📁 Project Structure

```
catalyst-pay/
├── backend/                    # Java backend (Maven multi-module)
│   ├── shared-kernel/         # Shared utilities, auth, security
│   └── payment-service/       # Payment domain (Hexagonal Architecture)
│       ├── domain/            # Business logic (framework-agnostic)
│       ├── application/       # Use cases and ports
│       └── infrastructure/    # Adapters (Spring, DB, Stripe)
├── frontend/                   # Next.js frontend
│   ├── src/core/              # Shared UI, API clients, hooks
│   ├── src/features/pay/      # Payment feature module
│   └── src/app/               # Pages and layouts
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

### Feature-Based Organization (Frontend)

```
features/
└── payment/
    ├── components/    # UI components
    ├── hooks/         # Custom React hooks
    ├── services/      # API calls
    └── types/         # TypeScript types
```

## 🗄️ Database Schema

The system uses a multi-schema PostgreSQL architecture for strict data isolation. See the [Infrastructure Manual](infra/README.md#database-schema) for the detailed entity relationship overview.

## 🔐 Security

- JWT tokens for authentication
- Spring Security on backend
- NextAuth.js on frontend
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

### Frontend Tests
```bash
cd frontend
npm test
```

### E2E Tests
```bash
cd frontend
npm run test:e2e
```

## 📄 License

This project is proprietary. All rights reserved.

---

**Note**: This is V0.1 focusing on Payments & Authentication. Future versions will expand functionality.


