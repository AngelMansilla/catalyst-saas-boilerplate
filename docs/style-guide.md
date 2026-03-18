# Catalyst Style Guide

## Language Policy
- **Code** (variables, methods, classes): English only.
- **Comments**: English only.
- **Commits**: English only.
- **Documentation**: English only.
- **User communication**: Spanish is acceptable.

## Naming Conventions

### Backend (Java)
| Element | Convention | Example |
|---------|-----------|---------|
| Classes | PascalCase | `PaymentService`, `UserRepository` |
| Methods | camelCase | `processPayment`, `findUserById` |
| Constants | UPPER_SNAKE_CASE | `MAX_RETRY_ATTEMPTS` |
| Packages | lowercase | `com.catalyst.payment.domain` |
| Interfaces | Descriptive, no "I" prefix | `PaymentGateway` (not `IPaymentGateway`) |

### Frontend (TypeScript / Angular)
| Element | Convention | Example |
|---------|-----------|---------|
| Components | PascalCase | `PaymentForm`, `UserProfile` |
| Functions | camelCase | `handleSubmit`, `fetchUserData` |
| Constants | UPPER_SNAKE_CASE | `API_BASE_URL` |
| Types / Interfaces | PascalCase | `User`, `PaymentRequest` |
| Files | kebab-case | `payment-form.component.ts`, `user-profile.component.ts` |

### Database
| Element | Convention | Example |
|---------|-----------|---------|
| Tables | snake_case, plural | `users`, `payment_transactions` |
| Columns | snake_case | `user_id`, `created_at` |
| Indexes | `idx_tablename_columnname` | `idx_users_email` |

## Code Organization

### Backend — Hexagonal Architecture
```java
// Domain Layer — No framework dependencies
package com.catalyst.payment.domain;

public class Payment {
    // Pure business logic
}

// Application Layer — Use cases and ports
package com.catalyst.payment.application;

public interface PaymentGateway {
    // Port definition
}

// Infrastructure Layer — Adapters with Spring
package com.catalyst.payment.infrastructure;

@Component
public class StripePaymentGateway implements PaymentGateway {
    // Adapter implementation
}
```

**Rules:**
1. No Spring annotations in the domain layer.
2. All external dependencies are injected via ports.
3. Use cases orchestrate domain logic.
4. Infrastructure adapters implement ports.
5. Each service owns its data.

### Frontend — Feature-Based Organization
```
/src/app/features/payment
  /components    # Angular components
  /services      # API call services
  /models        # TypeScript types/interfaces
  /guards        # Route guards
  /pipes         # Custom pipes
```

**Rules:**
1. API calls are isolated in service files.
2. Components are primarily presentational.
3. Business logic lives in services or custom utilities.
4. Features are lazy-loaded.
5. Shared logic goes in `src/app/core` (cross-feature) or `src/app/shared` (reusable UI).

## Testing Standards

### Backend Tests
```java
@Test
@DisplayName("Should process payment successfully when valid data is provided")
void shouldProcessPaymentSuccessfully() {
    // Given — set up test state
    // When — execute the action
    // Then — verify the outcome
}
```

- Use the **Given / When / Then** pattern.
- Use `@DisplayName` for human-readable test names.
- Test method names: `shouldDoSomethingWhenCondition`.

### Frontend Tests
```typescript
describe('PaymentForm', () => {
  it('should submit payment when form is valid', () => {
    // Arrange
    // Act
    // Assert
  });
});
```

- Use the **Arrange / Act / Assert** pattern.
- Descriptive `it()` blocks that read as specifications.

### Coverage Target
- Minimum **80%** test coverage across the project.

## Version Control Standards

### Branch Naming
- Feature branches: `feat/{id}-{description}` (e.g., `feat/shared-kernel`, `feat/payment-service`)
- Bug fix branches: `fix/{id}-{description}` (e.g., `fix/token-expiration`)

### Commit Format
Use **Conventional Commits**: `<type>(<scope>): <description>`

**Types:**
| Type | Usage |
|------|-------|
| `feat` | New functionality |
| `fix` | Bug fix |
| `docs` | Documentation changes |
| `style` | Formatting only (no logic changes) |
| `refactor` | Code restructuring (no behavior change) |
| `test` | Adding or correcting tests |
| `chore` | Build process, tooling, or dependency changes |

**Scope:** Module or component name (e.g., `shared-kernel`, `payment-service`, `auth`).

**Description:** Concise, imperative mood ("add", "implement", "fix"), no trailing period.

### Examples
```
feat(shared-kernel): add JWT token service
fix(auth): resolve token expiration overflow
docs(readme): update deployment instructions
test(payment): add integration tests for Stripe
refactor(security): extract password validation logic
chore(maven): update Spring Boot version to 3.4.1
```

### Commit Granularity
- **Atomic commits**: Each commit represents a single, logical change.
- **Sub-task commits**: Commit after completing each significant sub-task (e.g., domain models, then ports, then adapters). Do not wait until a feature is complete.
- **Compilation**: Code must compile and pass tests before committing.

## Documentation Standards

### Code Comments
- Use JavaDoc for public APIs.
- Explain **why**, not **what**.
- Keep comments synchronized with code changes.


## Security Standards
- Never commit secrets or API keys.
- Use environment variables for all configuration secrets.
- Validate all user inputs.
- Sanitize data before database operations.
- Use prepared statements for SQL queries.
- Implement rate limiting on all public endpoints.

## Performance Standards
- Use caching where appropriate (Redis).
- Implement pagination for large datasets.
- Use async patterns for I/O operations.
- Optimize database queries (indexes, explain plans).
- Lazy-load frontend feature modules.

## Error Handling
- Define custom exceptions in the domain layer.
- Use a global exception handler in the infrastructure layer.
- Provide meaningful error messages for end users.
- Log detailed error information for developers.
- Never expose internal errors or stack traces to end users.

## Code Quality Principles
- Follow **SOLID** principles.
- Keep methods small and focused (single responsibility).
- Avoid code duplication (**DRY**).
- Write self-documenting code with meaningful names.
- Maintain high test coverage (>80%).
