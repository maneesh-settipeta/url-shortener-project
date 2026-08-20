# Screenshot Guide for Vendor Submission

Use screenshots that prove behavior, not only source code.

## Screenshot 1 — successful build/tests

From the project root:

```powershell
mvn clean verify
```

Capture the final test summary and `BUILD SUCCESS` in the same screenshot if possible.

## Screenshot 2 — application startup

```powershell
mvn spring-boot:run
```

Capture the Spring Boot banner/startup lines showing the application started on port 8080.

## Screenshot 3 — health endpoint

Open a second PowerShell window:

```powershell
Invoke-RestMethod http://localhost:8080/actuator/health | ConvertTo-Json -Depth 8
```

Capture `"status": "UP"`.

## Screenshot 4 — create short URL

```powershell
$body = @{
  url = "https://example.com/docs"
  customAlias = "vendor-demo"
} | ConvertTo-Json

Invoke-RestMethod -Method Post `
  -Uri "http://localhost:8080/api/v1/urls" `
  -ContentType "application/json" `
  -Body $body | ConvertTo-Json -Depth 8
```

Capture the `code`, `shortUrl`, `originalUrl`, and expiration.

## Screenshot 5 — redirect

Use curl because it clearly displays the status and `Location` header:

```powershell
curl.exe -i http://localhost:8080/vendor-demo
```

Capture:

```text
HTTP/1.1 302
Location: https://example.com/docs
```

Do not use `-L` for this screenshot because following the redirect hides the shortener response.

## Screenshot 6 — analytics

Call the short URL one or two additional times first:

```powershell
curl.exe -I http://localhost:8080/vendor-demo
curl.exe -I http://localhost:8080/vendor-demo
```

Then:

```powershell
Invoke-RestMethod http://localhost:8080/api/v1/urls/vendor-demo/analytics | ConvertTo-Json -Depth 8
```

Capture the click total and `clicksByDay`.

## Screenshot 7 — controlled failure

Deactivate:

```powershell
Invoke-WebRequest -Method Delete http://localhost:8080/api/v1/urls/vendor-demo
curl.exe -i http://localhost:8080/vendor-demo
```

Capture `410 Gone`. This is useful because it proves lifecycle/error handling rather than only the happy path.

## Optional screenshot — GitHub CI

After pushing, capture the green GitHub Actions run showing `mvn -B clean verify` passed.

## Recommended submission set

If the vendor only wants a few screenshots, send:

1. `BUILD SUCCESS`
2. health `UP`
3. create response
4. `302` redirect
5. analytics response
6. `410 Gone`
7. GitHub Actions success, if available
