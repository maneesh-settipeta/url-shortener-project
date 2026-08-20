# Testing Approach

## Automated test layers

### Utility tests

`UrlPolicyTest`

- accepts HTTP/HTTPS
- normalizes URI path
- rejects unsafe schemes
- rejects embedded credentials
- validates custom aliases and reserved aliases

`Base62CodeGeneratorTest`

- generated value has expected length/alphabet
- a 10,000-item sample contains no duplicate values

The uniqueness sample is a useful sanity check, not a mathematical proof. The database unique constraint remains authoritative.

### Service tests

`ShortUrlServiceTest`

- generated short URL uses expected code and default TTL
- duplicate custom alias returns a conflict exception
- expiration beyond maximum TTL is rejected

A fixed clock makes time-dependent assertions deterministic.

### Integration test

`UrlShortenerIntegrationTest`

Uses Spring Boot + MockMvc + in-memory H2 and validates:

1. create custom short URL
2. request ID response header
3. redirect and `Location` header
4. analytics click count
5. deactivate
6. subsequent `410 Gone`
7. invalid URL rejection

## Run all tests

```bash
mvn clean verify
```

Expected Maven result when successful:

```text
BUILD SUCCESS
```

Do not paste a fabricated result into the submission. Run the command locally and capture the actual terminal output.

## Manual smoke test

Start the application:

```bash
mvn spring-boot:run
```

Then use one of:

```powershell
.\scripts\demo.ps1
```

or:

```bash
./scripts/demo.sh
```

The demo checks health, creation, redirect, analytics, and metadata.

## Additional negative paths worth demonstrating

Duplicate alias:

```bash
curl -X POST http://localhost:8080/api/v1/urls \
  -H "Content-Type: application/json" \
  -d '{"url":"https://example.org","customAlias":"vendor-demo"}'
```

Expected: `409 Conflict` if `vendor-demo` already exists.

Unsafe URL:

```bash
curl -X POST http://localhost:8080/api/v1/urls \
  -H "Content-Type: application/json" \
  -d '{"url":"javascript:alert(1)"}'
```

Expected: `400 Bad Request`.

Unknown code:

```bash
curl -i http://localhost:8080/does-not-exist
```

Expected: `404 Not Found`.

## Performance validation approach

A formal load test is not included because the baseline uses embedded H2 and is meant for local functional evaluation. A realistic production performance test would separately measure:

- redirect p50/p95/p99 latency
- create throughput
- cache hit ratio after introducing Redis
- analytics enqueue latency
- database connection pool saturation
- queue lag / analytics processing lag
- error rate under dependency degradation

## Security validation approach

At minimum before a real deployment:

- dependency vulnerability scan
- static analysis
- secret scan
- fuzz/negative testing of URL parsing and aliases
- abuse/rate-limit tests
- redirect-destination reputation controls if required by product policy
- penetration test against deployed configuration
