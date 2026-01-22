---
name: clean-code
description: Clean code principles for domain-driven design. Covers self-validating entities, parameter reduction with value objects, and transaction strategies.
---

# Clean Code Principles for Domain-Driven Design

These guidelines define HOW to implement domain models and services with clean code practices. For WHERE components belong (package structure, naming conventions), refer to `/architecture-patterns` skill.

## Self-Validating Entity Pattern

Domain entities must guarantee their own validity. An entity in an invalid state must never exist.

### Validation Responsibility by Layer

| Validation Type | Location | Characteristic | Decision Criteria |
|-----------------|----------|----------------|-------------------|
| Domain Invariant | Entity `create()` | Rules that must always hold true | "Is the entity meaningless if this rule is broken?" |
| Business Policy | `domain/policy/` | Rules that may change over time | "Can this rule change based on business decisions?" |
| External State | `application/service/` | Validation requiring external lookups | "Does this require database or external system queries?" |

### Why Self-Validation Matters

When validation exists only in the service layer:
- Other services or tests can bypass validation by creating entities directly
- Validation logic becomes duplicated across multiple services
- Entities fail to express their own invariants
- The codebase becomes fragile and error-prone

### Implementation Pattern

```java
public class Entity {
    private Entity(/* params */) {
        // Private constructor
    }

    public static Entity create(/* params */) {
        validateInvariants(/* params */);  // Enforce invariants
        return new Entity(/* params */);
    }

    public static Entity reconstitute(/* params */) {
        // Skip validation when restoring from database
        // Data is assumed valid as it was validated on creation
        return new Entity(/* params */);
    }

    private static void validateInvariants(/* params */) {
        // Throw domain exceptions for invalid state
    }
}
```

### Invariant vs Policy Examples

| Type | Example | Reasoning |
|------|---------|-----------|
| Invariant | "End time must be after start time" | Logically impossible otherwise |
| Invariant | "Quantity must be positive" | Zero or negative quantity is meaningless |
| Policy | "Booking must be at least 1 hour in advance" | Business may change this threshold |
| Policy | "Maximum 14 days advance booking" | Business may extend or reduce |

## Tell, Don't Ask Principle

Domain objects must perform their own behavior internally. Do not query object state from outside to make decisions. Request behavior instead.

### Anti-Pattern: Ask Then Decide

```java
// Anti-pattern: External code queries state and makes decisions
public void reactivateMatch(Long matchId, Long userId) {
    Match match = repository.findById(matchId);

    // Asking for state
    if (match.getStatus() != MatchStatus.CANCELLED) {
        throw new CannotReactivateException(matchId);
    }
    if (!match.getHostId().equals(userId)) {
        throw new NotHostException(matchId);
    }

    // Telling to change state
    match.setStatus(MatchStatus.RECRUITING);
}
```

Problems with this approach:
- Business rules are scattered in service layer
- Entity becomes an anemic data holder
- Same validation logic duplicated across services
- Entity state can be changed without validation

### Correct Pattern: Tell the Object

```java
// Service layer: orchestrates but doesn't decide
public void reactivateMatch(Long matchId, Long userId) {
    Match match = repository.findById(matchId);
    match.reactivate(userId);  // Tell, don't ask
    repository.save(match);
}

// Entity: owns its behavior and rules
public class Match {
    public void reactivate(Long requestUserId) {
        validateHost(requestUserId);
        validateCanReactivate();
        this.status = MatchStatus.RECRUITING;
        this.cancelledAt = null;
    }

    private void validateHost(Long requestUserId) {
        if (!this.hostId.equals(requestUserId)) {
            throw new NotHostException(this.id, requestUserId);
        }
    }

    private void validateCanReactivate() {
        if (this.status != MatchStatus.CANCELLED) {
            throw new CannotReactivateException(this.id);
        }
    }
}
```

### Query Methods Are Acceptable

