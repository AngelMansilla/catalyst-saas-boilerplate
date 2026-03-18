# Technical Debt Tracking

> Tracks intentional technical decisions made for MVP speed that should be revisited in future versions.

## Overview

**Current Version**: V0.1 (MVP)
**Next Review**: V0.2 Planning

---

## Deferred Items (Intentional for MVP)

### HIGH PRIORITY (V0.2)

#### 1. Circuit Breakers & Bulkheads

| Attribute | Value |
|-----------|-------|
| **Status** | Deferred to V0.2 |
| **Risk Level** | Medium |
| **Why Deferred** | Low user count in MVP makes manual recovery acceptable |
| **What We Have** | Basic retry with DLQ in notification-service (`@RetryableTopic`) |
| **What's Missing** | Resilience4j circuit breakers, bulkhead patterns |
| **When to Implement** | When we have production metrics showing failure patterns |
| **Estimated Effort** | 2-3 days |
| **Technology** | Resilience4j |

#### 2. Distributed Tracing (Full Observability)

| Attribute | Value |
|-----------|-------|
| **Status** | Deferred to V0.2 |
| **Risk Level** | Medium |
| **Why Deferred** | Structured logging is sufficient for MVP debugging |
| **What We Have** | Health checks, structured JSON logging with correlation IDs |
| **What's Missing** | OpenTelemetry instrumentation, Datadog APM, trace visualization |
| **When to Implement** | When debugging becomes complex across services |
| **Estimated Effort** | 3-4 days |
| **Technology** | OpenTelemetry + Datadog |

---

### MEDIUM PRIORITY (V0.3+)

#### 3. Saga Pattern / Distributed Transactions

| Attribute | Value |
|-----------|-------|
| **Status** | Deferred to V0.3+ |
| **Risk Level** | Low |
| **Why Deferred** | Current flows are simple; Stripe handles payment consistency |
| **What We Have** | Event-driven architecture with Kafka, eventual consistency |
| **What's Missing** | Choreography-based sagas, compensation handlers |
| **When to Implement** | When we add complex multi-service transactions (inventory, reservations) |
| **Estimated Effort** | 5-7 days |

#### 4. High Availability / Multi-Region

| Attribute | Value |
|-----------|-------|
| **Status** | Deferred to V0.3+ |
| **Risk Level** | Low for MVP |
| **Why Deferred** | Single-region deployment is sufficient; cost not justified |
| **What We Have** | Docker Compose, single-region deployment |
| **What's Missing** | Multi-region deployment, database replication, CDN distribution |
| **When to Implement** | When we have SLA requirements or global user base |
| **Estimated Effort** | 5-10 days + significant cost increase |

#### 5. Full Compliance Suite (GDPR, SOC2)

| Attribute | Value |
|-----------|-------|
| **Status** | Basic implementation pending |
| **Risk Level** | Depends on target market |
| **What We Have** | Encrypted PII, secure authentication |
| **MVP Required** | `DELETE /api/v1/users/me` endpoint - MUST implement before launch |
| **What's Missing (V0.2+)** | Consent management, data export, audit logs for compliance, privacy policy integration |
| **When to Implement Full** | Before EU market launch or enterprise sales |
| **Estimated Effort** | MVP endpoint: 2-3 hours, Full GDPR: 5-10 days, SOC2: weeks |

---

### LOW PRIORITY (Future)

#### 6. Advanced Rate Limiting

| Attribute | Value |
|-----------|-------|
| **Status** | Basic implementation |
| **What We Have** | Tiered rate limiting (anonymous/auth/premium) with Bucket4j |
| **What's Missing** | Distributed rate limiting across instances, API quotas per subscription |
| **When to Implement** | When we scale horizontally |

#### 7. Feature Flags

| Attribute | Value |
|-----------|-------|
| **Status** | Not implemented |
| **Why** | MVP does not need gradual rollouts |
| **When to Implement** | When we have A/B testing or gradual feature rollout needs |
| **Technology Candidate** | LaunchDarkly, Unleash, or custom |

#### 8. API Versioning Strategy

| Attribute | Value |
|-----------|-------|
| **Status** | V1 only |
| **What We Have** | All endpoints at `/api/v1/` |
| **What's Missing** | Version negotiation, deprecation strategy, migration guides |
| **When to Implement** | Before breaking API changes |

---

## Accepted Trade-offs

These are conscious decisions, not debt:

| Decision | Rationale | Acceptable Until |
|----------|-----------|------------------|
| Single database instance | Cost; MVP does not need HA | 1000+ users |
| No read replicas | Complexity; read load is low | Noticeable read latency |
| Synchronous email sending fallback | Kafka handles async; sync is backup | Never an issue |
| No CDN for static assets | Low traffic | Global user base |
| No blue-green deployments | Zero-downtime deploys handled by platform | Complex deployment needs |

---

## Recently Resolved

| Item | Resolution | Date |
|------|------------|------|
| Health Checks | Implemented custom indicators (DB, Redis, Kafka) | Jan 2026 |
| API Documentation | OpenAPI/Swagger in all services | Jan 2026 |

---

## Technology Reference

| Component | Version |
|-----------|---------|
| Java | 25 |
| Spring Boot | 3.4.1 |
| jjwt | 0.13.0 |
| Testcontainers | 1.20.4 |

---

## Review Process

1. **Each phase completion**: Review this document.
2. **V0.2 planning**: Prioritize HIGH items.
3. **V0.3 planning**: Prioritize MEDIUM items.
4. **Before enterprise sales**: Address compliance items.

---

## Notes

- This document is intentionally lean for MVP.
- Not all "debt" is bad -- some is strategic deferral.
- Review before adding new features that depend on missing infrastructure.
