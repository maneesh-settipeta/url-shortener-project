# API Reference

Base URL for local execution: `http://localhost:8080`

All responses include an `X-Request-Id` header. A caller may supply a safe request ID containing letters, numbers, `.`, `_`, or `-`; otherwise the application generates one.

## Create short URL

`POST /api/v1/urls`

Request:

```json
{
  "url": "https://example.com/docs",
  "customAlias": "vendor-demo",
  "expiresAt": "2026-09-30T23:59:59Z"
}
```

Only `url` is required. When `expiresAt` is omitted, the default TTL is 30 days. The maximum TTL is 365 days. `customAlias` must contain 4-32 letters, numbers, hyphens, or underscores.

Success: `201 Created`

```json
{
  "code": "vendor-demo",
  "shortUrl": "http://localhost:8080/vendor-demo",
  "originalUrl": "https://example.com/docs",
  "createdAt": "2026-08-20T14:00:00Z",
  "expiresAt": "2026-09-30T23:59:59Z",
  "active": true,
  "customAlias": true,
  "totalClicks": 0
}
```

Common errors:

- `400 Bad Request` invalid URL/alias/expiration
- `409 Conflict` alias already exists
- `429 Too Many Requests` creation rate limit exceeded

## Redirect

`GET /{code}`

Success: `302 Found`

```text
Location: https://example.com/docs
```

Errors:

- `404 Not Found` unknown code
- `410 Gone` expired or deactivated code

## Metadata

`GET /api/v1/urls/{code}`

Success: `200 OK`

Returns the create-response shape with current active state and lifetime `totalClicks`.

## Analytics

`GET /api/v1/urls/{code}/analytics`

Optional query parameters:

- `from`: ISO-8601 instant
- `to`: ISO-8601 instant

Example:

```text
GET /api/v1/urls/vendor-demo/analytics?from=2026-08-20T00:00:00Z&to=2026-08-21T00:00:00Z
```

Response:

```json
{
  "code": "vendor-demo",
  "totalClicks": 3,
  "firstClickAt": "2026-08-20T14:02:11Z",
  "lastClickAt": "2026-08-20T14:04:39Z",
  "from": "2026-08-20T00:00:00Z",
  "to": "2026-08-21T00:00:00Z",
  "clicksByDay": [
    {
      "date": "2026-08-20",
      "clicks": 3
    }
  ]
}
```

`totalClicks` in this analytics response represents clicks **within the requested range**. The metadata endpoint exposes lifetime click count.

## Deactivate

`DELETE /api/v1/urls/{code}`

Success: `204 No Content`

A subsequent redirect returns `410 Gone`.

## Health

`GET /actuator/health`

Expected status: `200 OK`

## Standard error body

```json
{
  "timestamp": "2026-08-20T14:10:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "only http and https URLs are supported",
  "path": "/api/v1/urls",
  "requestId": "cf3e8e77-5f10-4a51-96e6-f78fc2563e7c",
  "validationErrors": {}
}
```
