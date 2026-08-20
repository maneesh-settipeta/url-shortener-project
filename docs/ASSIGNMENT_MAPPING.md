# Assignment-to-Artifact Mapping

This file makes coverage explicit for the interview reviewer.

| Assignment expectation | Repository evidence |
|---|---|
| Requirement understanding | `README.md`, `SCENARIOS.md`, normalized API behavior |
| Task decomposition | `SCENARIOS.md`, `AI_ENGINEERING_LOG.md` |
| Brownfield codebase reasoning | Analytics enhancement scenario and impacted-module analysis |
| AI-assisted execution | `AI_ENGINEERING_LOG.md` with generated/edited/rejected decisions |
| Engineering output generation | Java source, API contracts, persistence model, tests, scripts, CI |
| Validation and risk control | `TESTING.md`, `RISKS_AND_TRADEOFFS.md`, validation/error logic |
| Controlled oversight | AI log and explicit human sign-off checklist |
| Final engineering summary | `FINAL_ENGINEERING_SUMMARY.md` |
| Working prototype | Spring Boot application + H2 + local run commands |
| Architecture overview | `ARCHITECTURE.md` |
| Greenfield scenario | `SCENARIOS.md` scenario 1 |
| Brownfield scenario | `SCENARIOS.md` scenario 2 |
| Ambiguous scenario | `SCENARIOS.md` scenario 3 |
| Setup instructions | `README.md` |
| Testing approach | `TESTING.md` |
| Limitations/trade-offs | `RISKS_AND_TRADEOFFS.md` |

## Core engineering principles demonstrated

### Modular
Controllers, domain service, analytics component, repositories, DTOs, exception handling, request filter, and utilities are separated by responsibility.

### Testable
Pure validation/code-generation utilities are isolated, time-sensitive service behavior supports an injected `Clock`, and an integration test covers the full primary workflow.

### Reliable
Unique constraints, bounded collision retries, lifecycle handling, health endpoints, request IDs, graceful shutdown, and a CI quality gate are included.

### Secure
URL scheme restrictions, embedded credential rejection, alias validation, client-address hashing, bounded stored headers, and basic creation abuse controls are included.

### Scalable path
The prototype documents migration from H2/in-memory limiting/synchronous analytics to PostgreSQL, distributed rate limiting/cache, and asynchronous analytics.

### Safe change management
The brownfield scenario starts with impact analysis, protects existing redirect semantics, adds tests, and documents rejected alternatives.
