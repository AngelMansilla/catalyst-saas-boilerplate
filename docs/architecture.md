# Catalyst Architecture

## Overview

Catalyst follows a **Microservices Architecture** with **Hexagonal Architecture** (Ports & Adapters) within each service. This ensures clean separation between business logic and infrastructure concerns, making the system testable, maintainable, and framework-agnostic at the domain level.

## High-Level System Architecture

```
                        ┌──────────────────────┐
                        │      Frontend         │
                        │    (Angular 21)       │
                        └──────────┬───────────┘
                                   │
                        ┌──────────▼───────────┐
                        │     API Gateway       │
                        │  (Future: Kong/Traefik)│
                        └──────────┬───────────┘
                                   │
          ┌────────────────────────┼────────────────────────┐
          ▼                        ▼                        ▼
   ┌──────────────┐       ┌──────────────┐       ┌──────────────┐
   │ user-service  │       │payment-service│       │notification- │
   │               │       │               │       │  service     │
   │ - Auth        │       │ - Stripe      │       │ - Emails     │
   │ - Users       │       │ - Subs        │       │ - Templates  │
   │ - Profiles    │       │ - Invoices    │       │              │
   └──────┬───────┘       └──────┬───────┘       └──────┬───────┘
          │                      │                      │
          └──────────────────────┼──────────────────────┘
                                 │
                       ┌─────────▼─────────┐
                       │   shared-kernel    │
                       │                    │
                       │ - JWT Security     │
                       │ - Rate Limiting    │
                       │ - Exceptions       │
                       │ - Encryption       │
                       │ - Utilities        │
                       └─────────┬─────────┘
                                 │
            ┌────────────────────┼────────────────────┐
            ▼                    ▼                    ▼
      ┌──────────┐         ┌──────────┐         ┌──────────┐
      │PostgreSQL│         │  Redis   │         │  Kafka   │
      │          │         │          │         │          │
      │- auth    │         │- Cache   │         │- Events  │
      │- payment │         │- Sessions│         │- Async   │
      │- audit   │         │- Rate    │         │          │
      └──────────┘         └──────────┘         └──────────┘
```

## Service Decomposition

### user-service
Handles authentication and user lifecycle management. Supports dual authentication (local email/password and OAuth via Google/GitHub), password reset flows, role-based access control, and profile management.

### payment-service
Manages Stripe integration including checkout sessions, subscription lifecycle, invoice tracking, and webhook processing. Owns the `payment` database schema.

### notification-service
Consumes Kafka events from other services and sends transactional emails using Thymeleaf templates. Supports 8 email notification types with retry and dead-letter queue (DLQ) handling.

### shared-kernel
A library module (not a standalone service) providing cross-cutting concerns: JWT token management, Spring Security configuration, rate limiting, encryption, global exception handling, and common utilities.

## Hexagonal Architecture (Per Service)

Each service follows a strict three-layer hexagonal architecture:

```
┌──────────────────────────────────────────┐
│              Domain Layer                │
│  - Entities (pure business logic)        │
│  - Value Objects (immutable)             │
│  - Domain Events                         │
│  - Domain Exceptions                     │
│  - NO framework annotations              │
└──────────────────────────────────────────┘
                    │
┌──────────────────────────────────────────┐
│           Application Layer              │
│  - Use Cases (input ports)               │
│  - Repository Ports (output)             │
│  - External Service Ports                │
│  - DTOs for boundaries                   │
└──────────────────────────────────────────┘
                    │
┌──────────────────────────────────────────┐
│          Infrastructure Layer            │
│  - REST Controllers (Spring)             │
│  - JPA Entities & Repositories           │
│  - External API Adapters (Stripe)        │
│  - Message Consumers (Kafka)             │
│  - Configuration                         │
└──────────────────────────────────────────┘
```

**Key rules:**
1. The domain layer has zero framework dependencies (no Spring annotations).
2. All external dependencies are injected via ports (interfaces).
3. Use cases in the application layer orchestrate domain logic.
4. Infrastructure adapters implement ports defined in the application layer.
5. Each service owns its own data (database-per-service via schema separation).

## Backend Module Structure

```
/backend (Maven Multi-module)
├── pom.xml                    # Parent POM
├── shared-kernel/             # Cross-cutting concerns (library)
│   ├── domain/                # DTOs, Value Objects, Exceptions
│   ├── application/           # Ports (interfaces)
│   └── infrastructure/        # Security, Rate Limiting, Utils
├── user-service/              # Authentication & User Management
│   ├── domain/                # User, Role, PasswordResetToken
│   ├── application/           # Use cases, ports
│   └── infrastructure/        # Controllers, JPA, adapters
├── payment-service/           # Payments & Subscriptions
│   ├── domain/                # Subscription, Invoice, Payment
│   ├── application/           # Use cases, ports
│   └── infrastructure/        # Stripe, Controllers, JPA
└── notification-service/      # Email Notifications
    ├── domain/                # Notification, Template
    ├── application/           # Use cases, ports
    └── infrastructure/        # Kafka consumer, SMTP
```

## Frontend Structure (Planned)