Tell, Don't Ask does not prohibit all getters. Query methods that provide information without driving external decisions are acceptable:

```java
// Acceptable: Providing information for display or logging
public MatchStatus getStatus() { return this.status; }
public LocalDateTime getStartTime() { return this.startTime; }

// Acceptable: Query methods for read-only purposes
public boolean isFull() { return this.currentParticipants >= this.maxParticipants; }
public boolean hasStarted() { return LocalDateTime.now().isAfter(this.startTime); }
```

The key distinction: query methods provide information, but behavior decisions must remain inside the entity.

## Immutability Rules

### Value Objects Must Be Immutable

Value objects must be completely immutable with all fields declared as `final`. Use Java record types for value objects.

```java
// Correct: Immutable value object using record
public record TimeRange(LocalTime start, LocalTime end) {
    public TimeRange {
        if (!start.isBefore(end)) {
            throw new InvalidTimeRangeException(start, end);
        }
    }
}

// Correct: Immutable value object using class
public final class Money {
    private final BigDecimal amount;
    private final Currency currency;

    // No setters, all fields final
}
```

### Entity Modification Through Behavior Methods

Entity state changes must occur through behavior methods, never through setters.

```java
// Anti-pattern: Setter allows uncontrolled state change
public void setStatus(MatchStatus status) {
    this.status = status;
}

// Correct: Behavior method with validation
public void cancel(Long requestUserId) {
    validateHost(requestUserId);
    validateCanCancel();
    this.status = MatchStatus.CANCELLED;
    this.cancelledAt = LocalDateTime.now();
}
```

### With-Methods for Immutable Updates

When an immutable object needs modification, provide `with*()` methods that return a new instance:

```java
public class AuthToken {
    private final String accessToken;
    private final String refreshToken;
    private final LocalDateTime expiresAt;

    public AuthToken withRefreshedToken(String newAccessToken, LocalDateTime newExpiresAt) {
        return new AuthToken(newAccessToken, this.refreshToken, newExpiresAt);
    }
}
```

### Defensive Copies for Collections

Collection fields must return defensive copies or unmodifiable views:

```java
public class Order {
    private final List<OrderItem> items;

    // Return unmodifiable view to prevent external modification
    public List<OrderItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    // Or return a defensive copy
    public List<OrderItem> getItems() {
        return new ArrayList<>(items);
    }
}
```

## Parameter Object Pattern with Value Objects

When a factory method or constructor accepts more than 5 parameters, group related parameters into Value Objects.

### Grouping Criteria

| Criterion | Description | Example |
|-----------|-------------|---------|
| Conceptual Cohesion | Data that conceptually belongs together | latitude + longitude + address → Location |
| Lifecycle Coupling | Data that is always created or modified together | startTime + endTime → TimeRange |
| Validation Coupling | Data that must be validated together | start < end validation requires both values |

### Value Object with Self-Validation

Value Objects validate their own consistency at construction time using Java record compact constructors:

```java
public record TimeRange(LocalTime start, LocalTime end) {
    public TimeRange {
        if (!start.isBefore(end)) {
            throw new InvalidTimeRangeException(start, end);
        }
    }

    public Duration duration() {
        return Duration.between(start, end);
    }
}

public record Coordinate(BigDecimal latitude, BigDecimal longitude) {
    public Coordinate {
        Objects.requireNonNull(latitude, "latitude must not be null");
        Objects.requireNonNull(longitude, "longitude must not be null");
        validateRange(latitude, -90, 90, "latitude");
        validateRange(longitude, -180, 180, "longitude");
    }
}
```

### Before and After Comparison

```java
// Before: Too many parameters (11 parameters)
Entity.create(hostId, hostName, title, description,
              latitude, longitude, address,
              date, startTime, endTime, capacity)

// After: Grouped into Value Objects (5 parameters)
Entity.create(Host host, String title, String description,
              Location location, Schedule schedule, Integer capacity)
```

