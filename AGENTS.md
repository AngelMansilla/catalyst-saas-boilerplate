# Code Review Rules — Project Catalyst

This file defines the coding standards enforced on every commit via automated review.

## Architecture

- **Hexagonal Architecture is non-negotiable**: domain layer must have zero Spring annotations or framework imports.
- Dependencies flow inward only: Infrastructure → Application → Domain. Never the reverse.
- Use cases live in the application layer and orchestrate domain logic. Controllers never contain business logic.
- Every external dependency (DB, Kafka, Stripe, SMTP) must be accessed via a port (interface) defined in the application layer.

## Java / Spring Boot

- Use `record` for immutable DTOs and Value Objects wherever possible.
- Prefer `Optional` over returning `null`. Never pass or return `null` explicitly.
- All public methods in domain and application layers must have meaningful Javadoc.
- No `@Autowired` on fields — constructor injection only.
- Configuration properties must use `@ConfigurationProperties` with typed records, not `@Value`.
- Virtual Threads are enabled — avoid `synchronized` blocks; use `ReentrantLock` if coordination is needed.
- Exceptions: use domain-specific exceptions in the domain layer, translate to HTTP responses only in controllers.

## Testing

- Unit tests cover domain and application layers — no Spring context, pure JUnit 5 + Mockito.
- Integration tests use Testcontainers — no mocking of infrastructure (real DB, real Kafka).
- Architecture tests with ArchUnit enforce layer dependency rules. Do not delete or weaken existing ArchUnit tests.
- Test method naming: `methodName_whenCondition_thenExpectedBehavior()`.
- Minimum coverage target: 80% on application and domain layers.

## Angular / TypeScript

- Use standalone components only — no NgModules.
- State management via Angular Signals. No NgRx unless explicitly justified in a decision doc.
- No `any` type. Use `unknown` with type guards if the type is truly dynamic.
- HTTP calls only inside services — never directly in components.
- Reactive Forms for all forms with validators. No template-driven forms.
- Lazy load every feature module via the router.

## Naming Conventions

### Java
- Use cases: `VerbNounUseCase` (e.g. `CreateSubscriptionUseCase`)
- Ports (input): `VerbNounUseCase` (interface in application/ports/input)
- Ports (output): `NounPort` (e.g. `SubscriptionRepositoryPort`)
- Adapters: `NounAdapter` (e.g. `StripePaymentAdapter`)
- Domain entities: plain nouns, no suffixes (e.g. `User`, `Subscription`)

### Angular
- Components: `FeatureNameComponent` (e.g. `LoginFormComponent`)
- Services: `FeatureNameService` (e.g. `AuthService`)
- Guards: `FeatureNameGuard` (e.g. `AuthGuard`)
- Models/interfaces: plain nouns (e.g. `User`, `AuthTokens`)

## Git

- Commit messages follow Conventional Commits: `type(scope): description`
- Types: `feat`, `fix`, `refactor`, `test`, `chore`, `docs`
- Scope is the service or module: `user-service`, `payment-service`, `shared-kernel`, `frontend`
- No commented-out code in commits.
- No `console.log` or `System.out.println` in production code.

## Security

- Never log sensitive data (passwords, tokens, card numbers).
- JWT secrets and API keys must come from environment variables — never hardcoded.
- All user input must be validated at the controller layer before reaching the application layer.