```
/frontend (Angular 21 — not yet implemented)
├── src/
│   ├── app/
│   │   ├── core/              # Core module (singleton services)
│   │   │   ├── auth/          # Auth guards, interceptors
│   │   │   ├── api/           # HTTP client, API services
│   │   │   └── models/        # Shared models/interfaces
│   │   ├── shared/            # Shared module
│   │   │   ├── components/    # Reusable components
│   │   │   ├── directives/    # Custom directives
│   │   │   └── pipes/         # Custom pipes
│   │   ├── features/          # Feature modules (lazy loaded)
│   │   │   ├── auth/          # Login, register, reset
│   │   │   ├── payment/       # Payment/checkout
│   │   │   ├── dashboard/     # Dashboard
│   │   │   ├── settings/      # Settings/profile
│   │   │   ├── marketing/     # Landing, pricing (public)
│   │   │   └── admin/         # Admin module
│   │   ├── layout/            # Layout components
│   │   └── app.config.ts      # App configuration
│   ├── assets/                # Static assets
│   └── styles/                # Global styles (Tailwind)
├── angular.json
└── package.json
```

## Communication Patterns

### Event-Driven Architecture (Kafka)

Services communicate asynchronously via Kafka topics:

| Topic | Producer | Consumer |
|-------|----------|----------|
| `catalyst.user.registered` | user-service | notification-service |
| `catalyst.user.password-reset` | user-service | notification-service |
| `catalyst.payment.subscription-created` | payment-service | notification-service, user-service |
| `catalyst.payment.failed` | payment-service | notification-service |
| `catalyst.invoice.paid` | payment-service | notification-service |

Failed messages are retried via `@RetryableTopic` and eventually routed to a dead-letter queue (DLQ) for manual inspection.

### Database Per Service (Schema Separation)

```
PostgreSQL
├── auth schema      → user-service
├── payment schema   → payment-service
└── audit schema     → shared (event log)
```

## Key Flows

### Authentication Flow
1. User submits credentials via login form (Angular frontend — planned).
2. `POST /api/auth/login` hits user-service.
3. Spring Security validates credentials.
4. JWT + Refresh Token are returned.
5. Frontend stores tokens and attaches JWT via HTTP interceptor on subsequent requests (Angular — planned).
6. `JwtAuthenticationFilter` validates the token on every protected request.

### Payment Flow
1. User selects a plan and proceeds to checkout.
2. payment-service creates a Stripe Checkout Session.
3. Stripe Elements renders the payment form.
4. After payment, Stripe sends a webhook to payment-service.
5. Subscription is updated in the database.
6. A Kafka event is published, consumed by notification-service to send a confirmation email.

### Email Notification Flow
1. Any service publishes a Kafka event (e.g., user registered, payment failed).
2. notification-service consumes the event.
3. Thymeleaf renders the email template.
4. Email is sent via SMTP (Mailpit in dev, SES in prod).

## Security Architecture

### Security Filter Chain Order
1. `CorsFilter`
2. `RateLimitFilter`
3. `JwtAuthenticationFilter`
4. `ExceptionTranslationFilter`
5. `AuthorizationFilter`

### Rate Limiting (Tiered)
| Tier | Limit |
|------|-------|
| Anonymous | 30 requests/minute |
| Authenticated | 100 requests/minute |
| Premium | 500 requests/minute |

### JWT Authentication
- Tokens are signed and validated per request.
- Claims are extracted and set in the Spring `SecurityContext`.
- Method-level authorization via `@PreAuthorize`.

## Observability

### Health Checks
- `GET /actuator/health` — overall health
- `GET /actuator/health/liveness` — liveness probe
- `GET /actuator/health/readiness` — readiness probe (db, redis, kafka)
- Custom health indicators: `DatabaseHealthIndicator`, `RedisHealthIndicator`, `KafkaHealthIndicator`

### Structured Logging
JSON-formatted logs with correlation IDs for cross-service tracing:
```json
{
  "timestamp": "2026-01-11T10:30:00Z",
  "level": "INFO",
  "service": "payment-service",
  "correlationId": "abc-123",
  "userId": "user-456",
  "message": "Subscription created"
}
```

### Audit Trail
```sql
audit.event_log (
  id, event_type, entity_type, entity_id,
  user_id, event_data, created_at, ip_address
)
```

## Testing Strategy

| Level | Tool | Scope |
|-------|------|-------|
| Unit Tests | JUnit 5 + Mockito | Domain, Application layers |
| Integration Tests | Testcontainers | Repositories, Kafka consumers |
| Contract Tests | MockMvc | REST API contracts |
| E2E Tests | Playwright | Critical user flows |
| Architecture Tests | ArchUnit | Layer dependency enforcement |

## CI/CD Pipeline (Planned)

```
Push → GitHub Actions
          │
          ├── Build & Test
          ├── Lint & Type Check
          ├── Security Scan
          ├── Build Docker Images
          └── Deploy to Environment
```

### Environment Profiles
| Profile | Purpose |
|---------|---------|
| `dev` | Local development (Docker Compose) |
| `staging` | Pre-production testing |
| `prod` | Production deployment |

## Architectural Decisions for MVP (V0.1)

### Implemented
| Pattern | Implementation |
|---------|---------------|
| DDD / Bounded Contexts | Hexagonal architecture per service |
| Event-Driven | Kafka for async communication |
| Database per Service | Schema isolation (auth, payment, audit) |
| Security | JWT, OAuth2, rate limiting, encryption |
| Observability | Health checks + structured logging |
| Basic Retry | `@RetryableTopic` with DLQ |

### Intentionally Deferred
| Pattern | Target Version | Rationale |
|---------|---------------|-----------|
| Circuit Breakers | V0.2 | Low traffic; manual recovery acceptable |
| Distributed Tracing | V0.2 | Structured logs sufficient for MVP |
| Saga Pattern | V0.3 | Simple flows; Stripe handles consistency |
| Multi-Region HA | Future | Cost not justified until SLA required |
| Full GDPR Suite | Pre-EU launch | Basic deletion endpoint covers MVP |

See `docs/decisions/` for individual ADRs and `docs/tech-debt.md` for the full technical debt tracker.
