# Catalyst Business Rules

## Product Vision

Catalyst is an elite, reusable SaaS boilerplate that provides production-ready payments, authentication, and enterprise-grade infrastructure out of the box. It eliminates the need to rebuild foundational systems for each new SaaS project.

### Problems Addressed
1. **Time to Market** — Reduces setup time from months to days.
2. **Security** — Provides battle-tested authentication and payment security.
3. **Architecture Complexity** — Offers a clean, maintainable microservices architecture.
4. **Integration** — Pre-configured integrations with Stripe, Kafka, and Angular.
5. **Scalability** — Built with horizontal scaling in mind from day one.
6. **Observability** — Logging, monitoring, and health checks included.

> **Note:** The frontend (Angular 21) is planned but not yet implemented. All page routes and user flows described below represent the target architecture. The backend APIs that support these flows are implemented.

## Subscription Model

### Tiers

| Tier | Monthly Price | Annual Price | Features |
|------|--------------|-------------|----------|
| Free Trial | $0 | N/A | 14 days (configurable via `payment.trial.duration-days`), basic features |
| Professional | $29/mo | $290/yr | All trial features + advanced capabilities |
| Clinic | $99/mo | $990/yr | All professional features + enterprise capabilities |

### Billing Rules
- Both monthly and annual billing cycles are supported.
- Annual billing provides a discount (roughly 2 months free).
- Trial duration is configurable at the application level.
- Stripe Customer Portal is available for self-service payment method management.
- Invoice history is accessible to all paying users.

## Authentication Rules

### Supported Methods
- **Email/Password** — Standard local authentication with BCrypt password hashing.
- **Social Authentication** — Google and GitHub OAuth providers.

### Password Reset Flow
1. User requests a reset via `/forgot-password`.
2. A secure, time-limited token is generated.
3. A reset link is sent via email (Kafka event to notification-service).
4. User sets a new password via `/reset-password` with the token.

### Session and Token Rules
- JWT tokens are issued upon successful login.
- Refresh tokens enable session continuity without re-authentication.
- Tokens are validated on every protected API request.
- Token storage: `localStorage` or `sessionStorage` on the frontend.

## Authorization Rules

### Role-Based Access Control (RBAC)
Users are assigned roles that determine access levels:

| Role | Access Scope |
|------|-------------|
| User (default) | Own profile, dashboard, billing, checkout |
| Admin | All user-level access + user management, subscription management, analytics |

### Rate Limiting by Tier
| Tier | Request Limit |
|------|--------------|
| Anonymous | 30 requests/minute |
| Authenticated | 100 requests/minute |
| Premium | 500 requests/minute |

## User Flows (V0.1)

### Public Flow
```
[Landing Page] → [Pricing/Plans] → [Login/Register]
                                         │
[Forgot Password] → [Reset Password]     │
                                         ↓
```

### Authenticated User Flow
```
[Dashboard] ←──────────────────────────┐
     │                                 │
     ↓                                 │
[Select Plan] → [Checkout/Stripe] → [Success]
     │
     ↓
[Profile/Settings] → [Stripe Customer Portal]
```

### Admin Flow
```
[Admin Dashboard] → [Users List] → [User Details]
         │
         ↓
[Subscriptions] → [Subscription Details]
         │
         ↓
[Analytics] → [Revenue, Signups, Churn]
```

## Pages and Routes

### Public Pages
| Page | Route | Description |
|------|-------|-------------|
| Landing | `/` | Hero section, features, testimonials, CTA |
| Pricing | `/pricing` | Plan cards with monthly/annual toggle |
| Login | `/login` | Email/password + social login buttons |
| Register | `/register` | Registration form |
| Forgot Password | `/forgot-password` | Email input for reset request |
| Reset Password | `/reset-password` | New password form (token required) |

### Protected Pages (User)
| Page | Route | Description |
|------|-------|-------------|
| Dashboard | `/dashboard` | User home / overview |
| Checkout | `/checkout` | Stripe Elements payment form |
| Checkout Success | `/checkout/success` | Payment confirmation |
| Settings | `/settings` | Settings overview |
| Profile | `/settings/profile` | Edit profile details |
| Billing | `/settings/billing` | Subscription info, invoice history |

### Admin Pages
| Page | Route | Description |
|------|-------|-------------|
| Admin Dashboard | `/admin` | Overview metrics |
| Users | `/admin/users` | User management table |
| Subscriptions | `/admin/subscriptions` | Subscription management |

## Transactional Email Rules

Emails are triggered by domain events published to Kafka and processed by the notification-service.

| Trigger | Email Type | Template |
|---------|-----------|----------|
| User registered | Welcome email | `welcome.html` |
| Password reset requested | Reset link email | `password-reset.html` |
| Subscription created | Confirmation email | `subscription-confirmed.html` |
| Payment failed | Alert email | `payment-failed.html` |
| Invoice paid | Receipt email | `invoice-receipt.html` |
| Trial ending soon | Reminder email | `trial-ending.html` |

### Email Delivery Rules
- Emails are sent asynchronously via Kafka consumers.
- Failed deliveries are retried automatically (`@RetryableTopic`).
- Permanently failed messages are routed to a dead-letter queue (DLQ).
- Development environment uses Mailpit (SMTP simulator); production uses Amazon SES.
- Internationalization (i18n) is supported in templates.

## Payment Processing Rules

### Stripe Integration
- Checkout sessions are created server-side via the Stripe API.
- Stripe Elements handles the client-side payment form (PCI-compliant).
- Webhooks from Stripe are the source of truth for payment state transitions.
- The payment-service publishes Kafka events after processing webhooks.

### Subscription Lifecycle
1. User selects a plan on the pricing page.
2. User must be authenticated before subscribing.
3. A Stripe Checkout Session is created.
4. Payment is processed via Stripe.
5. Webhook confirms the subscription status.
6. Subscription record is stored in the `payment` schema.
7. Confirmation email is sent via notification-service.

## Data and Privacy Rules

### Data Ownership
- Each microservice owns its data via schema isolation.
- `auth` schema: user-service (users, roles, tokens).
- `payment` schema: payment-service (subscriptions, invoices).
- `audit` schema: shared event log.

### Security Rules
- All PII is encrypted at rest.
- Passwords are hashed with BCrypt.
- JWT tokens are signed and validated on every request.
- Rate limiting is enforced on all public endpoints.
- All user inputs are validated before processing.
- SQL injection is prevented via prepared statements / JPA.

### Compliance (MVP Scope)
- Basic GDPR "right to be forgotten" via `DELETE /api/v1/users/me` (planned).
- Full GDPR/SOC2 compliance deferred to pre-EU-launch phase.

## Audit Trail
All significant actions are logged to `audit.event_log`:
- Event type, entity type, entity ID
- User ID, event data (JSON)
- Timestamp, IP address
