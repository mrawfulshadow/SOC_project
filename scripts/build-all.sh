#!/usr/bin/env bash
# ==============================================================================
# scripts/build-all.sh — Build all SOC microservices (skip tests for speed)
# Usage: bash scripts/build-all.sh [--with-tests]
# ==============================================================================
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(dirname "$SCRIPT_DIR")"

SERVICES=(
  "api-gateway"
  "auth-service"
  "product-service"
  "order-service"
  "payment-service"
  "notification-service"
)

WITH_TESTS=false
if [[ "${1:-}" == "--with-tests" ]]; then
  WITH_TESTS=true
fi

MVN_FLAGS="--no-transfer-progress"
if [ "$WITH_TESTS" = false ]; then
  MVN_FLAGS="$MVN_FLAGS -DskipTests"
fi

PASS=()
FAIL=()

echo ""
echo "=============================================="
echo "  SOC Microservices — Build All"
echo "  Skip tests: $([ "$WITH_TESTS" = false ] && echo 'YES' || echo 'NO')"
echo "=============================================="
echo ""

for service in "${SERVICES[@]}"; do
  echo "──────────────────────────────────────────────"
  echo "  Building: $service"
  echo "──────────────────────────────────────────────"
  svc_dir="$ROOT_DIR/$service"

  if [ ! -d "$svc_dir" ]; then
    echo "  ⚠️  Directory not found: $svc_dir — skipping"
    FAIL+=("$service (missing dir)")
    continue
  fi

  if (cd "$svc_dir" && ./mvnw clean package $MVN_FLAGS); then
    echo "  ✅ $service — BUILD SUCCESS"
    PASS+=("$service")
  else
    echo "  ❌ $service — BUILD FAILED"
    FAIL+=("$service")
  fi
  echo ""
done

echo "=============================================="
echo "  Build Summary"
echo "=============================================="
echo "  ✅ Passed (${#PASS[@]}): ${PASS[*]:-none}"
echo "  ❌ Failed (${#FAIL[@]}): ${FAIL[*]:-none}"
echo ""

if [ ${#FAIL[@]} -gt 0 ]; then
  exit 1
fi
