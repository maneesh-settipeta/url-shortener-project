# Risks, Trade-offs, and Guardrails

## 1. H2 is not the production datastore

**Reason used here:** evaluator can clone and run without installing infrastructure.

**Risk:** limited production concurrency/operations compared with a managed relational database.

**Production action:** PostgreSQL with migrations, backups, replication, capacity monitoring, connection-pool tuning, and tested recovery procedures.

## 2. In-memory rate limiting is per JVM

**Reason used here:** lightweight reliability/abuse control with no external dependency.

**Risk:** multiple instances each enforce their own limit; restart clears counters.

**Production action:** enforce at API gateway or use Redis-backed distributed limiting.

## 3. Synchronous click persistence adds redirect work

**Reason used here:** keeps prototype understandable and analytics immediately observable.

**Risk:** database latency can increase redirect latency. A database failure can cause analytics loss; the design intentionally prioritizes redirection over analytics completeness.

**Production action:** publish click events asynchronously to Kafka/queue and process them independently.

## 4. Short links can be abused

**Risk:** phishing, malware, spam, or deceptive destinations.

**Current guardrails:** HTTP/HTTPS only, embedded credentials rejected, create rate limiting.

**Production action:** authentication/tenant controls if applicable, reputation scanning, abuse reporting, takedown workflow, domain policies, and stronger quotas.

## 5. Random-code collision

Seven Base62 characters provide a large namespace, but collision probability is not zero.

**Guardrail:** database unique constraint plus bounded retry. The system never assumes random generation guarantees uniqueness.

## 6. Analytics privacy

**Risk:** IP addresses/user agents can be sensitive operational data.

**Current guardrail:** client address is hashed rather than stored raw; stored headers are length bounded.

**Production action:** define retention, legal basis, access controls, salt/key strategy if pseudonymous correlation is required, and data minimization policy.

## 7. H2 schema auto-update

`ddl-auto: update` is convenient for a prototype.

**Production risk:** implicit schema mutation is not controlled change management.

**Production action:** use Flyway or Liquibase and set Hibernate schema behavior appropriately.

## 8. Analytics aggregation in application memory

Current daily grouping loads matching click events for the requested URL/range and groups in Java.

**Risk:** unsuitable for very high event counts or unbounded ranges.

**Production action:** aggregate in database/materialized views or analytics store; enforce query range/page constraints.

## 9. Open redirect is the product behavior

A URL shortener intentionally redirects to user-provided destinations. This differs from accidental open-redirect vulnerabilities in normal applications.

**Guardrail:** destination validation is still required, and production products generally add reputation/abuse controls.

## 10. Human oversight

AI-generated or AI-edited code can be syntactically plausible but wrong.

**Guardrails:** engineer review, deterministic tests, manual smoke tests, authoritative framework docs, explicit rejected alternatives, and final sign-off.
