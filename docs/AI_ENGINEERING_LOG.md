# AI-Assisted Engineering Log

## Operating principle

AI is used as an engineering accelerator inside bounded tasks. Human engineering ownership remains explicit for requirements, architecture, acceptance criteria, security decisions, trade-offs, test interpretation, and final approval.

## Traceability format

Each task below records the intent, constraints, AI contribution, engineer action, and validation. This is meant to make the process reviewable rather than presenting generated output as automatically correct.

## Task 1 — Requirement normalization

**Intent:** Convert a broad URL-shortener assignment into a concrete prototype scope.

**Constraints supplied to AI:** Java/Spring Boot, runnable locally, production-minded, core APIs, analytics, reliability, tests, documentation, three scenario types, engineer-led execution.

**AI contribution:** Proposed API surface, domain model, reliability options, and documentation structure.

**Engineer decision/edit:** Kept the API narrow; selected H2 for evaluator portability; explicitly separated prototype and production recommendations; avoided adding authentication because user identity/tenancy was not part of the requirement.

**Rejected:** Adding Kubernetes, Kafka, Redis, PostgreSQL, OpenAPI UI, authentication, and a frontend to the runnable baseline. These would increase setup cost without proving the core assignment better.

**Validation:** Scope mapped against assignment deliverables in `ASSIGNMENT_MAPPING.md`.

## Task 2 — Greenfield vertical slice

**Intent:** Implement create + redirect end to end.

**Prompt pattern:** “Implement the next vertical slice only. Preserve controller/service/repository separation. Use Java 21 and Spring Boot conventions. Define expected HTTP status codes and edge cases before code.”

**AI contribution:** Initial DTO/entity/service/controller scaffolding and edge-case checklist.

**Engineer decision/edit:** Database unique constraint is treated as concurrency authority; generated codes use `SecureRandom`; custom alias race maps to `409`; unsafe URL schemes and embedded credentials are rejected.

**Validation:** Unit tests for URL policy/code generation and integration flow.

## Task 3 — Brownfield analytics enhancement

**Intent:** Add analytics to an existing redirect flow with minimal regression risk.

**Prompt pattern:** “Identify impacted modules and critical-path risks before modifying code. Do not make redirect correctness depend conceptually on analytics success.”

**AI contribution:** Suggested event model, repository access, aggregation, and integration-test changes.

**Engineer decision/edit:** Kept per-click event storage for reviewability and time-range analytics; hashed client address; bounded metadata lengths; documented queue-based production evolution.

**Rejected:** Incrementing a single counter column on every redirect because of hot-row contention and loss of time-series detail.

**Validation:** End-to-end test covers create -> redirect -> analytics -> deactivate.

## Task 4 — Ambiguous reliability requirement

**Intent:** Resolve “reliability features” without overbuilding.

**Prompt pattern:** “List plausible interpretations, rank them by prototype value, implementation cost, and production relevance, then implement only the smallest defensible subset.”

**AI contribution:** Candidate reliability controls and trade-off table.

**Engineer decision/edit:** Selected uniqueness/retry, health/metrics, request IDs, graceful shutdown, explicit lifecycle semantics, rate limiting, local durable data, and CI.

**Rejected:** Distributed cache/queue/HA database in the baseline because they require external services and distract from evaluator setup.

**Validation:** Configuration review, API tests, CI workflow, documented production path.

## Task 5 — Test generation and review

**Intent:** Cover domain rules and the main HTTP workflow.

**AI contribution:** Proposed test cases and initial JUnit structure.

**Engineer review criteria:**

- tests assert behavior, not implementation detail
- fixed clocks where time affects deterministic service behavior
- database reset between integration tests
- negative test for unsafe URL scheme
- redirect and analytics exercised together

**Validation command:**

```bash
mvn clean verify
```

The person submitting the repository should run this command in their local environment and record the actual result in `FINAL_ENGINEERING_SUMMARY.md`.

## Task 6 — Documentation and review preparation

**Intent:** Make design and AI-assisted execution defensible to reviewers.

**AI contribution:** Drafted architecture, scenario, API, testing, risk, and screenshot guidance.

**Engineer responsibility before submission:** Read every document, remove any statement that does not match the final code, run all commands, inspect the Git diff, and own the resulting repository.

## Quality gates

The intended quality-gate sequence is:

1. Requirement-to-artifact mapping.
2. Compile/test: `mvn clean verify`.
3. Manual API smoke test.
4. Negative-path smoke test.
5. Health endpoint check.
6. Review logs/request IDs.
7. Inspect persisted behavior across restart if desired.
8. Dependency/security scan in the target environment if available.
9. Human code review and final sign-off.

## Secure AI-use controls

- No real customer data, secrets, credentials, private keys, or proprietary production code should be placed into external AI prompts.
- AI suggestions are treated as untrusted until reviewed and tested.
- Dependency versions and framework behavior should be verified against authoritative documentation.
- High-impact production changes require engineer approval and normal change controls.
- Generated tests are not evidence of correctness by themselves; assertions and failure behavior must be reviewed.
