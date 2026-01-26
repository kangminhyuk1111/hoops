---
name: clean-code
description: Clean code principles for domain-driven design. Covers self-validating entities, parameter reduction with value objects, and transaction strategies.
---

# Clean Code Principles for Domain-Driven Design

These guidelines define HOW to implement domain models and services with clean code practices. For WHERE components belong (package structure, naming conventions), refer to `/architecture-patterns` skill.

## Self-Validating Entity Pattern

Domain entities must guarantee their own validity. An entity in an invalid state must never exist.

### Validation Responsibility by Layer

| Validation Type | Location | Decision Criteria |
|-----------------|----------|-------------------|
| Domain Invariant | Entity `create()` | "Is the entity meaningless if this rule is broken?" |
| Business Policy | `domain/policy/` | "Can this rule change based on business decisions?" |
| External State | `application/service/` | "Does this require database or external system queries?" |

### Why Self-Validation Matters

When validation exists only in the service layer:
- Other services or tests can bypass validation by creating entities directly
- Validation logic becomes duplicated across multiple services
- Entities fail to express their own invariants

### Implementation Pattern

```java
public class Entity {
    private Entity(/* params */) { }  // Private constructor

    public static Entity create(/* params */) {
        validateInvariants(/* params */);
        return new Entity(/* params */);
    }

    public static Entity reconstitute(/* params */) {
        return new Entity(/* params */);  // Skip validation for DB restoration
    }
}
```

### Invariant vs Policy

| Type | Example | Reasoning |
|------|---------|-----------|
| Invariant | "End time must be after start time" | Logically impossible otherwise |
| Policy | "Booking must be at least 1 hour in advance" | Business may change this threshold |

## Tell, Don't Ask Principle

Domain objects must perform their own behavior internally. Request behavior instead of querying state to make external decisions.

```java
// Anti-pattern: Ask then decide
public void reactivateMatch(Long matchId, Long userId) {
    Match match = repository.findById(matchId);
    if (match.getStatus() != MatchStatus.CANCELLED) {  // Asking
        throw new CannotReactivateException(matchId);
    }
    match.setStatus(MatchStatus.RECRUITING);  // External state change
}

// Correct: Tell the object
public void reactivateMatch(Long matchId, Long userId) {
    Match match = repository.findById(matchId);
    match.reactivate(userId);  // Entity owns validation and state change
    repository.save(match);
}
```

**Note**: Query methods for display/logging (`getStatus()`, `isFull()`) are acceptable. The rule applies to behavior decisions.

## Immutability Rules

### Value Objects

Value Objects must be immutable. Use Java records with compact constructors for self-validation:

```java
public record TimeRange(LocalTime start, LocalTime end) {
    public TimeRange {
        if (!start.isBefore(end)) {
            throw new InvalidTimeRangeException(start, end);
        }
    }
}
```

### Entity State Changes

Entity state must change through behavior methods only, never through setters:

```java
// Anti-pattern
public void setStatus(MatchStatus status) { this.status = status; }

// Correct
public void cancel(Long requestUserId) {
    validateHost(requestUserId);
    validateCanCancel();
    this.status = MatchStatus.CANCELLED;
    this.cancelledAt = LocalDateTime.now();
}
```

### Collections

Return defensive copies or unmodifiable views:

```java
public List<OrderItem> getItems() {
    return Collections.unmodifiableList(items);
}
```

## Parameter Object Pattern

When a method accepts more than 5 parameters, group related parameters into Value Objects.

### Grouping Criteria

| Criterion | Example |
|-----------|---------|
| Conceptual Cohesion | latitude + longitude + address → `Location` |
| Lifecycle Coupling | startTime + endTime → `TimeRange` |
| Validation Coupling | start < end requires both values |

### Before and After

```java
// Before: 11 parameters
Entity.create(hostId, hostName, title, description,
              latitude, longitude, address, date, startTime, endTime, capacity)

// After: 5 parameters with Value Objects
Entity.create(Host host, String title, String description,
              Location location, Schedule schedule, Integer capacity)
```

## Transaction Strategy

Separate services by read/write responsibility:

```java
// Query service
@Service
@Transactional(readOnly = true)
public class EntityFinder implements EntityQueryUseCase {
    public Entity findById(Long id) { /* ... */ }
}

// Command service
@Service
@Transactional
public class EntityCreator implements CreateEntityUseCase {
    public Entity create(CreateCommand command) { /* ... */ }
}
```

| Service Type | Annotation | Benefits |
|--------------|------------|----------|
| Query | `@Transactional(readOnly = true)` | Dirty checking disabled, replica routing |
| Command | `@Transactional` | Explicit write intent |

### Anti-Patterns

```java
// 1. Missing annotation on query service
@Service
public class EntityFinder { }  // Should have @Transactional(readOnly = true)

// 2. Write transaction on query methods
@Service
@Transactional  // Should be readOnly = true
public class EntityFinder { }

// 3. Missing override in mixed service
@Service
@Transactional(readOnly = true)
public class EntityService {
    public Entity create(Command cmd) { }  // Needs @Transactional override
}
```

## Method Naming Convention by Layer

Avoid database-oriented naming (`findBy*`) in public-facing interfaces.

| Layer | Pattern | Example | Rationale |
|-------|---------|---------|-----------|
| Domain Repository | `findBy*` | `findById()` | JPA semantics acceptable |
| Outbound Port | Business terms | `getUserInfo()` | Contract with external context |
| UseCase | Business terms | `getMatchById()` | Business operation, not data query |
| Controller | HTTP verb style | `getMatch()` | REST convention |

**Exception**: Internal helpers (`MatchFinder`, `ParticipationFinder`) may use `findBy*` as implementation details.

## Summary Checklist

### Entity Design
- [ ] `create()` validates domain invariants, `reconstitute()` skips validation
- [ ] Private constructor, no setters
- [ ] Behavior methods encapsulate validation and state transitions

### Value Objects
- [ ] Immutable (Java records preferred)
- [ ] Self-validating in compact constructor
- [ ] Collections return defensive copies

### Transaction & Naming
- [ ] Query services: `@Transactional(readOnly = true)`
- [ ] Command services: `@Transactional`
- [ ] Ports/UseCases use business terms, not `findBy*`

## Related Skills

- `/architecture-patterns` - Package structure, layer definitions
