---
name: skill-lookup
description: Hoops project skill index. Lists available skills and their purposes. Reference when unsure which skill to use.
---

# Hoops Skill Index

## Development Workflow Skills

| Skill | Purpose | Trigger |
|-------|---------|---------|
| `/architecture-patterns` | Hexagonal Architecture structure, package design | New domain creation, package structure questions |
| `/tdd-workflow` | ATDD workflow, Cucumber scenario writing | Starting feature implementation, writing tests |
| `/create-pull-request` | PR creation, commit analysis, branch management | PR creation request |

## Quality Management Skills

| Skill | Purpose | Trigger |
|-------|---------|---------|
| `/clean-code` | Self-validating entities, VO parameter grouping, transaction strategies | Domain model design, refactoring |
| `/code-review-excellence` | Code review guide, feedback writing | PR review, code inspection |
| `/debugging-strategies` | Systematic debugging, performance profiling | Bug investigation, performance issues |

## Meta Skills

| Skill | Purpose | Trigger |
|-------|---------|---------|
| `/skill-creator` | New skill creation guide | Skill creation/modification request |

## Required Usage Rules

1. **Architecture work** -> `/architecture-patterns` required
2. **Feature implementation** -> `/tdd-workflow` reference (Cucumber scenarios first)
3. **PR creation** -> `/create-pull-request` required (tests must pass)
