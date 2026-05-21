#!/usr/bin/env bash
set -euo pipefail

source "$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)/common.sh"

log "No Android-only E2E edge suites are registered yet."
log "Register Compose/Espresso/UI Automator suites under e2e/platform/android when Maestro cannot cover a case."
