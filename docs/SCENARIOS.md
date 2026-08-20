# Required Engineering Scenarios

The assignment asks for greenfield, brownfield, and ambiguous scenarios that each show decomposition, execution, and validation. These scenarios are intentionally tied to this codebase so they are defensible in a review.

## Scenario 1 — Greenfield: build the initial URL-shortening service

### Requirement
Build a URL shortener from scratch with core APIs.

### Interpretation
A minimal useful slice must support creation and redirection, persist mappings, reject invalid input, and expose deterministic HTTP behavior.

### Decomposition

1. Define API contract and status codes.
2. Define `ShortUrl` persistence model and unique short-code constraint.
3. Implement safe URL validation.
4. Implement generated Base62 codes and custom aliases.
5. Implement create service and controller.
6. Implement redirect lookup and HTTP `302` response.
7. Add error model and request ID.
8. Add unit and integration tests.

### Dependencies and sequence
Data model and API contract precede controller implementation. URL policy and code generation are isolated before integrating them into `ShortUrlService`. Integration tests are added after the vertical slice exists.

### Validation

- Valid HTTP/HTTPS URL creates successfully.
- Unsafe scheme is rejected.
- Generated code follows expected alphabet/length.
- Duplicate custom alias returns conflict.
- Redirect returns exact `Location` header.
- Unknown code returns `404`.

### Outcome
A runnable vertical slice with separation between HTTP, domain, and persistence concerns.

---

## Scenario 2 — Brownfield: add analytics without breaking redirects

### Existing behavior
The system already creates and redirects short URLs.

### Enhancement
Add click analytics and analytics APIs.

### Impact analysis
Impacted areas:

- redirect flow
- persistence schema
- repository layer
- service aggregation logic
- API response model
- tests
- operational risk because redirect is latency-sensitive

### Engineering concern
A naive implementation can make every redirect depend on analytics persistence. That couples core availability to a secondary feature.

### Decomposition

1. Add `ClickEvent` entity and index by URL/time.
2. Add repository queries.
3. Introduce an `AnalyticsRecorder` rather than placing analytics persistence in the controller or URL service.
4. Record bounded referrer/user-agent values and hash client address.
5. Add optional range query and daily aggregation.
6. Add analytics assertions to end-to-end integration test.
7. Document higher-scale asynchronous evolution.

### Validation

- A successful redirect records a click.
- Analytics query returns the click count.
- Range ordering is validated.
- Data model has an index supporting URL/time access.
- Raw client address is not stored.

### Rejected alternative
Store an ever-increasing click count directly on the `short_urls` row for every redirect. Rejected because it creates a hot-row write under traffic and provides no temporal analytics.

### Outcome
Analytics is added with explicit understanding of the redirect critical path and a clear production path to asynchronous event ingestion.

---

## Scenario 3 — Ambiguous: “add reliability features”

### Ambiguity
“Reliability” could mean replication, retries, timeouts, graceful shutdown, health checks, rate limits, durable storage, caching, asynchronous processing, observability, or disaster recovery. Implementing all of them would be inappropriate for a two-to-three-day prototype.

### Normalized requirement
Implement a small set of reliability controls that are valuable in a standalone prototype, then document what changes for a production distributed system.

### Selected controls

1. Database unique constraint as final collision authority.
2. Bounded retry for generated-code collisions/races.
3. Graceful shutdown configuration.
4. Actuator health/metrics endpoints.
5. Request correlation ID.
6. Explicit `404` vs `410` semantics.
7. Creation rate limiting.
8. Persistent H2 file mode for local restarts.
9. CI test gate.

### Explicit non-goals for prototype

- multi-region deployment
- distributed consensus
- distributed cache
- queue/stream infrastructure
- PostgreSQL HA topology
- production traffic reputation service

### Trade-off
In-memory rate limiting is simple and reviewable but does not enforce a global quota across replicas. This is documented rather than hidden. A production deployment moves enforcement to the gateway or Redis.

### Validation

- Collision logic has retry tests/logic and unique DB constraint.
- Health endpoint is available.
- Responses include request IDs.
- rate-limit failure maps to HTTP `429`.
- application supports graceful shutdown.
- CI runs `mvn clean verify` on pushes and pull requests.

### Outcome
The ambiguity is resolved through scoped engineering judgment rather than uncontrolled feature growth.
