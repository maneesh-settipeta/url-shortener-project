param(
    [string]$BaseUrl = "http://localhost:8080",
    [string]$Alias = "vendor-demo"
)

Write-Host "`n1) Health" -ForegroundColor Cyan
Invoke-RestMethod -Uri "$BaseUrl/actuator/health" | ConvertTo-Json -Depth 8

Write-Host "`n2) Create short URL" -ForegroundColor Cyan
$body = @{
    url = "https://example.com/docs"
    customAlias = $Alias
} | ConvertTo-Json
Invoke-RestMethod -Method Post -Uri "$BaseUrl/api/v1/urls" -ContentType "application/json" -Body $body | ConvertTo-Json -Depth 8

Write-Host "`n3) Redirect headers" -ForegroundColor Cyan
try {
    Invoke-WebRequest -Uri "$BaseUrl/$Alias" -MaximumRedirection 0 -ErrorAction Stop
} catch {
    $_.Exception.Response | Format-List StatusCode, Headers
}

Write-Host "`n4) Analytics" -ForegroundColor Cyan
Invoke-RestMethod -Uri "$BaseUrl/api/v1/urls/$Alias/analytics" | ConvertTo-Json -Depth 8

Write-Host "`n5) Metadata" -ForegroundColor Cyan
Invoke-RestMethod -Uri "$BaseUrl/api/v1/urls/$Alias" | ConvertTo-Json -Depth 8
