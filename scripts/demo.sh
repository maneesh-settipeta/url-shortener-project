#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${BASE_URL:-http://localhost:8080}"
ALIAS="${1:-vendor-demo}"

printf '\n1) Health\n'
curl -sS "$BASE_URL/actuator/health" | python -m json.tool

printf '\n2) Create short URL\n'
curl -sS -X POST "$BASE_URL/api/v1/urls" \
  -H 'Content-Type: application/json' \
  -d "{\"url\":\"https://example.com/docs\",\"customAlias\":\"$ALIAS\"}" | python -m json.tool

printf '\n3) Redirect response headers\n'
curl -sS -I "$BASE_URL/$ALIAS"

printf '\n4) Analytics\n'
curl -sS "$BASE_URL/api/v1/urls/$ALIAS/analytics" | python -m json.tool

printf '\n5) Metadata\n'
curl -sS "$BASE_URL/api/v1/urls/$ALIAS" | python -m json.tool
