#!/usr/bin/env bash
# ==============================================================================
# scripts/test-all.sh — Run unit & security tests across all SOC microservices
# Usage: bash scripts/test-all.sh [--owasp]
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

RUN_OWASP=false
if [[ "${1:-}" == "--owasp" ]]; then
  RUN_OWASP=true
fi

PASS=()
FAIL=()
OWASP_FAIL=()

echo ""
echo "=============================================="
echo "  SOC Microservices — Test All"
echo "  OWASP scan: $([ "$RUN_OWASP" = true ] && echo 'YES' || echo 'NO')"
echo "=============================================="
echo ""

for service in "${SERVICES[@]}"; do
  echo "──────────────────────────────────────────────"
  echo "  Testing: $service"
  echo "──────────────────────────────────────────────"
  svc_dir="$ROOT_DIR/$service"

  if [ ! -d "$svc_dir" ]; then
    echo "  ⚠️  Directory not found: $svc_dir — skipping"
    FAIL+=("$service (missing dir)")
    continue
  fi

  # Run unit tests
  if (cd "$svc_dir" && ./mvnw clean test --no-transfer-progress); then
    echo "  ✅ $service — TESTS PASSED"
    PASS+=("$service")
  else
    echo "  ❌ $service — TESTS FAILED"
    FAIL+=("$service")
  fi

  # Optionally run OWASP scan
  if [ "$RUN_OWASP" = true ]; then
    echo "  🔍 Running OWASP dependency check for $service..."
    if (cd "$svc_dir" && ./mvnw org.owasp:dependency-check-maven:check -DfailBuildOnCVSS=8 --no-transfer-progress); then
      echo "  ✅ $service — OWASP PASSED"
    else
      echo "  ⚠️  $service — OWASP vulnerabilities found (CVSS ≥ 8)"
      OWASP_FAIL+=("$service")
    fi
  fi

  echo ""
done

echo "=============================================="
echo "  Test Summary"
echo "=============================================="
echo "  ✅ Passed  (${#PASS[@]}): ${PASS[*]:-none}"
echo "  ❌ Failed  (${#FAIL[@]}): ${FAIL[*]:-none}"
if [ "$RUN_OWASP" = true ]; then
  echo "  ⚠️  OWASP   (${#OWASP_FAIL[@]}): ${OWASP_FAIL[*]:-none}"
fi
echo ""
echo "  Reports: <service>/target/surefire-reports/"
if [ "$RUN_OWASP" = true ]; then
  echo "  OWASP:   <service>/target/dependency-check-report.html"
fi
echo ""

if [ ${#FAIL[@]} -gt 0 ]; then
  exit 1
fi