### Benefits

- Reduced parameter count improves readability
- Validation logic encapsulated within Value Objects
- Domain concepts become explicit and named
- Increased reusability across the codebase
- Type safety prevents parameter order mistakes

## Transaction Strategy

### Single Responsibility Service Pattern (Recommended)

Separate services by read/write responsibility and apply appropriate transaction settings to each.

```java
// Query-only service
@Service
@Transactional(readOnly = true)
public class EntityFinder implements EntityQueryUseCase {
    public Entity findById(Long id) { /* ... */ }
    public List<Entity> findAll() { /* ... */ }
}

// Command-only service
@Service
@Transactional
public class EntityCreator implements CreateEntityUseCase {
    public Entity create(CreateCommand command) { /* ... */ }
}

@Service
@Transactional
public class EntityUpdater implements UpdateEntityUseCase {
    public Entity update(UpdateCommand command) { /* ... */ }
}
```

| Service Type | Class-Level Setting | Benefits |
|--------------|---------------------|----------|
| Query Service | `@Transactional(readOnly = true)` | Flush skipped, replica routing possible |
| Command Service | `@Transactional` | Explicit write intent expressed |

### Mixed Service Pattern (When Necessary)

When a single service must handle both reads and writes:

```java
@Service
@Transactional(readOnly = true)  // Default: read-only
public class EntityService {

    public Entity findById(Long id) {
        // Inherits readOnly = true
    }

    public List<Entity> findByCondition(Condition condition) {
        // Inherits readOnly = true
    }

    @Transactional  // Override for write operations
    public Entity create(CreateCommand command) {
        // Write transaction
    }

    @Transactional  // Override for write operations
    public void delete(Long id) {
        // Write transaction
    }
}
```

### Why readOnly Matters

| Aspect | Benefit |
|--------|---------|
| Performance | Hibernate dirty checking disabled, reducing overhead |
| Safety | Prevents accidental data modifications |
| Scalability | Enables read replica routing in database clusters |
| Intent | Clearly communicates the method's purpose |

### Anti-Patterns to Avoid

```java
// Anti-pattern 1: No transaction annotation on query service
@Service
public class EntityFinder {  // Missing @Transactional(readOnly = true)
    public Entity findById(Long id) { /* ... */ }
}

// Anti-pattern 2: Write transaction on query-only methods
@Service
@Transactional  // Should be readOnly = true
public class EntityFinder {
    public Entity findById(Long id) { /* ... */ }
}

// Anti-pattern 3: Missing override on write methods in mixed service
@Service
@Transactional(readOnly = true)
public class EntityService {
    public Entity create(Command cmd) {  // Missing @Transactional override!
        repository.save(entity);  // Will fail or behave unexpectedly
    }
}
```

## Summary Checklist

When implementing domain features, verify:

### Entity Design
- [ ] Factory method `create()` validates all domain invariants
- [ ] Factory method `reconstitute()` skips validation for database restoration
- [ ] Constructor is private to enforce factory method usage
- [ ] No setter methods exist for state changes
- [ ] Behavior methods encapsulate validation and state transitions
- [ ] Entity follows Tell, Don't Ask principle

### Value Objects
- [ ] Related parameters (more than 5) are grouped into Value Objects
- [ ] Value Objects validate their own consistency in compact constructor
- [ ] Value Objects are implemented as Java records
- [ ] Value Objects are immutable
- [ ] Collection fields return defensive copies or unmodifiable views

### Transaction Management
- [ ] Query-only services use `@Transactional(readOnly = true)` at class level
- [ ] Command services use `@Transactional` at class level
- [ ] Mixed services default to `readOnly = true` with method-level overrides
- [ ] No transaction annotations in domain or adapter layers

## Related Skills

For package structure, naming conventions, and component locations, refer to:
- `/architecture-patterns` - Hexagonal architecture, layer definitions, WHERE components belong
