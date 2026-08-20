# Final Engineering Summary

## Plan and rationale

The prototype was implemented as a layered Spring Boot application with an intentionally low-friction local setup. The design prioritizes a clear core path—create, redirect, inspect, analyze, deactivate—while adding a bounded set of reliability and security controls that can be defended in an interview.

The architecture avoids unnecessary infrastructure in the runnable baseline. H2 and local rate limiting are deliberate prototype choices, not production claims.

## Artifacts delivered

- runnable Spring Boot application
- URL creation and redirect APIs
- metadata and analytics APIs
- deactivation/lifecycle behavior
- persistent prototype data model
- unit/service/integration tests
- standard API error model
- request correlation IDs
- health/metrics endpoints
- rate limiting
- Dockerfile
- GitHub Actions CI
- PowerShell and shell demo scripts
- Postman collection
- architecture, API, testing, AI traceability, scenario, and risk documentation

## Validation strategy

Automated validation is defined through `mvn clean verify`. Manual validation covers health, create, redirect, analytics, metadata, invalid input, alias conflict, and deactivation.

### Submission-time validation result

**Update this section after running locally before you submit.**

```text
Command: mvn clean verify
Result: <REPLACE WITH YOUR ACTUAL RESULT>
Date/time: <REPLACE>
Environment: Java 21 / Maven <REPLACE VERSION>
```

Do not claim a passing build until this command has actually passed in the submission environment.

## Key risks and trade-offs

- H2 improves portability but should become PostgreSQL in production.
- local fixed-window rate limiting should become gateway/Redis distributed limiting.
- click analytics should move to asynchronous event ingestion under high redirect traffic.
- abuse/reputation controls are needed for a public shortening product.
- application-side analytics aggregation needs a bounded/scalable query design for large event volumes.

## Assumptions

- Public anonymous link creation is acceptable for the prototype because no identity or tenancy requirement was provided.
- Only HTTP and HTTPS destinations are in scope.
- Default expiry is 30 days; maximum expiry is 365 days as an explicit ambiguity resolution.
- A `302` redirect is preferred so destination behavior can change in future without encouraging permanent client caching.
- Daily analytics uses UTC.

## Limitations

- no authentication/authorization
- no distributed deployment state
- no destination reputation service
- no async messaging platform
- no production database migration tool
- no UI; API-only service
- no formal load-test harness in the baseline

## Engineering ownership

AI assistance was used to accelerate architecture exploration, implementation scaffolding, test drafting, review preparation, and documentation. The submission still requires engineer ownership: inspect the source, run the tests, reproduce the demo, understand the rejected alternatives, and approve the final Git diff before sharing it.
